package com.matrix.camera2.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.matrix.camera2.R
import com.matrix.camera2.decoration.LinearDecoration
import com.matrix.camera2.utils.ToastUtil
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.functions.Function
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : BaseActivity() {

    private val strList = listOf("camera2", "cameraX", "SelectPicture", "β射线", "等效原理", "自旋1/2")
    private lateinit var adapter: BaseQuickAdapter<String, BaseViewHolder>

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
                when (position) {
                    0 -> {
                        requestCameraStorage()
                        ActivityCompat.shouldShowRequestPermissionRationale(this, "")
                    }
                    1 -> {
                        val intent = Intent(this@MainActivity, CameraXActivity::class.java)
                        startActivity(intent)
                    }
                    2-> {
                        val intent = Intent(this@MainActivity, PhotoActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
        adapter.bindToRecyclerView(recycler_main)
    }

    @SuppressLint("CheckResult")
    private fun initRecycler() {
        recycler_main.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        val decoration = LinearDecoration.Builder().apply {
            left = 80
            right = 80
            top = 70
        }.build()
        recycler_main.addItemDecoration(decoration)
    }

    // 申请相机、存储权限
    @SuppressLint("CheckResult")
    private fun requestCameraStorage() {
        val rxPermissions = RxPermissions(this)
        rxPermissions.setLogging(true)
        rxPermissions.requestEach(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
            .subscribe { permission ->
                if (permission.granted) {
                    if (permission.name == (Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        val intent = Intent(this@MainActivity, CameraActivity::class.java)
                        startActivity(intent)
                    }
                } else if (permission.shouldShowRequestPermissionRationale) {
                    ToastUtil.showToast(this@MainActivity, "我再出弹出的选择框")
                    Log.d("xxd", "拒绝没禁止弹框")
                } else {
                    ToastUtil.showToast(this@MainActivity, "我应该去setting")
                    Log.d("xxd", "禁止弹框")
                    val intent = Intent()
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
                    intent.data = Uri.fromParts(
                        "package",
                        this.packageName,
                        null
                    )
                    this.startActivity(intent)
                }
            }
    }
}
