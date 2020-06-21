package com.matrix.camera2.camera2

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.TextureView
import com.matrix.camera2.utils.FileUtil
import com.matrix.camera2.utils.ToastUtil
import java.io.File
import java.lang.StringBuilder

/**
 * 基于camera2的实现类，通过builder构建
 */
class CameraHelperImpl private constructor(builder: Builder) : ICameraHelper {

    companion object {
        const val SAVE_WIDTH = 720
        const val SAVE_HEIGHT = 1080
    }

    /*************************与摄像头无关的参数，默认只需要一份*********************/
    // builder构建需要的参数
    val activity: Activity
    val cameraManager: CameraManager
    var textureView: TextureView? = null
    var lensFacing = CameraCharacteristics.LENS_FACING_BACK

    // 线程相关的，拍照需要异步
    val handlerThread = HandlerThread("cameraThread")
    val cameraHandler: Handler


    /*************************与摄像头相关的参数，每次切换都需要重新初始化*********************/
    // 摄像头id
    var cameraId = ""
    // 当前摄像头的特征数据
    var characteristics: CameraCharacteristics? = null
    var sensorOrientation: Int? = 0

    // 下面3个是需要释放的
    var device: CameraDevice? = null
    var captureSession: CameraCaptureSession? = null
    var imageReader: ImageReader? = null

    // 摄像头保存的尺寸
    var previewSize = Size(0, 0)
    var saveSize = Size(SAVE_WIDTH, SAVE_HEIGHT)


    init {
        activity = builder.activity
        textureView = builder.textureView
        lensFacing = builder.lensFacing

        cameraManager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        handlerThread.start()
        cameraHandler = Handler(handlerThread.looper)
    }

    override fun openPreview() {
        releaseCamera()
        if (!initLens())
            return
        if (!initPreView())
            return
//        openCamera()
    }

    override fun closePreview() {
        releaseCamera()
    }

    override fun takePhoto(file: File) {
        takePic()
    }

    override fun switchLens() {
        lensFacing =
            if (lensFacing == CameraCharacteristics.LENS_FACING_BACK)
                CameraCharacteristics.LENS_FACING_FRONT
            else CameraCharacteristics.LENS_FACING_BACK

        releaseCamera()
        if (!initLens())
            return
        if (!initPreView())
            return
//        openCamera()
    }

    /**
     * 所有参数准备就绪，真正开启相机
     */
    @SuppressLint("MissingPermission")
    private fun openCamera() {
        cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                device = camera
                createSession(camera)
            }

            override fun onDisconnected(camera: CameraDevice) {
                releaseCamera()
            }

            override fun onError(camera: CameraDevice, error: Int) {
            }
        }, cameraHandler)
    }

    private fun createSession(device: CameraDevice) {

        val surface = Surface(textureView!!.surfaceTexture)

        val captureRequestBuilder = device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)

        captureRequestBuilder.addTarget(surface)  // 将CaptureRequest的构建器与Surface对象绑定在一起
        captureRequestBuilder.set(
            CaptureRequest.CONTROL_AE_MODE,
            CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
        )      // 闪光灯
        captureRequestBuilder.set(
            CaptureRequest.CONTROL_AF_MODE,
            CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
        ) // 自动对焦

        device.createCaptureSession(
            arrayListOf(surface, imageReader?.surface),
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigureFailed(session: CameraCaptureSession) {
                }

                override fun onConfigured(session: CameraCaptureSession) {
                    captureSession = session
                    session.setRepeatingRequest(
                        captureRequestBuilder.build(),
                        captureCallBack,
                        cameraHandler
                    )
                }
            },
            cameraHandler
        )
    }

    private val captureCallBack = object : CameraCaptureSession.CaptureCallback() {

        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult
        ) {
            super.onCaptureCompleted(session, request, result)
//            ToastUtil.showToast(activity, "预览request++")
        }

        override fun onCaptureFailed(
            session: CameraCaptureSession,
            request: CaptureRequest,
            failure: CaptureFailure
        ) {
            super.onCaptureFailed(session, request, failure)
            ToastUtil.showToast(activity, "获取预览request失败")
        }
    }


    /**
     * 初始化预览
     */
    private fun initPreView(): Boolean {
        textureView ?: return false

        textureView!!.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture?,
                width: Int,
                height: Int
            ) {

            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {

            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
                releaseCamera()
                return true
            }

            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture?,
                width: Int,
                height: Int
            ) {
                initConfigurationMap(width, height)
            }
        }

        if (textureView!!.isAvailable) {
            initConfigurationMap(textureView!!.width, textureView!!.height)
        }
        return true
    }

    private fun initConfigurationMap(targetWidth: Int, targetHeight: Int) {

        // 获取到相机的旋转角度
        sensorOrientation = characteristics!!.get(CameraCharacteristics.SENSOR_ORIENTATION)

        // 获取到预览支持的数据、照相支持的数据
        val configurationMap =
            characteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val previewSizeArray = configurationMap?.getOutputSizes(SurfaceTexture::class.java)
        val saveSizeArray = configurationMap?.getOutputSizes(ImageFormat.JPEG)

        val rotation = activity.windowManager.defaultDisplay.rotation
        val exchange = CameraUtil.instance.exchangeWidthAndHeight(rotation, sensorOrientation)
        previewSize =
            CameraUtil.instance.getBestSize(
                if (exchange) targetHeight else targetWidth,
                if (exchange) targetWidth else targetHeight,
                previewSizeArray!!.toList()
            )
        saveSize =
            CameraUtil.instance.getBestSize(
                if (exchange) SAVE_HEIGHT else SAVE_WIDTH,
                if (exchange) SAVE_WIDTH else SAVE_HEIGHT,
                saveSizeArray!!.toList()
            )

//        previewSize = Size(1,1)
//        saveSize = Size(1500,1500)

        textureView!!.surfaceTexture.setDefaultBufferSize(previewSize.width, previewSize.height)

        imageReader = ImageReader.newInstance(saveSize.width, saveSize.height, ImageFormat.JPEG, 1)
        imageReader?.setOnImageAvailableListener(onImageAvailableListener, cameraHandler)

        openCamera()

    }

    private val onImageAvailableListener = ImageReader.OnImageAvailableListener {

        val image = it.acquireNextImage()
        val byteBuffer = image.planes[0].buffer
        val byteArray = ByteArray(byteBuffer.remaining())
        byteBuffer.get(byteArray)
        it.close()

        val rootString = Environment.getExternalStorageDirectory().toString()
        val fileString = StringBuilder().apply {
            append(rootString)
            append(File.separator)
            append("aaa")
            append(File.separator)
            append("temp.jpg")
        }.toString()
        FileUtil.copyFile(byteArray, File(fileString))
        activity.runOnUiThread {
            ToastUtil.showToast(activity,"保存照片成功")
        }

//        BitmapUtils.savePic(byteArray, mCameraSensorOrientation == 270, { savedPath, time ->
//            activity.runOnUiThread {
//                ToastUtil.showToast(activity,"图片保存成功！ 保存路径：$savedPath 耗时：$time")
//            }
//        }, { msg ->
//            activity.runOnUiThread {
//                ToastUtil.showToast(activity,"图片保存失败！ $msg")
//            }
//        })
    }

    /**
     * 拍照
     */
    fun takePic() {
        if (device == null || !textureView!!.isAvailable) return

        sensorOrientation = characteristics!!.get(CameraCharacteristics.SENSOR_ORIENTATION)
        val rotation = activity.windowManager.defaultDisplay.rotation
        Log.d("xxd", "屏幕方向  $rotation")
        Log.d("xxd", "相机方向  $sensorOrientation")

        device?.apply {

            val captureRequestBuilder = createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureRequestBuilder.addTarget(imageReader!!.surface)

            captureRequestBuilder.set(
                CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
            ) // 自动对焦
            captureRequestBuilder.set(
                CaptureRequest.CONTROL_AE_MODE,
                CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
            )     // 闪光灯
            captureRequestBuilder.set(
                CaptureRequest.JPEG_ORIENTATION,
                sensorOrientation
            )      //根据摄像头方向对保存的照片进行旋转，使其为"自然方向"
            captureSession?.capture(captureRequestBuilder.build(), null, cameraHandler)
                ?: ToastUtil.showToast(activity, "拍照异常！")
        }
    }


    /**
     * 找到需要的镜头，得到改镜头的 characteristics
     * @return 如果找不到需要的镜头，返回false
     */
    private fun initLens(): Boolean {
        var flag = false // 初始化是否成功

        val cameraIdList = cameraManager.cameraIdList
        if (cameraIdList.isEmpty())
            return flag
        for (id in cameraIdList) {
            val characteristics = cameraManager.getCameraCharacteristics(id)
            val lens = characteristics.get(CameraCharacteristics.LENS_FACING)
            if (lens == lensFacing) { // 找到了需要的镜头
                this.cameraId = id
                this.characteristics = characteristics
                flag = true
                break
            }
        }
        return flag
    }

    /**
     * 释放相机资源
     * 需要调用的地方很多：
     * 1.手动释放
     * 2.textureView被销毁
     * 3.相机断开连接（如其它应用打开相机）
     * 4.生命周期 onStop
     */
    private fun releaseCamera() {
        imageReader?.close()
        imageReader = null

        captureSession?.close()
        captureSession = null

        device?.close()
        device = null
    }

    /**
     * 构建一个相机
     */
    class Builder(val activity: Activity) {

        // 镜头的朝向，默认后置
        var lensFacing = CameraCharacteristics.LENS_FACING_BACK
        // 预览需要的textureView，这里不推荐使用surfaceView
        var textureView: TextureView? = null


        fun build(): CameraHelperImpl {
            return CameraHelperImpl(this)
        }
    }
}