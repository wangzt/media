package com.wzt.media.activity

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import com.wzt.media.renderer.AirHockeyRenderer2
import com.wzt.media.renderer.AirHockeyRenderer3


/**
 *
 * Created by wangzhitao on 2021/03/30
 *
 **/
class AirHockeyActivity3: Activity() {

    private var rendererSet = false
    private var glSurfaceView: GLSurfaceView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        glSurfaceView = GLSurfaceView(this)
        val activityManager:ActivityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        val configurationInfo = activityManager.deviceConfigurationInfo

        val supportsEs2 =
            configurationInfo.reqGlEsVersion >= 0x20000
                    || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
                    && (Build.FINGERPRINT.startsWith("generic")
                    || Build.FINGERPRINT.startsWith("unknown")
                    || Build.MODEL.contains("google_sdk")
                    || Build.MODEL.contains("Emulator")
                    || Build.MODEL.contains("Android SDK built for x86")))

        if (supportsEs2) {
            // Request an OpenGL ES 2.0 compatible context.
            glSurfaceView?.setEGLContextClientVersion(2);

            glSurfaceView?.setEGLConfigChooser(8 , 8, 8, 8, 16, 0);

            // Assign our renderer.
            glSurfaceView?.setRenderer(AirHockeyRenderer3(this))
            rendererSet = true;
        } else {
            /*
             * This is where you could create an OpenGL ES 1.x compatible
             * renderer if you wanted to support both ES 1 and ES 2. Since
             * we're not doing anything, the app will crash if the device
             * doesn't support OpenGL ES 2.0. If we publish on the market, we
             * should also add the following to AndroidManifest.xml:
             *
             * <uses-feature android:glEsVersion="0x00020000"
             * android:required="true" />
             *
             * This hides our app from those devices which don't support OpenGL
             * ES 2.0.
             */
            Toast.makeText(
                this, "This device does not support OpenGL ES 2.0.",
                Toast.LENGTH_LONG
            ).show();
            return
        }

        setContentView(glSurfaceView)
    }

    override fun onPause() {
        super.onPause()
        if (rendererSet) {
            glSurfaceView!!.onPause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (rendererSet) {
            glSurfaceView!!.onResume()
        }
    }
}