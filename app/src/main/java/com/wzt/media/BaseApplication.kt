package com.wzt.media

import android.app.Application
import com.wzt.media.util.AppEnv

class BaseApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        AppEnv.init(this, this.packageName)
    }
}