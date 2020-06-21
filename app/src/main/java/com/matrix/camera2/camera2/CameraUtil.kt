package com.matrix.camera2.camera2

import android.util.Log
import android.util.Size
import android.view.Surface
import com.matrix.camera2.utils.LogUtil
import java.util.*
import kotlin.math.abs

/**
 * 一些camera需要的工具类
 */
class CameraUtil private constructor() {

    companion object {
        val instance: CameraUtil by lazy {
            CameraUtil()
        }
    }

    /**
     * 根据提供的屏幕方向 [displayRotation] 和相机方向 [sensorOrientation] 返回是否需要交换宽高
     */
    fun exchangeWidthAndHeight(displayRotation: Int, sensorOrientation: Int?): Boolean {
        var exchange = false
        when (displayRotation) {
            Surface.ROTATION_0, Surface.ROTATION_180 ->
                if (sensorOrientation == 90 || sensorOrientation == 270) {
                    exchange = true
                }
            Surface.ROTATION_90, Surface.ROTATION_270 ->
                if (sensorOrientation == 0 || sensorOrientation == 180) {
                    exchange = true
                }
            else -> Log.d("xxd", "Display rotation is invalid: $displayRotation")
        }

        Log.d("xxd", "屏幕方向  $displayRotation")
        Log.d("xxd", "相机方向  $sensorOrientation")
        return exchange
    }

    /**
     * 根据传入的数据获取最适合的size
     */
    fun getBestSize(
        targetWidth: Int,  //需要的width
        targetHeight: Int, // 需要的height
        sizeList: List<Size>  // 照相机支持的size集合
    ): Size {

        // 适合target的size集合
        var bigSizeList = mutableListOf<Size>()
        var smallSizeList = mutableListOf<Size>()

        // 合适的范围是什么？宽高比保留1位小数，与目标宽高比相差0.2以内即可，可以自己扩充适合的范围
        val targetRatio = getRatioDecimal1(targetWidth, targetHeight)
        for (size in sizeList) {
            val fl = targetRatio - getRatioDecimal1(size.width, size.height)
            if (abs(fl) <= 0.2) {
                // TODO 这里打印找到的适合宽高比
                LogUtil.d("找到的合适的大小width=${size.width} height=${size.height}")
                if (size.width >= targetWidth && targetHeight >= targetHeight)
                    bigSizeList.add(size)
                else
                    smallSizeList.add(size)
            }
        }

        var bestSize: Size =
            when {
                bigSizeList.size > 0 -> Collections.min(bigSizeList) { o1, o2 -> o1.width - o2.width }
                smallSizeList.size > 0 -> Collections.max(bigSizeList) { o1, o2 -> o1.width - o2.width }
                else -> sizeList[0]
            }

        LogUtil.d("最终的size width=${bestSize.width} height=${bestSize.height}")
        return bestSize
    }

    /**
     * 获取保留 1 位小数的宽高比数据
     */
    private fun getRatioDecimal1(width: Int, height: Int): Float {
        return width.toFloat() / height.toFloat()
    }
}