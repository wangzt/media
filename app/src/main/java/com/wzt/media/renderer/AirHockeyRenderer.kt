package com.wzt.media.renderer

import android.content.Context
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import com.wzt.media.BuildConfig
import com.wzt.media.R
import com.wzt.media.util.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


/**
 *
 * Created by wangzhitao on 2021/03/25
 *
 **/
class AirHockeyRenderer(private val context: Context): GLSurfaceView.Renderer {

    companion object {
        private const val U_COLOR = "u_Color"
        private const val A_POSITION = "a_Position"
        private const val POSITION_COMPONENT_COUNT = 2
        private const val BYTES_PER_FLOAT = 4
    }

    private var program: Int = 0
    private var uColorLocation: Int = 0
    private var aPositionLocation: Int = 0

    private val vertexData: FloatBuffer

    init {
        val tableVerticesWithTriangles = floatArrayOf( // Triangle 1
            -0.5f, -0.5f,
            0.5f, 0.5f,
            -0.5f, 0.5f,  // Triangle 2
            -0.5f, -0.5f,
            0.5f, -0.5f,
            0.5f, 0.5f,  // Line 1
            -0.5f, 0f,
            0.5f, 0f,  // Mallets
            0f, -0.25f,
            0f, 0.25f
        )

        vertexData = ByteBuffer
            .allocateDirect(tableVerticesWithTriangles.size * BYTES_PER_FLOAT)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()

        vertexData.put(tableVerticesWithTriangles)
    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        /*
		// Set the background clear color to red. The first component is red,
		// the second is green, the third is blue, and the last component is
		// alpha, which we don't use in this lesson.
		glClearColor(1.0f, 0.0f, 0.0f, 0.0f);
         */

        /*
		// Set the background clear color to red. The first component is red,
		// the second is green, the third is blue, and the last component is
		// alpha, which we don't use in this lesson.
		glClearColor(1.0f, 0.0f, 0.0f, 0.0f);
         */
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f)

        val vertexShaderSource: String = readTextFileFromResource(context, R.raw.simple_vertex_shader)
        val fragmentShaderSource: String = readTextFileFromResource(context, R.raw.simple_fragment_shader)

        val vertexShader: Int = compileVertexShader(vertexShaderSource)
        val fragmentShader: Int = compileFragmentShader(fragmentShaderSource)

        program = linkProgram(vertexShader, fragmentShader)

        if (BuildConfig.DEBUG) {
            validateProgram(program)
        }

        glUseProgram(program)

        uColorLocation = glGetUniformLocation(program, U_COLOR)

        aPositionLocation = glGetAttribLocation(program, A_POSITION)

        // Bind our data, specified by the variable vertexData, to the vertex
        // attribute at location A_POSITION_LOCATION.

        // Bind our data, specified by the variable vertexData, to the vertex
        // attribute at location A_POSITION_LOCATION.
        vertexData.position(0)
        glVertexAttribPointer(
            aPositionLocation, POSITION_COMPONENT_COUNT, GL_FLOAT,
            false, 0, vertexData
        )

        glEnableVertexAttribArray(aPositionLocation)
    }

    override fun onSurfaceChanged(glUnused: GL10?, width: Int, height: Int) {
        // Set the OpenGL viewport to fill the entire surface.
        glViewport(0, 0, width, height);
    }

    override fun onDrawFrame(p0: GL10?) {
        // Clear the rendering surface.
        glClear(GL_COLOR_BUFFER_BIT);

        // Draw the table.
        glUniform4f(uColorLocation, 1.0f, 1.0f, 1.0f, 1.0f);
        glDrawArrays(GL_TRIANGLES, 0, 6);

        // Draw the center dividing line.
        glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
        glDrawArrays(GL_LINES, 6, 2);

        // Draw the first mallet blue.
        glUniform4f(uColorLocation, 0.0f, 0.0f, 1.0f, 1.0f);
        glDrawArrays(GL_POINTS, 8, 1);

        // Draw the second mallet red.
        glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
        glDrawArrays(GL_POINTS, 9, 1);
    }
}