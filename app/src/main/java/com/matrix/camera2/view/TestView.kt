package com.matrix.camera2.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import com.matrix.camera2.R
import com.matrix.camera2.utils.LogUtil

class TestView : View {

    constructor(context: Context?) : super(context) {
        initAttribute(context, null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initAttribute(context, attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initAttribute(context, attrs)
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        initAttribute(context, attrs)
    }

    private fun initAttribute(context: Context?, attrs: AttributeSet?) {
        attrs?.let {
            repeat(attrs.attributeCount) {
                LogUtil.d("name=${attrs.getAttributeName(it)} value=${attrs.getAttributeValue(it)}")
            }
        }

        val ta = context?.obtainStyledAttributes(attrs, R.styleable.Test)
        ta?.run {
            val size = getDimension(R.styleable.Test_test_size, 15f)
            val color = getColor(R.styleable.Test_test_color, Color.RED)
            val drawable = getDrawable(R.styleable.Test_test_drawable)
            val width = getDimension(R.styleable.Test_test_width, 0f)
            LogUtil.d("size=${size} color=${color} drawable=${drawable} width=${width}")


            recycle()
        }
    }
}