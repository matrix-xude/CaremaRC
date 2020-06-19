package com.matrix.camera2.utils

/**
 *    author : xxd
 *    date   : 2020/6/19
 *    desc   :
 */
object TimestampUtil {

    private var lastTimeMillis: Long = 0L

    fun logTimestamp() {
        val currentTimeMillis = System.currentTimeMillis()
        LogUtil.d("当前时间戳$currentTimeMillis , 与上次时间相差${currentTimeMillis - lastTimeMillis}")
        lastTimeMillis = currentTimeMillis
    }
}