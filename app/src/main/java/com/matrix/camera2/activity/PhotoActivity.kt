package com.matrix.camera2.activity

import android.content.Intent
import com.google.gson.Gson
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.listener.OnResultCallbackListener
import com.matrix.camera2.R
import com.matrix.camera2.tools.PictureSelectorImageEngine
import com.matrix.camera2.utils.GlideUtil
import com.matrix.camera2.utils.LogUtil
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.activity_photo.*

/**
 *    author : xxd
 *    date   : 2020/6/23
 *    desc   :
 */
class PhotoActivity : BaseActivity() {

    override fun getLayoutId(): Int {
        return R.layout.activity_photo
    }

    override fun initView() {
        super.initView()

        tv_select.setOnClickListener {
            openPictureSelector()
        }
    }

    // 打开图库，最大选择一张图片，不能是视频
    private fun openPictureSelector() {
        PictureSelector.create(this)
            .openGallery(PictureMimeType.ofImage())
            .selectionMode(PictureConfig.SINGLE)
//            .isPreviewImage(true)
//            .openGallery(PictureMimeType.ofImage())
            .isCamera(false)
            .isGif(true)
//            .selectionMode(PictureConfig.SINGLE)
            .isPreviewImage(true)
            .isAndroidQTransform(true)
//            .imageSpanCount(4)
            .isSingleDirectReturn(true)
//            .enablePreviewAudio(false)
            .imageEngine(PictureSelectorImageEngine())
            .forResult(object : OnResultCallbackListener<LocalMedia> {
                override fun onResult(result: MutableList<LocalMedia>?) {
                    result?.let {
                        LogUtil.d("选择了${result.size}张图片")
                        val gson = Gson()
                        for (localMedia in result) {
                            val toJson = gson.toJson(localMedia)
                            Logger.json(toJson)
                            GlideUtil.loadNormal(iv_icon,localMedia.androidQToPath)

                            val intent = Intent(this@PhotoActivity, CanvasAnimatorActivity::class.java)
                            intent.putExtra("photoPath", localMedia.androidQToPath)
                            startActivity(intent)
                        }
                    }
                }

                override fun onCancel() {
                    LogUtil.d("选择图库点击了返回按钮")
                }

            })

    }
}