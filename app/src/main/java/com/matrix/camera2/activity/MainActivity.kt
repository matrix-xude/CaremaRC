package com.matrix.camera2.activity

import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.matrix.camera2.R
import com.matrix.camera2.decoration.LinearDecoration
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    private val strList = listOf("相机", "图库", "β射线", "等效原理", "自旋1/2")
    private lateinit var adapter : BaseQuickAdapter<String, BaseViewHolder>

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun initView() {
        super.initView()
        initRecycler()
        initAdapter()
    }

    override fun initData() {
        super.initData()
        adapter.setNewData(strList)
    }

    private fun initAdapter() {
        adapter = object : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_main) {
            override fun convert(helper: BaseViewHolder, item: String?) {
                helper.setText(R.id.tv_name, item)
            }
        }
        adapter.onItemClickListener =
            BaseQuickAdapter.OnItemClickListener { _, _, position ->
                when(position){
                    0 ->{
                        val intent = Intent(this@MainActivity, CameraActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
        adapter.bindToRecyclerView(recycler_main)
    }

    private fun initRecycler() {
        recycler_main.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)

        val decoration = LinearDecoration.Builder().apply {
            left = 80
            right = 80
            top = 70
        }.build()
        recycler_main.addItemDecoration(decoration)
    }
}
