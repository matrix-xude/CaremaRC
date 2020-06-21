package com.matrix.camera2.helper

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import android.view.TextureView
import com.matrix.camera2.utils.LogUtil
import com.matrix.camera2.utils.TimestampUtil
import com.matrix.camera2.utils.ToastUtil

/**
 *    author : xxd
 *    date   : 2020/6/19
 *    desc   :
 */
class MyCamera2Helper(val activity: Activity, val textureView: TextureView) {

    // 必须放在init方法前面；默认此方法调用的时候还没有available
    private val surfaceTextureListener = object : TextureView.SurfaceTextureListener {

        // view大小发生变化调用
        override fun onSurfaceTextureSizeChanged(
            surface: SurfaceTexture?,
            width: Int,
            height: Int
        ) {
            LogUtil.d("onSurfaceTextureSizeChanged width = $width , height = $height")
            TimestampUtil.logTimestamp()
        }

        // onSurfaceTextureSizeChanged后会再次调用
        override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
            LogUtil.d("onSurfaceTextureUpdated")
            TimestampUtil.logTimestamp()
        }

        // view回收之后调用
        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
            LogUtil.d("onSurfaceTextureDestroyed")
            TimestampUtil.logTimestamp()
            return true
        }

        // 第一次准备好的时候调用
        override fun onSurfaceTextureAvailable(
            surface: SurfaceTexture?,
            width: Int,
            height: Int
        ) {
            LogUtil.d("onSurfaceTextureAvailable width = $width , height = $height")
            TimestampUtil.logTimestamp()
            initCamera(width, height)
        }
    }

    // 摄像头朝向，可以外部用来切换朝向，开始选择前置摄像头
    private var mCameraLensFacing = CameraCharacteristics.LENS_FACING_FRONT
    private var mCanSwitchCameraLens = false

    // 遍历摄像头找到的对应需要使用的摄像头参数
    private lateinit var mCameraCharacteristics: CameraCharacteristics
    private var mCameraId = ""

    // 相机打开的线程
    private val handlerThread = HandlerThread("CameraThread")
    private lateinit var mCameraHandler: Handler

    // 相机成功开启后得到的device
    private var mCameraDevice: CameraDevice? = null

    // device 创建成功得到 session
    private var mCameraSession: CameraCaptureSession? = null
    // 创建回调
    private var mImageReader: ImageReader? = null

    init {
        if (textureView.isAvailable) {
            initCamera(textureView.width, textureView.height)
        } else {
            textureView.surfaceTextureListener = surfaceTextureListener
        }
    }

    /**
     * 在textureView准备好之后执行
     * @param width 当前在 textureView 的宽度
     * @param height 当前在 textureView 的高度
     */
    private fun initCamera(width: Int, height: Int) {

        val cameraManager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        val initLensSuccess = initLens(cameraManager, mCameraLensFacing)
        if (!initLensSuccess) {
            ToastUtil.showToast(activity, "没找到制定的摄像头镜头！")
            return
        }

        initRotation()

        initSupportLevel()


        // 子线程开启相机，null则为当前线程
        handlerThread.start()
        mCameraHandler = Handler(handlerThread.looper)

        mImageReader = ImageReader.newInstance(500, 800, ImageFormat.JPEG, 1)

        openCamera(cameraManager, mCameraHandler)




    }

    /**
     * 初始化旋转
     */
    private fun initRotation() {
        val orientation = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)
        LogUtil.d("摄像头默认的旋转角度： ${orientation.toString()}")

        val rotation = activity.windowManager.defaultDisplay.rotation
        LogUtil.d("手机默认的旋转角度： $rotation")

        val orientation2 = activity.resources.configuration.orientation
        LogUtil.d("Activity默认的旋转角度： $orientation2")
    }

    /**
     * 初始化所有摄像头尺寸的大小
     */
    private fun initSupportSize(characteristics: CameraCharacteristics) {
        val configurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val outputSizes = configurationMap?.getOutputSizes(ImageFormat.JPEG)
        val outputSizesPre = configurationMap?.getOutputSizes(SurfaceTexture::class.java)
//        for (sizePic in outputSizes!!){
//            LogUtil.d("Picture输出格式,${sizePic.width}-${sizePic.height}")
//        }
//        for (sizePre in outputSizesPre!!){
//            LogUtil.d("预览的大小,${sizePre.width}-${sizePre.height}")
//        }
    }

    /**
     * session处理预览
     */
    private fun initRepeatingRequest() {
        val captureRequestBuilder =
            mCameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        val surface = Surface(textureView.surfaceTexture)
        captureRequestBuilder?.addTarget(surface)  // 将CaptureRequest的构建器与Surface对象绑定在一起
        captureRequestBuilder?.set(
            CaptureRequest.CONTROL_AE_MODE,
            CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
        )      // 闪光灯
        captureRequestBuilder?.set(
            CaptureRequest.CONTROL_AF_MODE,
            CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
        ) // 自动对焦

        mCameraSession?.setRepeatingRequest(
            captureRequestBuilder!!.build(),
            object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: TotalCaptureResult
                ) {
                    LogUtil.d("创建预览界面成功")
                }

                override fun onCaptureFailed(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    failure: CaptureFailure
                ) {
                    LogUtil.d("创建预览界面失败")
                }
            },
            mCameraHandler
        )
    }

    /**
     * 创建session
     */
    private fun initSession() {
        val surface = Surface(textureView.surfaceTexture)
        mCameraDevice?.createCaptureSession(
            arrayListOf(surface),
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigureFailed(session: CameraCaptureSession) {
                    LogUtil.d("创建session失败！！")
                }

                override fun onConfigured(session: CameraCaptureSession) {
                    LogUtil.d("创建session成功")
                    mCameraSession = session
                    initRepeatingRequest()
                }
            },
            mCameraHandler
        )
    }

    /**
     * 一切准备就绪后，打开相机，不能直接调用，必须在 initCamera 之后调用
     */
    @SuppressLint("MissingPermission")
    private fun openCamera(
        cameraManager: CameraManager,
        handler: Handler
    ) {

        cameraManager.openCamera(mCameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                LogUtil.d("相机创建成功")
                mCameraDevice = camera
                initSession()
            }

            // 如果textureView destroy，需要手动销毁相机，此方法不会自动调用
            override fun onDisconnected(camera: CameraDevice) {
                LogUtil.d("相机断开连接")
            }

            override fun onError(camera: CameraDevice, error: Int) {
                LogUtil.d("相机创建失败：error=$error")
            }

        }, handler)
    }

    /**
     * 初始化支持的设备等级
     * 如果需要特定的设备功能，可以在此方法里面控制
     */
    private fun initSupportLevel(): Boolean {
        var initSuccess = false

        // 前置摄像头支持硬件设备等级
        // 小米8 -> INFO_SUPPORTED_HARDWARE_LEVEL_FULL
        // 华为P30pro -> INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED
        val supportLevel =
            mCameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
        if (supportLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
            ToastUtil.showToast(activity, "摄像头太老,不支持新特新!")
        } else {
            initSuccess = true
        }
        return initSuccess
    }

    /**
     * 初始化镜头
     * 找到对应的镜头数据，设置 cameraCharacteristics，cameraId
     * @param cameraManager
     * @param cameraLensFacing 需要寻找的镜头
     */
    private fun initLens(cameraManager: CameraManager, cameraLensFacing: Int): Boolean {
        var initSuccess = false

        // 小米8 ID 为 "0"，"1" -> 二个可用摄像头
        // 华为P30pro ID 为 "0"，"1"，"2"，"3"，"4" -> 五个可用摄像头
        val cameraIdList = cameraManager.cameraIdList

        if (cameraIdList.isEmpty()) {
            ToastUtil.showToast(activity, "当前设备没有可用的摄像头")
            return initSuccess
        }

        val cameraLensList = mutableListOf<Int?>() // 判断是否有前、后置摄像头的
        for (cameraId in cameraIdList) {
            LogUtil.d("$cameraId")
            // 相机特征值，里面有摄像头方向，旋转度，可以拍照、预览的尺寸等大量信息，主要获取数据的类
            val cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId)

            // 摄像头的朝向，外置、内置、外接第三方 一共3种
            // 华为P30pro lensFacing 为 1,0,1,1,1 -> 1表示后置摄像头，0表示前置
            // 小米8 lensFacing 为 1，0 -> 1表示后置摄像头，0表示前置
            val lensFacing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)

            cameraLensList.add(lensFacing)
            if (lensFacing == cameraLensFacing) {
                initSuccess = true
                mCameraId = cameraId
                mCameraCharacteristics = cameraCharacteristics
            }

            initSupportSize(cameraCharacteristics)
        }

        // 包含前、后摄像头则可以切换
        if (cameraLensList.contains(CameraCharacteristics.LENS_FACING_FRONT)
            && cameraLensList.contains(CameraCharacteristics.LENS_FACING_BACK)
        ) {
            mCanSwitchCameraLens = true
        }

        return initSuccess
    }


}