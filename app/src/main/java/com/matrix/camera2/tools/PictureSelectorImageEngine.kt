package com.matrix.camera2.tools

import android.content.Context
import android.widget.ImageView
import com.luck.picture.lib.engine.ImageEngine
import com.luck.picture.lib.listener.OnImageCompleteCallback
import com.luck.picture.lib.widget.longimage.SubsamplingScaleImageView
import com.matrix.camera2.utils.GlideUtil

/**
 *    author : xxd
 *    date   : 2020/6/23
 *    desc   :
 */
class PictureSelectorImageEngine : ImageEngine {

    override fun loadImage(context: Context, url: String, imageView: ImageView) {
        GlideUtil.loadNormal(imageView, url)
    }

    override fun loadImage(
        context: Context,
        url: String,
        imageView: ImageView,
        longImageView: SubsamplingScaleImageView?,
        callback: OnImageCompleteCallback?
    ) {
        GlideUtil.loadNormal(imageView, url)
    }

    override fun loadImage(
        context: Context,
        url: String,
        imageView: ImageView,
        longImageView: SubsamplingScaleImageView?
    ) {
        GlideUtil.loadNormal(imageView, url)
    }

    override fun loadAsGifImage(context: Context, url: String, imageView: ImageView) {
        GlideUtil.loadNormal(imageView, url)
    }

    override fun loadGridImage(context: Context, url: String, imageView: ImageView) {
        GlideUtil.loadNormal(imageView, url)
    }

    override fun loadFolderImage(context: Context, url: String, imageView: ImageView) {
        GlideUtil.loadNormal(imageView, url)
    }
}