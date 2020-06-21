package com.matrix.camera2.utils

import java.io.File
import java.io.FileOutputStream

object FileUtil {

    fun copyFile(byteArray: ByteArray, file: File?) {
        if (file == null)
            return
        createOrExistsDir(file.parentFile)
        file.createNewFile()
        val fos = FileOutputStream(file, false)
        fos.use {
            it.write(byteArray)
            it.flush()
        }
    }

    fun createOrExistsDir(file: File?): Boolean {
        // 如果存在，是目录则返回true，是文件则返回false，不存在则返回是否创建成功
        return file != null && if (file.exists()) file.isDirectory else file.mkdirs()
    }
}