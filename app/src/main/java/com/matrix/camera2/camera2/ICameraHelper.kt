package com.matrix.camera2.camera2

import java.io.File

/**
 * 照相机需要的基本功能
 */
interface ICameraHelper {

    /**
     * 打开预览
     */
    fun openPreview()

    /**
     * 关闭预览
     */
    fun closePreview()

    /**
     * 拍照
     * @param file 照片保存的地方
     */
    fun takePhoto(file: File)

    /**
     * 切换前后镜头
     */
    fun switchLens()
}