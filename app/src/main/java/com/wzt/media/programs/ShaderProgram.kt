package com.wzt.media.programs

import android.content.Context
import android.opengl.GLES20.*
import com.wzt.media.R
import com.wzt.media.util.buildProgram
import com.wzt.media.util.readTextFileFromResource


/**
 *
 * Created by wangzhitao on 2021/04/07
 *
 **/
open class ShaderProgram(
    context: Context,
    vertexShaderResourceId: Int,
    fragmentShaderResourceId: Int
) {

    // Uniform constants
    protected val U_MATRIX = "u_Matrix"
    protected val U_TEXTURE_UNIT = "u_TextureUnit"

    // Attribute constants
    protected val A_POSITION = "a_Position"
    protected val A_COLOR = "a_Color"
    protected val A_TEXTURE_COORDINATES = "a_TextureCoordinates"

    protected val program = buildProgram(
        readTextFileFromResource(
            context, vertexShaderResourceId
        ),
        readTextFileFromResource(
            context, fragmentShaderResourceId
        )
    )

    open fun useProgram() {
        // Set the current OpenGL shader program to this program.
        glUseProgram(program)
    }
}

class ColorShaderProgram(context: Context): ShaderProgram(
    context, R.raw.simple_vertex_shader_texture,
    R.raw.simple_fragment_shader_texture
) {

    // Retrieve uniform locations for the shader program.
    private val uMatrixLocation = glGetUniformLocation(program, U_MATRIX)

    // Retrieve attribute locations for the shader program.
    private val aPositionLocation = glGetAttribLocation(program, A_COLOR)
    private val aColorLocation = glGetAttribLocation(program, A_COLOR)

    fun setUniforms(matrix: FloatArray?) {
        // Pass the matrix into the shader program.
        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)
    }

    fun getPositionAttributeLocation(): Int {
        return aPositionLocation
    }

    fun getColorAttributeLocation(): Int {
        return aColorLocation
    }
}

class TextureShaderProgram(context: Context): ShaderProgram(
    context, R.raw.texture_vertext_shader,
    R.raw.texture_fragment_shader
) {

    // Retrieve uniform locations for the shader program.
    private val uMatrixLocation = glGetUniformLocation(program, U_MATRIX)
    private val uTextureUnitLocation = glGetUniformLocation(program, U_TEXTURE_UNIT)

    // Retrieve attribute locations for the shader program
    private val aPositionLocation = glGetAttribLocation(program, A_POSITION)
    private val aTextureCoordinatesLocation =
        glGetAttribLocation(program, A_TEXTURE_COORDINATES)


    fun setUniforms(matrix: FloatArray?, textureId: Int) {
        // Pass the matrix into the shader program.
        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)

        // Set the active texture unit to texture unit 0.
        glActiveTexture(GL_TEXTURE0)

        // Bind the texture to this unit.
        glBindTexture(GL_TEXTURE_2D, textureId)

        // Tell the texture uniform sampler to use this texture in the shader by
        // telling it to read from texture unit 0.
        glUniform1i(uTextureUnitLocation, 0)
    }

    fun getPositionAttributeLocation(): Int {
        return aPositionLocation
    }

    fun getTextureCoordinatesAttributeLocation(): Int {
        return aTextureCoordinatesLocation
    }
}