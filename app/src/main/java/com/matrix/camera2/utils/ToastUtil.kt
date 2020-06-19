package com.matrix.camera2.utils

import android.content.Context
import android.widget.Toast

/**
 *    author : xxd
 *    date   : 2020/6/18
 *    desc   :
 */
object ToastUtil {

    fun showToast(context: Context, content: String) {
        Toast.makeText(context,content,Toast.LENGTH_SHORT).show()
    }

}