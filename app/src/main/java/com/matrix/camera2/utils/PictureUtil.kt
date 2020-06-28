package com.matrix.camera2.utils

import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import java.io.IOException

/**
 *    author : xxd
 *    date   : 2020/6/28
 *    desc   :
 */
object PictureUtil {

    /**
     * 获取图片旋转角度
     *
     * @param filePath 文件路径
     * @return 旋转角度
     */
    fun getRotateDegree(filePath: String): Int {
        var degree = 0
        try {
            val exifInterface = ExifInterface(filePath)
            val orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL)
            degree = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return degree
    }

    /**
     * 旋转图片
     *
     * @param src     源图片
     * @param degrees 旋转角度
     * @param px      旋转点横坐标
     * @param py      旋转点纵坐标
     * @return 旋转后的图片
     */
    @JvmStatic
    fun rotate(src: Bitmap, degrees: Int, px: Float, py: Float, recycle: Boolean = false): Bitmap? {
        if (isEmptyBitmap(src)) {
            return null
        }
        if (degrees == 0) {
            return src
        }
        val matrix = Matrix()
        matrix.setRotate(degrees.toFloat(), px, py)
        val ret = Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
        if (recycle && !src.isRecycled) {
            src.recycle()
        }
        return ret
    }

    /**
     * 判断bitmap对象是否为空
     *
     * @param src 源图片
     * @return `true`: 是<br></br>`false`: 否
     */
    fun isEmptyBitmap(src: Bitmap?): Boolean {
        return src == null || src.width == 0 || src.height == 0
    }
}