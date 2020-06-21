package com.matrix.camera2.helper

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.TextureView
import androidx.core.content.ContextCompat
import com.matrix.camera2.utils.LogUtil
import com.matrix.camera2.utils.ToastUtil

/**
 *    author : xxd
 *    date   : 2020/6/19
 *    desc   : camera2拍照的帮助类
 *
 *    CameraManager： 管理手机上的所有摄像头设备，它的作用主要是获取摄像头列表和打开指定的摄像头
 *    CameraDevice： 具体的摄像头设备，它有一系列参数（预览尺寸、拍照尺寸等），可以通过CameraManager
 *      的getCameraCharacteristics()方法获取。它的作用主要是创建CameraCaptureSession和CaptureRequest
 *    CameraCaptureSession： 相机捕获会话，用于处理拍照和预览的工作（很重要）
 *    CaptureRequest： 捕获请求，定义输出缓冲区以及显示界面（TextureView或SurfaceView）等
 */
class Camera2Helper(val activity: Activity, val textureView: TextureView) {

    companion object {
        const val PREVIEW_WIDTH = 720
        const val PREVIEW_HEIGHT = 1080
        const val SAVE_WIDTH = 720
        const val SAVE_HEIGHT = 1080
    }

    private lateinit var mCameraManager: CameraManager

    private var mImageReader: ImageReader? = null
    private var mCameraDevice: CameraDevice? = null
    private var mCameraCaptureSession: CameraCaptureSession? = null

    private var mCameraId = "0"
    private lateinit var mCameraCharacteristics: CameraCharacteristics

    private var mCameraSensorOrientation = 0  // 摄像头方向

    private var mCameraDefaultFace = CameraCharacteristics.LENS_FACING_BACK // 默认的摄像头方向，需要API 21才能用
    private val mDisplayRotation = activity.windowManager.defaultDisplay.rotation // 手机默认的旋转方向

    private var canTakePicture = true // 是否可以拍照
    private var canSwitchCamera = true  // 是否可以切换摄像头

    private lateinit var mCameraHandler: Handler
    private val handlerThread = HandlerThread("CameraThread")  // 启动一个线程加载

    private var mPreviewSize = Size(PREVIEW_WIDTH, PREVIEW_HEIGHT)

    private var mSaveSize = Size(SAVE_WIDTH, SAVE_HEIGHT)

    init {
        handlerThread.start() // 第2步，启用线程后可以获得一个looper
        mCameraHandler = Handler(handlerThread.looper)

        // textureView 数据回调接口
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture?,
                width: Int,
                height: Int
            ) {

            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
                return true
            }

            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture?,
                width: Int,
                height: Int
            ) {
                initCameraInfo()
            }

        }
    }

    /**
     * 初始化camera，必须在surfaceTexture回调available中执行
     */
    private fun initCameraInfo() {
        mCameraManager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        // 摄像头处理
        val cameraIdList = mCameraManager.cameraIdList // 获取所有摄像头
        if (cameraIdList.isEmpty()) {
            ToastUtil.showToast(activity, "没有可用的相机")
        }
        for (id in cameraIdList) {
            val cameraCharacteristics = mCameraManager.getCameraCharacteristics(id)
            if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == mCameraDefaultFace) {
                mCameraId = id
                mCameraCharacteristics = cameraCharacteristics
            }
            LogUtil.d("可用的摄像头id $id")
        }

        val supportLevel =
            mCameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
        if (supportLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
            ToastUtil.showToast(activity, "相机硬件不支持新特新")
        }

        // 获取摄像头方向
        val mCameraSensorOrientation =
            mCameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)
        // 管理摄像头支持的所有输出格式和尺寸
        val configurationMap =
            mCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

        val savePicSize = configurationMap?.getOutputSizes(ImageFormat.JPEG) // 保存照片的尺寸
        val previewSize = configurationMap?.getOutputSizes(SurfaceTexture::class.java) // 预览尺寸

        val exchange = exchangeWidthAndHeight(mDisplayRotation, mCameraSensorOrientation)

        mSaveSize = getBestSize(
            if (exchange) mSaveSize.height else mSaveSize.width,
            if (exchange) mSaveSize.width else mSaveSize.height,
            if (exchange) mSaveSize.height else mSaveSize.width,
            if (exchange) mSaveSize.width else mSaveSize.height,
            savePicSize!!.toList()
        )

        mPreviewSize = getBestSize(
            if (exchange) mPreviewSize.height else mPreviewSize.width,
            if (exchange) mPreviewSize.width else mPreviewSize.height,
            if (exchange) textureView.height else textureView.width,
            if (exchange) textureView.width else textureView.height,
            previewSize!!.toList()
        )

        textureView.surfaceTexture.setDefaultBufferSize(mPreviewSize.width, mPreviewSize.height)
        Log.d("xxd","预览最优尺寸 ：${mPreviewSize.width} * ${mPreviewSize.height}, 比例  ${mPreviewSize.width.toFloat() / mPreviewSize.height}")
        Log.d("xxd","保存图片最优尺寸 ：${mSaveSize.width} * ${mSaveSize.height}, 比例  ${mSaveSize.width.toFloat() / mSaveSize.height}")

        //根据预览的尺寸大小调整TextureView的大小，保证画面不被拉伸
//        val orientation = activity.resources.configuration.orientation
//        if (orientation == Configuration.ORIENTATION_LANDSCAPE){
//            val layoutParams = textureView.layoutParams
//            layoutParams.width = mPreviewSize.width
//            layoutParams.height = mPreviewSize.height
//            textureView.layoutParams = layoutParams
////            textureView.setAspectRatio(mPreviewSize.width, mPreviewSize.height)
//        }
//        else{
//            val layoutParams = textureView.layoutParams
//            layoutParams.width = mPreviewSize.height
//            layoutParams.height = mPreviewSize.width
//            textureView.layoutParams = layoutParams
////            textureView.setAspectRatio(mPreviewSize.height, mPreviewSize.width)
//        }

        mImageReader = ImageReader.newInstance(mPreviewSize.width, mPreviewSize.height, ImageFormat.JPEG, 1)
        mImageReader?.setOnImageAvailableListener(onImageAvailableListener, mCameraHandler)

//        if (openFaceDetect)
//            initFaceDetect()

        openCamera()
    }

    /**
     * 根据提供的屏幕方向 [displayRotation] 和相机方向 [sensorOrientation] 返回是否需要交换宽高
     */
    private fun exchangeWidthAndHeight(displayRotation: Int, sensorOrientation: Int?): Boolean {
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
     *
     * 根据提供的参数值返回与指定宽高相等或最接近的尺寸
     *
     * @param targetWidth   目标宽度
     * @param targetHeight  目标高度
     * @param maxWidth      最大宽度(即TextureView的宽度)
     * @param maxHeight     最大高度(即TextureView的高度)
     * @param sizeList      支持的Size列表
     *
     * @return  返回与指定宽高相等或最接近的尺寸
     *
     */
    private fun getBestSize(
        targetWidth: Int,
        targetHeight: Int,
        maxWidth: Int,
        maxHeight: Int,
        sizeList: List<Size>
    ): Size {
        val bigEnough = ArrayList<Size>()     //比指定宽高大的Size列表
        val notBigEnough = ArrayList<Size>()  //比指定宽高小的Size列表

        for (size in sizeList) {

            //宽<=最大宽度  &&  高<=最大高度  &&  宽高比 == 目标值宽高比
            if (size.width <= maxWidth && size.height <= maxHeight
                && size.width == size.height * targetWidth / targetHeight
            ) {

                if (size.width >= targetWidth && size.height >= targetHeight)
                    bigEnough.add(size)
                else
                    notBigEnough.add(size)
            }
            Log.d(
                "xxd",
                "系统支持的尺寸: ${size.width} * ${size.height} ,  比例 ：${size.width.toFloat() / size.height}"
            )
        }

        Log.d("xxd", "最大尺寸 ：$maxWidth * $maxHeight, 比例 ：${targetWidth.toFloat() / targetHeight}")
        Log.d(
            "xxd",
            "目标尺寸 ：$targetWidth * $targetHeight, 比例 ：${targetWidth.toFloat() / targetHeight}"
        )

        //选择bigEnough中最小的值  或 notBigEnough中最大的值
        return when {
//            bigEnough.size > 0 -> Collections.min(bigEnough, CompareSizesByArea())
//            notBigEnough.size > 0 -> Collections.max(notBigEnough, CompareSizesByArea())
            else -> sizeList[0]
        }
    }

    /**
     * 打开相机
     */
    private fun openCamera() {

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ToastUtil.showToast(activity, "没有相机权限！")

            return
        }

        mCameraManager.openCamera(mCameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                Log.d("xxd","onOpened")
                mCameraDevice = camera
                createCaptureSession(camera)
            }

            override fun onDisconnected(camera: CameraDevice) {
                Log.d("xxd","onDisconnected")
            }

            override fun onError(camera: CameraDevice, error: Int) {
                Log.d("xxd","onError $error")
                ToastUtil.showToast(activity, "打开相机失败")
            }
        }, mCameraHandler)
    }

    /**
     * 创建预览会话
     */
    private fun createCaptureSession(cameraDevice: CameraDevice) {

        val captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)

        val surface = Surface(textureView.surfaceTexture)
        captureRequestBuilder.addTarget(surface)  // 将CaptureRequest的构建器与Surface对象绑定在一起
        captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)      // 闪光灯
        captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE) // 自动对焦

        // 为相机预览，创建一个CameraCaptureSession对象
        cameraDevice.createCaptureSession(arrayListOf(surface, mImageReader?.surface), object : CameraCaptureSession.StateCallback() {

            override fun onConfigureFailed(session: CameraCaptureSession) {
                ToastUtil.showToast(activity, "开启预览会话失败！")
            }

            override fun onConfigured(session: CameraCaptureSession) {
                mCameraCaptureSession = session
                session.setRepeatingRequest(captureRequestBuilder.build(), mCaptureCallBack, mCameraHandler)
            }

        }, mCameraHandler)
    }

    private val mCaptureCallBack = object : CameraCaptureSession.CaptureCallback() {

        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult
        ) {
            super.onCaptureCompleted(session, request, result)
            canSwitchCamera = true
            canTakePicture = true
        }

        override fun onCaptureFailed(
            session: CameraCaptureSession,
            request: CaptureRequest,
            failure: CaptureFailure
        ) {
            super.onCaptureFailed(session, request, failure)
            Log.d("xxd","onCaptureFailed")
            ToastUtil.showToast(activity, "开启预览失败！")
        }
    }

    /**
     * 拍照
     */
    fun takePic() {
        if (mCameraDevice == null || !textureView.isAvailable || !canTakePicture) return

        mCameraDevice?.apply {

            val captureRequestBuilder = createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureRequestBuilder.addTarget(mImageReader!!.surface)

            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE) // 自动对焦
            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)     // 闪光灯
            captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, mCameraSensorOrientation)      //根据摄像头方向对保存的照片进行旋转，使其为"自然方向"
            mCameraCaptureSession?.capture(captureRequestBuilder.build(), null, mCameraHandler)
                ?: ToastUtil.showToast(activity,"拍照异常！")
        }
    }

    private val onImageAvailableListener = ImageReader.OnImageAvailableListener {

        val image = it.acquireNextImage()
        val byteBuffer = image.planes[0].buffer
        val byteArray = ByteArray(byteBuffer.remaining())
        byteBuffer.get(byteArray)
        it.close()
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

    fun releaseCamera() {
        mCameraCaptureSession?.close()
        mCameraCaptureSession = null

        mCameraDevice?.close()
        mCameraDevice = null

        mImageReader?.close()
        mImageReader = null

        canSwitchCamera = false
    }

    fun releaseThread() {
        handlerThread.quitSafely()
    }
}