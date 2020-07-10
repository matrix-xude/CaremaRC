package com.matrix.camera2.activity

import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.matrix.camera2.R
import com.matrix.camera2.bean.CenterBean
import com.matrix.camera2.decoration.LinearDecoration
import com.matrix.camera2.helper.CenterLayoutManager
import com.matrix.camera2.utils.LogUtil
import com.matrix.camera2.view.CenterRecyclerView
import kotlinx.android.synthetic.main.activity_skin_result.*
import kotlinx.android.synthetic.main.item_skin_result.view.*
import java.lang.Exception

/**
 *    author : xxd
 *    date   : 2020/6/29
 *    desc   :
 */
class SkinResultActivity : BaseActivity() {

    private var strList = mutableListOf<CenterBean>()
    lateinit var baseQuickAdapter: BaseMultiItemQuickAdapter<CenterBean, BaseViewHolder>

    override fun getLayoutId(): Int {
        return R.layout.activity_skin_result
    }

    override fun initView() {
        super.initView()
        initRecycler()
    }

    override fun initData() {
        super.initData()
        strList.add(CenterBean("头部", CenterBean.TYPE_HEADER_HOLDER))
        repeat(20) {
            strList.add(CenterBean("条目${it}"))
        }
        strList.add(CenterBean("尾部", CenterBean.TYPE_TAIL_HOLDER))

        baseQuickAdapter.setNewData(strList)

        baseQuickAdapter.setOnItemClickListener { _, _, position ->
            val centerBean = strList[position]
            when (centerBean.type) {
                CenterBean.TYPE_HEADER_HOLDER, CenterBean.TYPE_TAIL_HOLDER -> {
                }
                CenterBean.TYPE_DATA -> {
                    for (i in 0 until strList.size) {
                        strList[i].selected = position == i
                    }
                    recycler_view.smoothScrollToPosition(position)
                    baseQuickAdapter.notifyDataSetChanged()
                }
            }
        }

        baseQuickAdapter.setOnItemChildClickListener{ _, _, position ->

        }
    }

    private fun initRecycler() {
        recycler_view.layoutManager =
            CenterLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recycler_view.addItemDecoration(LinearDecoration.Builder().also {
            it.top = 0
            it.left = 10
            it.right = 10
        }.build())
//        baseQuickAdapter =
//            object : BaseQuickAdapter<CenterBean, BaseViewHolder>(R.layout.item_skin_result) {
//                override fun convert(helper: BaseViewHolder, item: CenterBean?) {
//                    item?.run {
//                        helper.setText(R.id.tv_name, name)
//                        helper.itemView.tv_name.setBackgroundColor(
//                            ContextCompat.getColor(
//                                this@SkinResultActivity,
//                                if (selected) R.color.yellow else R.color.deepskyblue
//                            )
//                        )
//                    }
//                }
//            }

        baseQuickAdapter = object : BaseMultiItemQuickAdapter<CenterBean, BaseViewHolder>(strList) {
            init {
                addItemType(CenterBean.TYPE_HEADER_HOLDER, R.layout.item_skin_result_header)
                addItemType(CenterBean.TYPE_DATA, R.layout.item_skin_result)
                addItemType(CenterBean.TYPE_TAIL_HOLDER, R.layout.item_skin_result_tail)
            }

            override fun onCreateDefViewHolder(parent: ViewGroup?, viewType: Int): BaseViewHolder {
                when (viewType) {
                    CenterBean.TYPE_HEADER_HOLDER -> {
                        return object :
                            BaseViewHolder(getItemView(R.layout.item_skin_result_header, parent)) {
                            init {
                                val view = itemView
                                val width =
                                    (recyclerView.width - recyclerView.paddingStart - recyclerView.paddingEnd) / 2 - 80
                                LogUtil.d("头部的长度：${width}")
                                view.layoutParams.width = width
                            }
                        }
                    }
                    CenterBean.TYPE_TAIL_HOLDER -> {
                        return object :
                            BaseViewHolder(getItemView(R.layout.item_skin_result_tail, parent)) {
                            init {
                                val view = itemView
                                val width =
                                    (recyclerView.width - recyclerView.paddingStart - recyclerView.paddingEnd) / 2 - 80
                                LogUtil.d("尾部的长度：${width}")
                                view.layoutParams.width = width
                            }
                        }
                    }
                    else -> return super.onCreateDefViewHolder(parent, viewType)
                }

            }

            override fun convert(helper: BaseViewHolder, item: CenterBean?) {
                when (helper.itemViewType) {
                    CenterBean.TYPE_HEADER_HOLDER -> {
                    }
                    CenterBean.TYPE_TAIL_HOLDER -> {
                    }
                    CenterBean.TYPE_DATA -> {
                        item?.run {
                            helper.setText(R.id.tv_name, name)
                            helper.itemView.tv_name.setBackgroundColor(
                                ContextCompat.getColor(
                                    this@SkinResultActivity,
                                    if (selected) R.color.yellow else R.color.deepskyblue
                                )
                            )
                        }
                    }
                }
            }

        }
        baseQuickAdapter.bindToRecyclerView(recycler_view)


        recycler_view.setOnScrollCenterListener(object :
            CenterRecyclerView.OnScrollCenterListener {
            override fun onScrollCenter(position: Int) {
                LogUtil.d("我中间经过第${position}个条目")
                for (i in 0 until strList.size) {
                    strList[i].selected = position == i
                }

                try {
                    baseQuickAdapter.notifyDataSetChanged()

                } catch (e: Exception) {
                    LogUtil.d(e.message)
                }
            }

            override fun onScrollIdle(position: Int) {
                LogUtil.d("我静止在${position}个条目")
                recycler_view.smoothScrollToPosition(position)
                for (i in 0 until strList.size) {
                    strList[i].selected = position == i
                }
                baseQuickAdapter.notifyDataSetChanged()
            }
        })
    }
}