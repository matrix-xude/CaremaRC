package com.matrix.camera2.decoration

import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import java.lang.reflect.Constructor

class LinearDecoration private constructor(builder: Builder) : RecyclerView.ItemDecoration() {

    var top = 0
    var bottom = 0
    var left = 0
    var right = 0

    init {
        this.top = builder.top
        this.bottom = builder.bottom
        this.left = builder.left
        this.right = builder.right
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.left = left
        outRect.right = right
        outRect.top = top
        outRect.bottom = bottom
    }

    class Builder {
        var top = 0
        var bottom = 0
        var left = 0
        var right = 0

        fun buildNormal(): LinearDecoration {
            return LinearDecoration(Builder().also {
                it.top = 5
                it.left = 10
                it.right = 10
            })
        }

        fun build(): LinearDecoration {
            return LinearDecoration(this)
        }
    }
}
