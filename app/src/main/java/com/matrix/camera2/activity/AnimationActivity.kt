package com.matrix.camera2.activity

import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.matrix.camera2.R
import com.matrix.camera2.decoration.LinearDecoration
import kotlinx.android.synthetic.main.activity_animation.*

/**
 *    author : xxd
 *    date   : 2020/6/24
 *    desc   :
 */
class AnimationActivity : BaseActivity() {

    val strList = listOf("translate", "rotate", "scale", "gradual", "mixture")
    lateinit var baseQuickAdapter: BaseQuickAdapter<String, BaseViewHolder>

    override fun getLayoutId(): Int {
        return R.layout.activity_animation
    }

    override fun initView() {
        super.initView()
        initRecycler()
    }

    private fun initRecycler() {
        recycler_animation.layoutManager = GridLayoutManager(this, 2)
        recycler_animation.addItemDecoration(LinearDecoration.Builder().getNormalBuilder().build())

        baseQuickAdapter =
            object : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_animation) {
                override fun convert(helper: BaseViewHolder, item: String?) {
                    helper.setText(R.id.tv_name, item)
                }
            }
        baseQuickAdapter.bindToRecyclerView(recycler_animation)
        baseQuickAdapter.setNewData(strList)

        baseQuickAdapter.setOnItemClickListener { _, _, position ->
            when (position) {
                0 -> {
                }
            }
        }
    }
}