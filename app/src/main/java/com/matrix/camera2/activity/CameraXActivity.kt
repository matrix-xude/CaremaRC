package com.matrix.camera2.activity

import android.content.Context
import android.content.Intent
import android.util.DisplayMetrics
import android.view.View
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.matrix.camera2.R
import com.matrix.camera2.utils.FileUtil
import com.matrix.camera2.utils.LogUtil
import com.matrix.camera2.utils.ToastUtil
import kotlinx.android.synthetic.main.activity_camerax.*
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 *    author : xxd
 *    date   : 2020/6/22
 *    desc   :
 */
class CameraXActivity : BaseActivity() {

    // 执行拍照里面的延时操作线程池
    private lateinit var cameraExecutor: ExecutorService

    // 照片存储的路径(Android 10已经之后不能再获取 ExternalStorageDirectory,这里使用外部cache)
    private lateinit var photoFile: File


    private var cameraProvider: ProcessCameraProvider? = null // camera控制器，必须不为null
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK // 摄像头方向

    private var preview: Preview? = null  // 预览的界面
    private var imageCapture: ImageCapture? = null // 拍照的捕捉器
    private var flashMode: Int = ImageCapture.FLASH_MODE_AUTO // 默认自动闪光

    private var canTakePhoto = false  // 是否可以拍照
    private var canSwitchLens = false  // 是否可以切换前后镜头

    companion object {
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0

        private val flashMap = mapOf(
            ImageCapture.FLASH_MODE_AUTO to "闪光灯（自动）",
            ImageCapture.FLASH_MODE_ON to "闪光灯（常亮）",
            ImageCapture.FLASH_MODE_OFF to "闪光灯（关闭）"
        )
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_camerax
    }

    override fun initView() {
        super.initView()

        cameraExecutor = Executors.newSingleThreadExecutor()
        photoFile = getOutputFile(this)

        // 所有的操作都要放在preview测量好之后操作
        preview_view.post {
            updateCameraUi()
            setUpCamera()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        cameraExecutor.shutdown()
    }

    /**
     * camera界面各种按钮的状态，需要根据当前camera的状态动态调整
     */
    private fun updateCameraUi() {

        tv_switch_camera.setOnClickListener {
            if (!canSwitchLens) // 防止重复前后切换摄像头
                return@setOnClickListener
            canSwitchLens = false

            lensFacing = if (CameraSelector.LENS_FACING_FRONT == lensFacing) {
                CameraSelector.LENS_FACING_BACK
            } else {
                CameraSelector.LENS_FACING_FRONT
            }
            updateCameraFlashButton()
            bindCameraUseCases()
        }

        tv_flash.setOnClickListener {
            imageCapture?.let {
                flashMode = when (flashMode) {
                    ImageCapture.FLASH_MODE_AUTO -> ImageCapture.FLASH_MODE_ON
                    ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_OFF
                    ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_AUTO
                    else -> ImageCapture.FLASH_MODE_AUTO
                }
                tv_flash.text = flashMap[flashMode]
                it.flashMode = flashMode
            }
        }

        tv_take_picture.setOnClickListener {

            imageCapture?.let {
                if (!canTakePhoto) // 防止重复点击拍照
                    return@setOnClickListener
                canTakePhoto = false

                val metadata = ImageCapture.Metadata().apply {
                    isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
                }

                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
                    .setMetadata(metadata)
                    .build()

                it.takePicture(outputOptions, cameraExecutor,
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            runOnUiThread {
                                canTakePhoto = true
                                ToastUtil.showToast(
                                    this@CameraXActivity,
                                    "拍照成功，保存在${photoFile.absolutePath}"
                                )
                            }
                            val intent =
                                Intent(this@CameraXActivity, CanvasAnimatorActivity::class.java)
                            intent.putExtra("photoPath", photoFile.absolutePath)
                            startActivity(intent)
                        }

                        override fun onError(exception: ImageCaptureException) {
                            canTakePhoto = true
                            LogUtil.d(exception.message)
                        }
                    })
            }
        }
    }

    /**
     * 初始化camera,确定是否能切换镜头，并且绑定相机的行为
     */
    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable {

            cameraProvider = cameraProviderFuture.get()

            val hasBackCamera = hasBackCamera()
            val hasFrontCamera = hasFrontCamera()
            lensFacing = when {
                hasBackCamera -> CameraSelector.LENS_FACING_BACK
                hasFrontCamera -> CameraSelector.LENS_FACING_FRONT
                else -> throw IllegalStateException("Back and front camera are unavailable")
            }

            tv_flash.text = flashMap[flashMode]
            updateCameraFlashButton()
            updateCameraSwitchButton(hasBackCamera && hasFrontCamera)

            bindCameraUseCases()

        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraUseCases() {
        // 根据preview的大小确定预览像素
        val metrics = DisplayMetrics().also { preview_view.display.getRealMetrics(it) }

        val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)

        val rotation = preview_view.display.rotation

        // 再次检测CameraProvider
        val cameraProvider = cameraProvider
            ?: throw IllegalStateException("Camera initialization failed.")

        // CameraSelector，根据镜头朝向获取device设备
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        // Preview
        preview = Preview.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()

        // ImageCapture
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .setFlashMode(ImageCapture.FLASH_MODE_AUTO)
            .build()

        // 非常重要，必须先接触其它绑定行为，才能切换
        cameraProvider.unbindAll()

        // bind行为，开启预览
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
        preview?.setSurfaceProvider(preview_view.createSurfaceProvider())

        canTakePhoto = true
        canSwitchLens = true
    }

    // 闪光灯的状态
    private fun updateCameraFlashButton() {
        tv_flash.visibility = if (lensFacing == CameraSelector.LENS_FACING_BACK)
            View.VISIBLE else View.GONE
    }

    // 切换前后镜头的按钮状态
    private fun updateCameraSwitchButton(show: Boolean) {
        tv_switch_camera.visibility = if (show) View.VISIBLE else View.GONE
    }

    /**
     * 拍照保留在这个file上，每次覆盖上次拍摄
     */
    private fun getOutputFile(context: Context): File {
        val filePath = StringBuilder().apply {
            append(context.externalCacheDir?.absolutePath)
            append(File.separator)
            append("ai")
            append(File.separator)
            append("ai_photo.jpg")
        }.toString()
        LogUtil.d(filePath)
        val file = File(filePath)
        FileUtil.createOrExistsDir(file.parentFile)
        file.createNewFile()
        return file
    }

    /** 是否有后置摄像头 */
    private fun hasBackCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false
    }

    /** 是否有前置摄像头 */
    private fun hasFrontCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: false
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

}
