package com.matrix.camera2.activity

import android.graphics.SurfaceTexture
import android.view.TextureView
import android.view.View
import com.matrix.camera2.R
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

    lateinit var cameraHelper: Camera2Helper

    override fun getLayoutId(): Int {
        return R.layout.activity_camera
    }

    override fun initView() {
        super.initView()

        tv_take_picture.setOnClickListener {
        }

        tv_switch_camera.setOnClickListener {
        }

    }

    override fun initData() {
        super.initData()
        cameraHelper = Camera2Helper(this, texture_view)
    }

    fun testTextureViewParamChange() {
        val layoutParams = texture_view.layoutParams
        layoutParams.width = 500
        layoutParams.height = 1000
        texture_view.layoutParams = layoutParams

    }

    /**
     * TextureView.SurfaceTextureListener 相当于普通view的 ViewTreeObserver 监听view变化，完成
     * 时 SurfaceTexture,width,height 已经准备好。
     * tips: 如果监听的时候 TextureView 已经准备完毕，则不会回调任何方法，非粘性事件，这时候应该通过
     * TextureView.isAvailable 判断是否可用
     */
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

        // onSurfaceTextureSizeChanged后会再次调用,非切换调用
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
        }
    }
}