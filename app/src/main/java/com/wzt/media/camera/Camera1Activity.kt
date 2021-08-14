package com.wzt.media.camera

import android.R.attr
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.hardware.Camera
import android.os.Bundle
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.widget.Toast
import com.wzt.media.databinding.ActivityCamera1Binding
import com.wzt.media.thread.JobWorker
import com.wzt.media.util.FileUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class Camera1Activity:Activity(),Camera.PreviewCallback {

    companion object {
        private const val TAG = "Camera1Activity"
    }

    private lateinit var binding: ActivityCamera1Binding

    private var mSurfaceHolder: SurfaceHolder? = null

    private var mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT
    private var mCamera: Camera? = null
    private var mParameters: Camera.Parameters? = null

    private var mWidth = 0
    private var mHeight = 0

    private var mOrientation = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCamera1Binding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
    }

    private fun initViews() {
        binding.camera1Capture.setOnClickListener {
            takePicture()
        }

        mSurfaceHolder = binding.camera1SurfaceView.holder

        mSurfaceHolder?.addCallback(object: SurfaceHolder.Callback {
            override fun surfaceCreated(p0: SurfaceHolder) {
                openCamera()
                startPreview()
            }

            override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
            }

            override fun surfaceDestroyed(p0: SurfaceHolder) {
                releaseCamera()
            }

        })

        val dm = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(dm)
        mWidth = dm.widthPixels
        mHeight = dm.heightPixels
    }

    private fun openCamera() {
        if (isSupport(mCameraId)) {
            try {
                mCamera = Camera.open(mCameraId)
                initParameters(mCamera)

                //preview
                if (null != mCamera) {
                    mCamera!!.setPreviewCallback(this)
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun initParameters(camera: Camera?) {
        mParameters = camera?.parameters
        mParameters?.run {
            previewFormat = ImageFormat.NV21 //default
            supportedPreviewFormats
            supportedPictureFormats
            if (isSupportFocus(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)
            } else if (isSupportFocus(Camera.Parameters.FOCUS_MODE_AUTO)) {
                setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO)
            }
        }

        setPreviewSize()
        setPictureSize()
        camera?.parameters = mParameters
    }

    private fun startPreview() {
        try {
            mCamera!!.setPreviewDisplay(mSurfaceHolder)
            setCameraDisplayOrientation()
            mCamera!!.startPreview()
            startFaceDetect()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun startFaceDetect() {
        mCamera!!.startFaceDetection()
        mCamera!!.setFaceDetectionListener { faces, camera ->
            Log.d(TAG,"##### face length: " + faces.size
            )
        }
    }

    private fun setPreviewSize() {
        val supportSizes = mParameters!!.supportedPreviewSizes
        var biggestSize: Camera.Size? = null
        var fitSize: Camera.Size? = null
        var targetSize: Camera.Size? = null
        var targetSiz2: Camera.Size? = null
        if (null != supportSizes) {
            for (i in supportSizes.indices) {
                val size = supportSizes[i]
                Log.d(
                    TAG,
                    "###### SupportedPreviewSizes: width=" + size.width + ", height=" + size.height
                )
                if (biggestSize == null ||
                    size.width >= biggestSize.width && size.height >= biggestSize.height
                ) {
                    biggestSize = size
                }
                if (size.width == mWidth
                    && size.height == mHeight
                ) {
                    fitSize = size
                    //如果任一宽或者高等于所支持的尺寸
                } else if (size.width == mWidth
                    || size.height == mHeight
                ) {
                    if (targetSize == null) {
                        targetSize = size
                        //如果上面条件都不成立 如果任一宽高小于所支持的尺寸
                    } else if (size.width < mWidth
                        || size.height < mHeight
                    ) {
                        targetSiz2 = size
                    }
                }
            }
            if (fitSize == null) {
                fitSize = targetSize
            }
            if (fitSize == null) {
                fitSize = targetSiz2
            }
            if (fitSize == null) {
                fitSize = biggestSize
            }
            Log.d(
                TAG,
                "##### fitSize width: " + fitSize!!.width + ", height: " + fitSize.height
            )
            mParameters!!.setPreviewSize(fitSize.width, fitSize.height)
        }
    }

    private fun setCameraDisplayOrientation() {
        val cameraInfo = Camera.CameraInfo()
        Camera.getCameraInfo(mCameraId, cameraInfo)
        val rotation = windowManager.defaultDisplay.rotation //自然方向
        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }
        var result: Int
        //cameraInfo.orientation 图像传感方向
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (cameraInfo.orientation + degrees) % 360
            result = (360 - result) % 360
        } else {
            result = (cameraInfo.orientation - degrees + 360) % 360
        }
        Log.d(
            TAG, "##### setCameraDisplayOrientation rotation: " + rotation
                    + ", cameraInfo.orientation: " + cameraInfo.orientation + ", result: " + result
        )
        mOrientation = result
        //相机预览方向
        mCamera!!.setDisplayOrientation(result)
    }

    private fun isSupport(backOrFront: Int): Boolean {
        val cameraInfo = Camera.CameraInfo()
        for (i in 0 until Camera.getNumberOfCameras()) {
            Camera.getCameraInfo(i, cameraInfo)
            if (cameraInfo.facing == backOrFront) {
                return true
            }
        }
        return false
    }

    private fun isSupportFocus(focusMode: String): Boolean {
        for (mode in mParameters!!.supportedFocusModes) {
            if (focusMode == mode) {
                return true
            }
        }
        return false
    }

    private fun takePicture() {
        mCamera?.takePicture(object: Camera.ShutterCallback {
            override fun onShutter() {

            }

        }, Camera.PictureCallback { data, camera ->
            // base data
        },
            Camera.PictureCallback { data, camera ->
                mCamera?.startPreview()
                JobWorker.execute {
                    savePhoto(data)
                }
            })
    }

    private fun savePhoto(data: ByteArray) {
        val f = File(FileUtils.getCaptureFilePath())
        if (f != null && !f.exists()) {
            try {
                f.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(f)
            fos.write(data)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        rotateImageView(mCameraId, mOrientation, f.path)
    }

    private fun rotateImageView(cameraId: Int, orientation: Int, path: String) {
        Log.d(TAG   , "##### save path: $path")
        val bitmap = BitmapFactory.decodeFile(path)
        var resizeBitmap: Bitmap? = null
        when (cameraId) {
            Camera.CameraInfo.CAMERA_FACING_BACK -> {
                val matrix = Matrix()
                if (mOrientation == 90) {
                    matrix.postRotate(90f)
                }
                resizeBitmap = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.width, bitmap.height,
                    matrix, true
                )
            }
            Camera.CameraInfo.CAMERA_FACING_FRONT -> {
                val m = Matrix()
                m.postScale(-1f, 1f)
                resizeBitmap = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.width, bitmap.height,
                    m, true
                )
            }
        }
        val file = File(path)
        try {
            val fos = FileOutputStream(file)
            resizeBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
            resizeBitmap.recycle()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        Looper.prepare()
        Toast.makeText(this@Camera1Activity, "save file path: $path", Toast.LENGTH_LONG).show()
        Looper.loop()
    }

    /**
     * save picture size
     */
    private fun setPictureSize() {
        val mPictureSizes = mParameters!!.supportedPictureSizes
        val previewSize = mParameters!!.previewSize
        var biggestSize: Camera.Size? = null
        var fitSize: Camera.Size? = null // 优先选预览界面的尺寸
        Log.d(
            TAG,
            "##### preview size: " + previewSize!!.width + ", height:" + previewSize.height
        )
        var scaleSize = 0f
        if (null != previewSize) {
            scaleSize = previewSize.width / previewSize.height.toFloat()
        }
        for (i in mPictureSizes.indices) {
            val picture = mPictureSizes[i]
            Log.d(
                TAG, "###### SupportedPictureSizes: width=" + picture.width + ", height="
                        + picture.height
            )
            if (null == biggestSize) {
                biggestSize = picture
            } else if (picture.width > biggestSize.width && picture.height > biggestSize.height) {
                biggestSize = picture
            }
            if (scaleSize > 0 && picture.width > previewSize.width && picture.height > previewSize.height) {
                val currentScale = picture.width / picture.height.toFloat()
                if (scaleSize == currentScale) {
                    if (null == fitSize) {
                        fitSize = picture
                    } else if (picture.width > fitSize.width && picture.height > fitSize.height) {
                        fitSize = picture
                    }
                }
            }
        }
        if (null == fitSize) {
            fitSize = biggestSize
        }
        Log.d(TAG, "##### fit size: " + fitSize!!.width + ", height:" + fitSize.height)
        mParameters!!.setPictureSize(fitSize.width, fitSize.height)
    }

    private fun releaseCamera() {
        mCamera?.run {
            stopPreview()
            stopFaceDetection()
            setPreviewCallback(null)
            release()
        }
        mCamera = null
    }


    override fun onDestroy() {
        super.onDestroy()
        releaseCamera()
    }

    override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {

    }
}