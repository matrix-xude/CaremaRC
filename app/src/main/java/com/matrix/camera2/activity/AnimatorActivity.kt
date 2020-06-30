package com.matrix.camera2.activity

import android.animation.*
import android.graphics.drawable.AnimationDrawable
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.core.graphics.scaleMatrix
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.matrix.camera2.R
import com.matrix.camera2.decoration.LinearDecoration
import com.matrix.camera2.utils.ToastUtil
import kotlinx.android.synthetic.main.activity_animation.*
import kotlinx.android.synthetic.main.item_animation.view.*

class AnimatorActivity : BaseActivity() {

    private val strList =
        listOf("translate", "scale", "alpha", "rotate", "mixture", "stop", "animation-list")
    lateinit var baseQuickAdapter: BaseQuickAdapter<String, BaseViewHolder>

    override fun getLayoutId(): Int {
        return R.layout.activity_animator
    }

    override fun initView() {
        super.initView()
        initRecycler()
        iv_vector.setOnClickListener {
            ToastUtil.showToast(this, "我被点击了")
        }
    }

    private fun initRecycler() {
        recycler_animation.layoutManager = GridLayoutManager(this, 2)
        recycler_animation.addItemDecoration(LinearDecoration.Builder().buildNormal())

        baseQuickAdapter =
            object : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_animation) {
                override fun convert(helper: BaseViewHolder, item: String?) {
                    helper.setText(R.id.tv_name, item)
                    helper.itemView.tv_name.setBackgroundColor(
                        ContextCompat.getColor(
                            mContext,
                            when (helper.adapterPosition) {
                                5 -> R.color.blueviolet
                                else -> R.color.lightskyblue
                            }
                        )
                    )
                }
            }
        baseQuickAdapter.bindToRecyclerView(recycler_animation)
        baseQuickAdapter.setNewData(strList)

        initClick()
    }

    /**
     * 点击事件的初始化
     */
    private fun initClick() {
        baseQuickAdapter.setOnItemClickListener { _, _, position ->
            when (position) {
                0 -> {
                    ObjectAnimator.ofFloat(iv_vector, "x", 0f, 700f,350f).apply {
                        duration = 3000
                        repeatCount = 0
                        repeatMode = ValueAnimator.REVERSE
                        interpolator = TimeInterpolator {
                            when{
                                it < 0.5f -> 1f
                                else -> it+0.5f
                            }
                        }
                    }.start()
                }
                1 -> {
                    ObjectAnimator.ofFloat(iv_vector.apply { pivotX = iv_vector.width/2f }, "scaleX", 1f, 2f).apply {

                        duration = 1500
                        repeatCount = 3
                        repeatMode = ValueAnimator.REVERSE
                    }.start()

                }
            }
        }
    }
}