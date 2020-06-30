package com.matrix.camera2.activity

import android.animation.TimeInterpolator
import android.graphics.BitmapFactory
import android.graphics.drawable.Animatable
import android.graphics.drawable.AnimationDrawable
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.matrix.camera2.R
import com.matrix.camera2.decoration.LinearDecoration
import com.matrix.camera2.utils.ToastUtil
import kotlinx.android.synthetic.main.activity_animation.*
import kotlinx.android.synthetic.main.item_animation.view.*

/**
 *    author : xxd
 *    date   : 2020/6/24
 *    desc   :
 */
class AnimationActivity : BaseActivity() {

    val strList =
        listOf("translate", "scale", "alpha", "rotate", "mixture", "stop", "animation-list")
    lateinit var baseQuickAdapter: BaseQuickAdapter<String, BaseViewHolder>

    override fun getLayoutId(): Int {
        return R.layout.activity_animation
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
                    iv_vector.visibility = View.VISIBLE
                    AnimationUtils.loadAnimation(this, R.anim.translate_2).also {
                        iv_vector.startAnimation(it)
                    }
                }
                1 -> {
                    AnimationUtils.loadAnimation(this, R.anim.scale_1).also {
                        iv_vector.startAnimation(it)
                    }
                }
                2 -> {
                    AnimationUtils.loadAnimation(this, R.anim.alpha_1).also {
                        iv_vector.startAnimation(it)
                    }
                }
                3 -> {
                    AnimationUtils.loadAnimation(this, R.anim.rotate_1).also {
                        iv_vector.startAnimation(it)
                    }
                }
                4 -> {
                    AnimationUtils.loadAnimation(this, R.anim.set_1).also {
                        iv_vector.startAnimation(it)
                    }
                }
                5 -> {
                    iv_vector.clearAnimation()
                }
                6 -> {
//                    iv_vector.setBackgroundResource(R.drawable.animation_list)
//                    val animation = iv_vector.background
//                    if (animation is Animatable)
//                        animation.start()
                    iv_vector.setImageBitmap(null)

                    iv_vector.setBackgroundResource(R.drawable.animation_list)
                    val animation = iv_vector.background
                    if (animation is AnimationDrawable)
                        animation.start()

                }
            }
        }
    }
}