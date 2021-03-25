package com.wzt.media.util

import android.content.Context
import android.content.res.Resources
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.RuntimeException
import java.lang.StringBuilder

/**
 *
 * Created by wangzhitao on 2021/03/24
 *
 **/

fun readTextFileFromResource(context: Context, resId: Int): String {
    val body = StringBuilder()

    try {
        val inputStream = context.resources.openRawResource(resId)
        val inputReader = InputStreamReader(inputStream)
        val bufferReader = BufferedReader(inputReader)

        var nextLine: String? = null
        nextLine = bufferReader.readLine()
        while (nextLine != null) {
            body.append(nextLine)
            body.append('\n')
            nextLine = bufferReader.readLine()
        }
    } catch (e: IOException) {
        throw RuntimeException("Could not open resource: $resId", e)
    } catch (nfe: Resources.NotFoundException) {
        throw RuntimeException("Resource not found: $resId", nfe)
    }

    return body.toString()
}
