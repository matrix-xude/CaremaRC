package com.matrix.camera2.activity

import android.graphics.SurfaceTexture
import android.view.TextureView
import android.view.View
import com.matrix.camera2.R
import com.matrix.camera2.camera2.CameraHelperImpl
import com.matrix.camera2.helper.Camera2Helper
import com.matrix.camera2.helper.MyCamera2Helper
import com.matrix.camera2.utils.LogUtil
import com.matrix.camera2.utils.TimestampUtil
import kotlinx.android.synthetic.main.activity_camera.*

/**
 *    author : xxd
 *    date   : 2020/6/18
 *    desc   : camera2调用测试
 */
class CameraActivity : BaseActivity() {

    lateinit var cameraHelper: CameraHelperImpl

    override fun getLayoutId(): Int {
        return R.layout.activity_camera
    }

    override fun initView() {
        super.initView()

        tv_take_picture.setOnClickListener {
            cameraHelper.takePic()

        }

        tv_switch_camera.setOnClickListener {
            cameraHelper.switchLens()
        }

    }

    override fun initData() {
        super.initData()
        cameraHelper = CameraHelperImpl.Builder(this).apply {
            textureView = texture_view
        }.build()
        cameraHelper.openPreview()
//        Camera2Helper(this,texture_view)
    }

    fun testTextureViewParamChange() {
        val layoutParams = texture_view.layoutParams
        layoutParams.width = 500
        layoutParams.height = 1000
        texture_view.layoutParams = layoutParams

    }

}