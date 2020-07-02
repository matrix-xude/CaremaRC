package com.matrix.camera2.view

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import com.matrix.camera2.utils.LogUtil

/**
 *    author : xxd
 *    date   : 2020/7/1
 *    desc   : 滑动的时候中间的view回调，滑动停止后的view也回调
 */
class CenterRecyclerView : RecyclerView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private var onScrollCenterListener: OnScrollCenterListener? = null
    private var currentIndex = -1
    private var isDragging = false // 是否正在被拖拽，如果没有拖拽，静止时不回调，可能是代码设置的移动

    private val onScrollListener: OnScrollListener = object : OnScrollListener() {

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (dx == 0 && dy == 0) // 防止初始化的回调,导致视图初始不能点击
                return
            val realWidth =
                recyclerView.width - recyclerView.paddingStart - recyclerView.paddingEnd
            val childCount = recyclerView.childCount
            repeat(childCount) {
                val itemView = recyclerView.getChildAt(it)
                if (itemView.left < realWidth / 2 && itemView.right > realWidth / 2) {
                    val childAdapterPosition = getChildAdapterPosition(itemView)
                    if (childAdapterPosition != currentIndex && childAdapterPosition>=0) {
                        currentIndex = childAdapterPosition
                        onScrollCenterListener?.onScrollCenter(currentIndex)
                    }
                    return@repeat
                }
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == SCROLL_STATE_IDLE) {
                if (!isDragging)
                    return
                isDragging = false
                LogUtil.d("我滑动静止了")
                var flag = false //是否找到了停止的那个item
                val realWidth =
                    recyclerView.width - recyclerView.paddingStart - recyclerView.paddingEnd
                val childCount = recyclerView.childCount
                repeat(childCount) {
                    val itemView = recyclerView.getChildAt(it)
                    if (itemView.left < realWidth / 2 && itemView.right > realWidth / 2) {
                        val childAdapterPosition = getChildAdapterPosition(itemView)
                        if (childAdapterPosition < 0)
                            return@repeat
                        flag = true
                        currentIndex = childAdapterPosition
                        onScrollCenterListener?.onScrollIdle(currentIndex)
                        return@repeat
                    }
                }
                if (!flag && currentIndex != -1) {  // 停在了item的空隙中
                    onScrollCenterListener?.onScrollIdle(currentIndex)
                }
            } else if (newState == SCROLL_STATE_DRAGGING) {
                isDragging = true
                LogUtil.d("我被手拖拽了")
            } else if (newState == SCROLL_STATE_SETTLING) {
                LogUtil.d("自动滑滑")
            }
        }
    }

    init {
        addOnScrollListener(onScrollListener)
    }

    fun setOnScrollCenterListener(listener: OnScrollCenterListener) {
        this.onScrollCenterListener = listener
    }

    interface OnScrollCenterListener {

        fun onScrollCenter(position: Int)

        fun onScrollIdle(position: Int)

    }


}