package com.wzt.media

import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 *
 * Created by wangzhitao on 2021/02/09
 *
 **/
class OpenGLRenderer : GLSurfaceView.Renderer {

    val tableVerticesWithTriangles = floatArrayOf(
        0f,0f,
        9f,14f,
        0f,14f,

        0f,0f,
        9f,0f,
        9f,14f,

        0f,7f,
        9f,7f,

        4.5f,2f,
        4.5f,12f
    )
    private lateinit var vertexData: FloatBuffer

    companion object {
        private const val BYTES_PER_FLOAT = 4
    }

    init {
        vertexData = ByteBuffer
            .allocateDirect(tableVerticesWithTriangles.size * BYTES_PER_FLOAT)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        vertexData.put(tableVerticesWithTriangles)
    }
    override fun onDrawFrame(p0: GL10?) {
        glClear(GL_COLOR_BUFFER_BIT)
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)
    }

    override fun onSurfaceCreated(p0: GL10?, config: EGLConfig?) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
    }
}