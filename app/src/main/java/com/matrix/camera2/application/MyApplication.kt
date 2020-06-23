package com.matrix.camera2.application

import android.app.Application
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger

/**
 *    author : xxd
 *    date   : 2020/6/22
 *    desc   :
 */
class MyApplication : Application() , CameraXConfig.Provider {

    override fun getCameraXConfig(): CameraXConfig {
        return Camera2Config.defaultConfig()
    }

    override fun onCreate() {
        super.onCreate()

        // 初始化Logger
        Logger.addLogAdapter(AndroidLogAdapter())
    }

}