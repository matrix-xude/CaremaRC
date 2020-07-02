package com.matrix.camera2.bean

import com.chad.library.adapter.base.entity.MultiItemEntity

/**
 *    author : xxd
 *    date   : 2020/7/1
 *    desc   :
 */
data class CenterBean(val name: String, var type: Int = TYPE_DATA, var selected: Boolean = false) :
    MultiItemEntity {

    companion object {
        const val TYPE_HEADER_HOLDER = 1  // 头部占位的空白部分
        const val TYPE_DATA = 2  // 真正的数据部分
        const val TYPE_TAIL_HOLDER = 3 // 尾部的站位，包含相机按钮
    }

    override fun getItemType(): Int {
        return type
    }
}
