package com.wzt.media.renderer

import android.content.Context
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import android.opengl.Matrix.*
import com.wzt.media.R
import com.wzt.media.objects.Mallet
import com.wzt.media.objects.Table
import com.wzt.media.programs.ColorShaderProgram
import com.wzt.media.programs.TextureShaderProgram
import com.wzt.media.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


/**
 *
 * Created by wangzhitao on 2021/03/30
 *
 **/
class AirHockeyRendererTexture(private val context: Context): GLSurfaceView.Renderer {

    private val projectionMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)

    private var table: Table? = null
    private var mallet: Mallet? = null

    private var textureProgram: TextureShaderProgram? = null
    private var colorProgram: ColorShaderProgram? = null

    private var texture = 0

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f)

        table = Table()
        mallet = Mallet()

        textureProgram = TextureShaderProgram(context)
        colorProgram = ColorShaderProgram(context)

        texture = loadTexture(context, R.drawable.air_hockey_surface)
    }

    override fun onSurfaceChanged(glUnused: GL10?, width: Int, height: Int) {
        // Set the OpenGL viewport to fill the entire surface.
        // Set the OpenGL viewport to fill the entire surface.
        glViewport(0, 0, width, height)

        perspectiveM(
            projectionMatrix, 45f, width.toFloat()
                    / height.toFloat(), 1f, 10f
        )

        setIdentityM(modelMatrix, 0)
        translateM(modelMatrix, 0, 0f, 0f, -2.5f)
        rotateM(modelMatrix, 0, -60f, 1f, 0f, 0f)

        val temp = FloatArray(16)
        multiplyMM(temp, 0, projectionMatrix, 0, modelMatrix, 0)
        System.arraycopy(temp, 0, projectionMatrix, 0, temp.size)
    }

    override fun onDrawFrame(p0: GL10?) {
        // Clear the rendering surface.
        glClear(GL_COLOR_BUFFER_BIT);

        // Draw the table.
        textureProgram?.let {
            it.useProgram()
            it.setUniforms(projectionMatrix, texture)
            table?.bindData(it)
            table?.draw()
        }

        // Draw the mallets.
        colorProgram?.let {
            it.useProgram()
            it.setUniforms(projectionMatrix)
            mallet?.bindData(it)
            mallet?.draw()
        }
    }
}