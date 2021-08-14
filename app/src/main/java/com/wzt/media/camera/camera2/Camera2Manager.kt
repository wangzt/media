package com.wzt.media.camera.camera2

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.hardware.camera2.CameraCaptureSession.CaptureCallback
import android.media.Image
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.text.TextUtils
import android.util.Log
import android.util.Range
import android.util.Size
import android.view.Choreographer.FrameCallback
import android.view.Surface
import com.wzt.media.util.AppEnv
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class Camera2Manager {

    companion object {
        private const val TAG = "Camera2Manager"
    }

    private val mCameraManager: CameraManager =
        (AppEnv.getContext().getSystemService(Context.CAMERA_SERVICE) as CameraManager)

    private var mCameraId: String? = null

    private var mCameraThread: HandlerThread? = null
    private var mCameraHandler: Handler? = null

    private var mImageThread: HandlerThread? = null
    private var mImageHandler: Handler? = null

    private var mImageReader: ImageReader? = null

    private var mCameraDevice: CameraDevice? = null

    private var mCameraCharacteristics: CameraCharacteristics? = null

    /**
     * Max preview width that is guaranteed by Camera2 API
     */
    private val MAX_PREVIEW_WIDTH = 1920

    /**
     * Max preview height that is guaranteed by Camera2 API
     */
    private val MAX_PREVIEW_HEIGHT = 1080

    /**
     * A [Semaphore] to prevent the app from exiting before closing the camera.
     */
    private val mCameraOpenCloseLock: Semaphore = Semaphore(1)

    private var mPreviewSize: Size = Size(1920, 1080)

//    private byte[] mBuffer;

    //    private byte[] mBuffer;
    private var mPreviewRequestBuilder: CaptureRequest.Builder? = null

    private var mCaptureSession: CameraCaptureSession? = null

    private var mFacing = CameraCharacteristics.LENS_FACING_BACK

    private var mFrameCallback: FrameCallback? = null

    private var mSurfaceTexture: SurfaceTexture? = null
    private var mSurfaceTexture1: SurfaceTexture? = null

    private val mImageAvailableListener =
        ImageReader.OnImageAvailableListener { reader ->
            val image = reader.acquireNextImage()
            //            mBuffer = convertYUV420888ToNv21(image);
            //            if (mFrameCallback != null) {
            Log.d(TAG, "##### onFrame: " + image.planes)
            //                mFrameCallback.onFrame(image);
            //            }
            image.close()
        }

    private val mStateCallback: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            mCameraOpenCloseLock.release()
            mCameraDevice = camera
            startCaptureSession()
        }

        override fun onDisconnected(camera: CameraDevice) {
            mCameraOpenCloseLock.release()
            camera.close()
            mCameraDevice = null
        }

        override fun onError(camera: CameraDevice, error: Int) {
            Log.e("DEBUG", "onError: $error")
            mCameraOpenCloseLock.release()
            camera.close()
            mCameraDevice = null
            //多路不出数据，这里重启相机
            Log.e("DEBUG", "onError:  restart camera")
            stopPreview()
            startPreview()
        }
    }

    fun startPreview() {
        if (!chooseCameraIdByFacing()) {
            Log.e(TAG, "Choose camera failed.")
            return
        }
        mCameraThread = HandlerThread("CameraThread")
        mCameraThread?.let {
            it.start()
            mCameraHandler = Handler(it.looper)
        }

        mImageThread = HandlerThread("ImageThread")
        mImageThread?.let {
            it.start()
            mImageHandler = Handler(it.looper)
        }

        prepareImageReader()
        openCamera()
    }

    fun stopPreview() {
        closeCamera()
        mCameraThread?.quitSafely()
        mCameraThread = null
        mCameraHandler = null

        mImageThread?.quitSafely()
        mImageThread = null
        mImageHandler = null

//        mBuffer = null;
    }

    private fun prepareImageReader() {
        mImageReader?.close()
        mImageReader = ImageReader.newInstance(
            mPreviewSize.width,
            mPreviewSize.height,
            ImageFormat.YUV_420_888,
            1
        )
        mImageReader?.setOnImageAvailableListener(mImageAvailableListener, mImageHandler)
    }

    private fun chooseOptimalSize(
        choices: Array<Size>, textureWidth: Int, textureHeight: Int,
        maxWidth: Int, maxHeight: Int, aspectRatio: Size
    ): Size? {
        val bigEnough: MutableList<Size> = ArrayList()
        val notBigEnough: MutableList<Size> = ArrayList()
        val w = aspectRatio.width
        val h = aspectRatio.height
        for (option in choices) {
            if (option.width <= maxWidth && option.height <= maxHeight && option.width == option.height * w / h) {
                if (option.width >= textureWidth && option.height >= textureHeight) {
                    bigEnough.add(option)
                } else {
                    notBigEnough.add(option)
                }
            }
        }
        if (bigEnough.size > 0) {
            return Collections.min(bigEnough, CompareSizesByArea())
        } else if (notBigEnough.size > 0) {
            return Collections.max(notBigEnough, CompareSizesByArea())
        }
        return choices[0]
    }


    private fun chooseCameraIdByFacing(): Boolean {
        try {
            val ids = mCameraManager.cameraIdList
            if (ids.isEmpty()) {
                Log.e(TAG, "No available camera.")
                return false
            }
            for (cameraId in mCameraManager.cameraIdList) {
                val characteristics = mCameraManager.getCameraCharacteristics(cameraId)
                val map = characteristics.get(
                    CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
                ) ?: continue
                val level = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
                if (level == null || level == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
                    continue
                }
                val internal = characteristics.get(CameraCharacteristics.LENS_FACING) ?: continue
                if (internal === mFacing) {
                    mCameraId = cameraId
                    mCameraCharacteristics = characteristics
                    return true
                }
            }
            mCameraId = ids[1]
            mCameraCharacteristics = mCameraManager.getCameraCharacteristics(mCameraId!!)
            val level =
                mCameraCharacteristics?.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
            if (level == null || level == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
                return false
            }
            val internal = mCameraCharacteristics?.get(CameraCharacteristics.LENS_FACING)
                ?: return false
            mFacing = CameraCharacteristics.LENS_FACING_BACK
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        return true
    }

    @SuppressLint("MissingPermission")
    fun openCamera() {
//        configureTransform(previewSize);
        if (TextUtils.isEmpty(mCameraId)) {
            Log.e(TAG, "Open camera failed. No camera available")
            return
        }
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw RuntimeException("Time out waiting to lock camera opening.")
            }
            mCameraManager.openCamera(mCameraId!!, mStateCallback, mCameraHandler)
        } catch (e: InterruptedException) {
            Log.e(TAG, e.message!!)
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.message!!)
        }
    }

    private fun closeCamera() {
        try {
            mCameraOpenCloseLock.acquire()

            mCaptureSession?.close()
            mCaptureSession = null

            mCameraDevice?.close()
            mCameraDevice = null

            mImageReader?.close()
            mImageReader = null
        } catch (e: InterruptedException) {
            throw java.lang.RuntimeException("Interrupted while trying to lock camera closing.", e)
        } finally {
            mCameraOpenCloseLock.release()
        }
    }

    private fun configureTransform(width: Int, height: Int) {}

    private fun startCaptureSession() {
        if (mCameraDevice == null) {
            return
        }
        if (mImageReader != null || mSurfaceTexture != null) {
            try {
                mPreviewRequestBuilder =
                    mCameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                mPreviewRequestBuilder?.addTarget(mImageReader!!.surface)
                //                Zoom zoom = new Zoom(mCameraCharacteristics);
//                zoom.setZoom(mPreviewRequestBuilder, zoom.maxZoom  / 10);
                var surfaceList: List<Surface?>? = listOf(mImageReader?.surface)
                if (mSurfaceTexture != null) {
                    // We configure the size of default buffer to be the size of camera preview we want.
                    mSurfaceTexture?.setDefaultBufferSize(mPreviewSize.width, mPreviewSize.height)
                    mSurfaceTexture1?.setDefaultBufferSize(mPreviewSize.width, mPreviewSize.height)
                    val surface = Surface(mSurfaceTexture)
                    val surface1 = Surface(mSurfaceTexture1)
                    mPreviewRequestBuilder?.run {
                        addTarget(mImageReader!!.surface)
                        addTarget(surface)
                        addTarget(surface1)
                    }
                    surfaceList = listOf(surface, surface1, mImageReader!!.surface)
                } else {
//                    mPreviewRequestBuilder.addTarget(mImageReader.getSurface());
                }
                val fpsRanges: Array<Range<Int>> =
                    mCameraCharacteristics!!.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES)!!
                Log.d(TAG, "##### fpsRange: " + Arrays.toString(fpsRanges))

//                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, fpsRanges[3]);
                mCameraDevice?.createCaptureSession(
                    surfaceList!!,
                    object : CameraCaptureSession.StateCallback() {
                        override fun onConfigured(session: CameraCaptureSession) {
                            if (mCameraDevice == null) return

                            //二代关闭自动对焦  : CaptureRequest.CONTROL_AF_MODE_OFF
                            mPreviewRequestBuilder?.set(
                                CaptureRequest.CONTROL_AF_MODE,
                                CaptureRequest.CONTROL_AF_MODE_OFF
                            )
                            mCaptureSession = session
                            try {
                                if (mCaptureSession != null) mCaptureSession?.setRepeatingRequest(
                                    mPreviewRequestBuilder!!.build(),
                                    null,
                                    null
                                )
                            } catch (e: CameraAccessException) {
                                e.printStackTrace()
                            } catch (e: IllegalArgumentException) {
                                e.printStackTrace()
                            } catch (e: IllegalStateException) {
                                e.printStackTrace()
                            } catch (e: NullPointerException) {
                                e.printStackTrace()
                            }
                        }

                        override fun onConfigureFailed(session: CameraCaptureSession) {
                            Log.e(TAG, "Failed to configure capture session")
                        }

                        override fun onClosed(session: CameraCaptureSession) {
                            if (mCaptureSession != null && mCaptureSession == session) {
                                mCaptureSession = null
                            }
                        }
                    },
                    mCameraHandler
                )
            } catch (e: CameraAccessException) {
//            e.printStackTrace();
                Log.e(TAG, e.message!!)
            } catch (e: IllegalStateException) {
                stopPreview()
                startPreview()
            } catch (e: UnsupportedOperationException) {
//            e.printStackTrace();
                Log.e(TAG, e.message!!)
            }
        }
    }

    //    private byte[] convertYUV420888ToNv21(final Image image) {
//        if(image == null) return null;
//
//        Image.Plane[] planes = image.getPlanes();
//        if(planes.length == 0) return mBuffer;
//
//        ByteBuffer bufY = planes[0].getBuffer();
//        int size = image.getWidth() * image.getHeight();
//        int len = size * 3 / 2;
//        if(mBuffer == null || mBuffer.length != len) {
//            mBuffer = new byte[len];
//        }
//        bufY.get(mBuffer, 0, size);
//        ByteBuffer bufVU = planes[2].getBuffer();
//        bufVU.get(mBuffer, size, bufVU.remaining());
//        return mBuffer;
//    }

    fun getPreviewSize(): Size? {
        return mPreviewSize
    }

    fun setPreviewSize(previewSize: Size) {
        mPreviewSize = previewSize
    }

    fun getFrameCallback(): FrameCallback? {
        return mFrameCallback
    }

    fun setFrameCallback(frameCallback: FrameCallback) {
        mFrameCallback = frameCallback
    }

    fun setSurfaceTexture(surfaceTexture: SurfaceTexture) {
        mSurfaceTexture = surfaceTexture
    }

    fun setSurfaceTexture1(surfaceTexture: SurfaceTexture) {
        mSurfaceTexture1 = surfaceTexture
    }
}

interface FrameCallback {
    fun onFrame(data: Image)
}

class CompareSizesByArea : Comparator<Size> {

    override fun compare(o1: Size, o2: Size): Int {
        return java.lang.Long.signum((o1.width * o1.height - o2.width * o2.height).toLong())
    }
}