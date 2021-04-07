package com.wzt.media.util

import android.opengl.GLES20.*
import android.util.Log
import com.wzt.media.BuildConfig


/**
 *
 * Created by wangzhitao on 2021/03/25
 *
 **/

private const val TAG = "ShaderHelper"

/**
 * Loads and compiles a vertex shader, returning the OpenGL object ID.
 */
fun compileVertexShader(shaderCode: String?): Int {
    return compileShader(GL_VERTEX_SHADER, shaderCode!!)
}

/**
 * Loads and compiles a fragment shader, returning the OpenGL object ID.
 */
fun compileFragmentShader(shaderCode: String?): Int {
    return compileShader(GL_FRAGMENT_SHADER, shaderCode!!)
}

/**
 * Compiles a shader, returning the OpenGL object ID.
 */
private fun compileShader(type: Int, shaderCode: String): Int {

    // Create a new shader object.
    val shaderObjectId = glCreateShader(type)
    if (shaderObjectId == 0) {
        if (BuildConfig.DEBUG) {
            Log.w(TAG, "Could not create new shader.")
        }
        return 0
    }

    // Pass in the shader source.
    glShaderSource(shaderObjectId, shaderCode)

    // Compile the shader.
    glCompileShader(shaderObjectId)

    // Get the compilation status.
    val compileStatus = IntArray(1)
    glGetShaderiv(shaderObjectId, GL_COMPILE_STATUS, compileStatus, 0)
    if (BuildConfig.DEBUG) {
        // Print the shader info log to the Android log output.
        Log.v(
            TAG, """
     Results of compiling source:
     $shaderCode
     :${glGetShaderInfoLog(shaderObjectId)}
     """.trimIndent()
        )
    }

    // Verify the compile status.
    if (compileStatus[0] == 0) {
        // If it failed, delete the shader object.
        glDeleteShader(shaderObjectId)
        if (BuildConfig.DEBUG) {
            Log.w(TAG, "Compilation of shader failed.")
        }
        return 0
    }

    // Return the shader object ID.
    return shaderObjectId
}


/**
 * Links a vertex shader and a fragment shader together into an OpenGL
 * program. Returns the OpenGL program object ID, or 0 if linking failed.
 */
fun linkProgram(vertexShaderId: Int, fragmentShaderId: Int): Int {

    // Create a new program object.
    val programObjectId = glCreateProgram()
    if (programObjectId == 0) {
        if (BuildConfig.DEBUG) {
            Log.w(TAG, "Could not create new program")
        }
        return 0
    }

    // Attach the vertex shader to the program.
    glAttachShader(programObjectId, vertexShaderId)
    // Attach the fragment shader to the program.
    glAttachShader(programObjectId, fragmentShaderId)

    // Link the two shaders together into a program.
    glLinkProgram(programObjectId)

    // Get the link status.
    val linkStatus = IntArray(1)
    glGetProgramiv(programObjectId, GL_LINK_STATUS, linkStatus, 0)
    if (BuildConfig.DEBUG) {
        // Print the program info log to the Android log output.
        Log.v(
            TAG, """
     Results of linking program:
     ${glGetProgramInfoLog(programObjectId)}
     """.trimIndent()
        )
    }

    // Verify the link status.
    if (linkStatus[0] == 0) {
        // If it failed, delete the program object.
        glDeleteProgram(programObjectId)
        if (BuildConfig.DEBUG) {
            Log.w(TAG, "Linking of program failed.")
        }
        return 0
    }

    // Return the program object ID.
    return programObjectId
}

/**
 * Validates an OpenGL program. Should only be called when developing the
 * application.
 */
fun validateProgram(programObjectId: Int): Boolean {
    glValidateProgram(programObjectId)
    val validateStatus = IntArray(1)
    glGetProgramiv(programObjectId, GL_VALIDATE_STATUS, validateStatus, 0)
    Log.v(
        TAG, """
     Results of validating program: ${validateStatus[0]}
     Log:${glGetProgramInfoLog(programObjectId)}
     """.trimIndent()
    )
    return validateStatus[0] != 0
}

/**
 * Helper function that compiles the shaders, links and validates the
 * program, returning the program ID.
 */
fun buildProgram(
    vertexShaderSource: String?,
    fragmentShaderSource: String?): Int {
    val program: Int

    // Compile the shaders.
    val vertexShader = compileVertexShader(vertexShaderSource)
    val fragmentShader = compileFragmentShader(fragmentShaderSource)

    // Link them into a shader program.
    program = linkProgram(vertexShader, fragmentShader)
    if (BuildConfig.DEBUG) {
        validateProgram(program)
    }
    return program
}