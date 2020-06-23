package com.matrix.camera2.utils

import android.widget.ImageView
import com.bumptech.glide.Glide

/**
 *    author : xxd
 *    date   : 2020/6/23
 *    desc   :
 */
object GlideUtil {

    fun loadNormal(imageView: ImageView, url: String) {
        Glide.with(imageView)
            .load(url)
            .fitCenter()
            .into(imageView)
    }
}