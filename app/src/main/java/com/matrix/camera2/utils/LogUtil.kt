package com.matrix.camera2.utils

import android.util.Log

/**
 *    author : xxd
 *    date   : 2020/6/19
 *    desc   :
 */
object LogUtil {

    private const val TAG = "xxd"

    fun d(content: String?) {
        Log.d(TAG, content ?: "打印了个六娃...")
    }
}