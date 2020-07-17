package com.matrix.camera2.activity

import android.os.Bundle
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.matrix.camera2.R
import kotlinx.android.synthetic.main.activity_canvas_animator.*

/**
 *    author : xxd
 *    date   : 2020/6/28
 *    desc   :
 */
class CanvasAnimatorActivity : BaseActivity() {

    private var photoPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        photoPath = intent.getStringExtra("photoPath")
        super.onCreate(savedInstanceState)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_canvas_animator
    }

    override fun initView() {
        super.initView()

        view_ai_skin.initAfterMeasure(photoPath)


        initPhoto()
    }

    private fun initPhoto() {
        // 路径一样，防止每次重复加载缓存
        photoPath?.let {
            Glide.with(this)
                .load(it)
                .signature(ObjectKey(System.currentTimeMillis()))
                .into(iv_icon)
        }
    }
}