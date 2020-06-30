package com.matrix.camera2.helper

import android.content.Context
import android.util.AttributeSet
import android.util.DisplayMetrics
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView

/**
 *    author : xxd
 *    date   : 2020/6/30
 *    desc   : 外部调用 smoothScrollToPosition 方法会使得 position 处于中间位置
 */
class CenterLayoutManager : LinearLayoutManager {

    constructor(context: Context?) : super(context)

    constructor(context: Context?, orientation: Int, reverseLayout: Boolean) : super(
        context,
        orientation,
        reverseLayout
    )

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    /**
     * 重写此方法，使得每次调用此函数position会居中，而不是原先的只有在不可见的时候才能滑动到首尾
     */
    override fun smoothScrollToPosition(
        recyclerView: RecyclerView?,
        state: RecyclerView.State?,
        position: Int
    ) {
        val smoothScroller =
            CenterSmoothScroller(recyclerView!!.context)
        smoothScroller.targetPosition = position
        startSmoothScroll(smoothScroller)
    }

    inner class CenterSmoothScroller(context: Context?) : LinearSmoothScroller(context) {

        override fun calculateDtToFit(
            viewStart: Int,
            viewEnd: Int,
            boxStart: Int,
            boxEnd: Int,
            snapPreference: Int
        ): Int {
            return (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2);
        }

        override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float {
            return MILLISECONDS_PER_INCH / displayMetrics!!.densityDpi
        }
    }

    companion object {
        // 滑动帧的速度
        private const val MILLISECONDS_PER_INCH = 100f
    }


}