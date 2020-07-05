package com.matrix.camera2.activity

import android.os.Bundle
import com.matrix.camera2.R
import kotlinx.android.synthetic.main.activity_canvas_animator.*

/**
 *    author : xxd
 *    date   : 2020/6/28
 *    desc   :
 */
class CanvasAnimatorActivity : BaseActivity() {

    private var photoPath : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        photoPath = intent.getStringExtra("photoPath")
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_canvas_animator
    }

    override fun initView() {
        super.initView()

        view_ai_skin.post {
            view_ai_skin.initAfterMeasure(photoPath)
        }


    }
}