package com.wzt.media.util

import android.content.Context

object AppEnv {
    private var mApplicationContext: Context? = null
    private var PACKAGE_NAME: String? = null

    fun init(context: Context, packageName: String) {
        mApplicationContext = context
        PACKAGE_NAME = packageName
    }

    fun getContext():Context {
        return mApplicationContext!!
    }
}