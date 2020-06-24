package com.matrix.camera2.activity

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.TranslateAnimation
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

    val strList = listOf("translate", "rotate", "scale", "gradual", "mixture", "stop")
    lateinit var baseQuickAdapter: BaseQuickAdapter<String, BaseViewHolder>

    override fun getLayoutId(): Int {
        return R.layout.activity_animation
    }

    override fun initView() {
        super.initView()
        initRecycler()
        iv_vector.setOnClickListener{
            ToastUtil.showToast(this,"我被点击了")
        }
    }

    private fun initRecycler() {
        recycler_animation.layoutManager = GridLayoutManager(this, 2)
        recycler_animation.addItemDecoration(LinearDecoration.Builder().getNormalBuilder().build())

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

        baseQuickAdapter.setOnItemClickListener { _, _, position ->
            when (position) {
                0 -> {
                    AnimationUtils.loadAnimation(this, R.anim.translate_1).also {
                        iv_vector.startAnimation(it)
                    }
                }
                5 ->{
                    iv_vector.clearAnimation()
                }
            }
        }
    }
}