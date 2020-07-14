package com.matrix.camera2.activity

import com.matrix.camera2.R
import com.matrix.camera2.view.SkinView
import kotlinx.android.synthetic.main.activity_table_view.*

/**
 *    author : xxd
 *    date   : 2020/7/14
 *    desc   :
 */
class TableViewActivity : BaseActivity() {

    override fun getLayoutId(): Int {
        return R.layout.activity_table_view
    }

    override fun initData() {
        fakeData()
    }

    private fun fakeData() {
        val list = mutableListOf<SkinView.SkinViewBean>()
        repeat(10) {
            list.add(SkinView.SkinViewBean().apply {
                score = (Math.random() * 100).toInt()
                bottomDesc = "06/${(Math.random() * 30).toInt()}"
                topDesc = "${score}分"
            })
        }
        skin_view.bindData(list)
    }
}