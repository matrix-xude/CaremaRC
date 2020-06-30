package com.matrix.camera2.activity

import android.view.LayoutInflater
import android.widget.TextView
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.matrix.camera2.R
import com.matrix.camera2.decoration.LinearDecoration
import com.matrix.camera2.helper.CenterLayoutManager
import com.matrix.camera2.utils.LogUtil
import kotlinx.android.synthetic.main.activity_recycler.*
import kotlinx.android.synthetic.main.item_simple_line.view.*

/**
 *    author : xxd
 *    date   : 2020/6/29
 *    desc   :
 */
class RecyclerActivity : BaseActivity() {

    private var strList = mutableListOf<String>()
    lateinit var baseQuickAdapter: BaseQuickAdapter<String, BaseViewHolder>

    override fun getLayoutId(): Int {
        return R.layout.activity_recycler
    }

    override fun initView() {
        super.initView()
        initRecycler()
    }

    override fun initData() {
        super.initData()
        repeat(20) {
            if (it == 0) {
                strList.add("条")
            } else
                strList.add("条目 $it")
        }
        baseQuickAdapter.setNewData(strList)
    }


    private fun initRecycler() {
        recycler_simple.layoutManager =
            CenterLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recycler_simple.addItemDecoration(LinearDecoration.Builder().also {
            it.top = 0
            it.left = 10
            it.right = 10
        }.build())

        baseQuickAdapter =
            object : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_simple_line) {
                override fun convert(helper: BaseViewHolder, item: String?) {
//                    LogUtil.d("layoutPosition=${helper.layoutPosition}")
//                    LogUtil.d("adapterPosition=${helper.adapterPosition}")
                    helper.setText(R.id.tv_name, item)
                }
            }
        baseQuickAdapter.bindToRecyclerView(recycler_simple)

//        val tv3 = LayoutInflater.from(this)
//            .inflate(R.layout.item_simple_line, null, false)
//        tv3.tv_name.text= "头部"
//        baseQuickAdapter.addHeaderView(tv3)
//
//        val tv4 = LayoutInflater.from(this)
//            .inflate(R.layout.item_simple_line, null, false)
//        tv4.tv_name.text= "头部22"
//        baseQuickAdapter.addHeaderView(tv4)
//
//
//        val tv = LayoutInflater.from(this)
//            .inflate(R.layout.item_simple_line, null, false)
//        tv.tv_name.text= "哈哈哈"
//        baseQuickAdapter.addFooterView(tv)
//
//        val tv2 = LayoutInflater.from(this)
//            .inflate(R.layout.item_simple_line, null, false)
//        tv2.tv_name.text = "哈哈222哈"
//        baseQuickAdapter.addFooterView(tv2,90)


        initClick()
    }

    /**
     * 点击事件的初始化
     */
    private fun initClick() {
        baseQuickAdapter.setOnItemClickListener { adapter, view, position ->

            LogUtil.d(
                "itemCount=${baseQuickAdapter.itemCount},position=${position}," +
                        "adapter.data.size=${adapter.data.size}"
            )

            LogUtil.d("view.width=${view.width}")
            val layoutParams = view.layoutParams
//            layoutParams.width = 400
//            view.layoutParams = layoutParams


            var width2 = 0
            val parent = view.parent
            if (parent is RecyclerView) {
                width2 = parent.width
                LogUtil.d("parent.width=${parent.measuredWidth}")
            }
            LogUtil.d(
                "computeScroll=${recycler_simple.computeHorizontalScrollOffset()}" +
                        ",range=${recycler_simple.computeHorizontalScrollRange()}" +
                        ",extent=${recycler_simple.computeHorizontalScrollExtent()}"
            )

            val linearLayoutManager = recycler_simple.layoutManager as LinearLayoutManager
            LogUtil.d("view.width=${view.width}")
//            linearLayoutManager.scrollToPositionWithOffset(position, (width2-view.width)/2)
//            recycler_simple.smoothScrollToPosition(position)
//            recycler_simple.smoothScrollBy(
//                view.left + view.width / 2 - recycler_simple.width / 2,
//                0
//            )
            linearLayoutManager.smoothScrollToPosition(recycler_simple,RecyclerView.State(),15)

            LogUtil.d("findFirstVisibleItemPosition=${linearLayoutManager.findFirstVisibleItemPosition()}")
            LogUtil.d("findFirstCompletelyVisibleItemPosition=${linearLayoutManager.findFirstCompletelyVisibleItemPosition()}")
            LogUtil.d("findLastVisibleItemPosition=${linearLayoutManager.findLastVisibleItemPosition()}")
            LogUtil.d("findLastCompletelyVisibleItemPosition=${linearLayoutManager.findLastCompletelyVisibleItemPosition()}")


            val childCount = recycler_simple.childCount
            val firstChildPosition =
                recycler_simple.getChildAdapterPosition(recycler_simple.getChildAt(0))
            val lastChildPosition =
                recycler_simple.getChildAdapterPosition(recycler_simple.getChildAt(childCount-1))
            LogUtil.d("childCount=${childCount}")
            LogUtil.d("firstChildPosition=${firstChildPosition}")
            LogUtil.d("lastChildPosition=${lastChildPosition}")

            val left = recycler_simple.getChildAt(position-firstChildPosition).left
            val right = recycler_simple.getChildAt(lastChildPosition-position).left
        }
    }
}