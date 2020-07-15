package com.matrix.camera2.view

import android.content.Context
import android.graphics.*
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View

/**
 *    author : xxd
 *    date   : 2020/7/15
 *    desc   : 肌肤分数的view
 */
class SkinScoreView : View {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    companion object {
        const val START_COLOR = "#FF3B7E"
        const val END_COLOR = "#6C9AFF"

        const val CIRCLE_RADIUS = 63f // 圆环的半径，下面文字的宽度，这样才能匹配
        const val CIRCLE_PADDING = 5f // 圆环和下部文字的间距
        const val CIRCLE_STOKE_WIDTH = 8f // 圆环的宽度
        const val TEXT_HEIGHT = 22f // 文字的高度
        const val AROUND_PADDING = 10f // 四周的边距

        const val ARC_DEFAULT_COLOR = "#EEEEEE" // 背景圆弧的颜色

        const val FULL_SCORE = 100  // 满分
    }

    private val paint = Paint()
    private val textPaint = Paint()
    private val arcDefaultColor: Int
    private val startColor: Int
    private val endColor: Int
    private val radius: Float
    private val arcWidth: Float
    private val bottomWidth: Float
    private val bottomHeight: Float
    private val bottomPadding: Float
    private val aroundPadding: Float

    init {
        arcDefaultColor = Color.parseColor(ARC_DEFAULT_COLOR)
        startColor = Color.parseColor(START_COLOR)
        endColor = Color.parseColor(END_COLOR)
        radius = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, CIRCLE_RADIUS, resources.displayMetrics
        )
        arcWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, CIRCLE_STOKE_WIDTH, resources.displayMetrics
        )
        bottomWidth = radius * 2
        bottomHeight = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, TEXT_HEIGHT, resources.displayMetrics
        )
        bottomPadding = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, CIRCLE_PADDING, resources.displayMetrics
        )
        aroundPadding = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, AROUND_PADDING, resources.displayMetrics
        )
    }

    private var score = 40
    private var desc = "肌肤状态良好"

    /**
     * 绑定数据
     */
    fun bindData(score: Int, desc: String) {
        when {
            score < 0 -> this.score = 0
            score > FULL_SCORE -> this.score = FULL_SCORE
            else -> this.score = score
        }
        if (TextUtils.isEmpty(desc)) {
            this.desc = ""
        } else {
            this.desc = desc
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.let {
            drawArc(it)
            drawBottomText(it)
            drawScore(it)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            (radius * 2 + aroundPadding * 2).toInt(),
            (radius + bottomHeight + bottomPadding + aroundPadding * 2).toInt()
        )
    }

    /**
     * 绘制分数文字
     */
    private fun drawScore(canvas: Canvas) {
        val textSize1 = radius / 4 * 3
        val textSize2 = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, 16f, resources.displayMetrics
        )
        textPaint.apply {
            textSize = textSize1
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = Color.BLACK
        }
        val measureText1 = textPaint.measureText(score.toString())
        textPaint.textSize = textSize2
        val measureText2 = textPaint.measureText("分")

        textPaint.textSize = textSize1
        canvas.drawText(
            score.toString(),
            radius - (measureText1 + measureText2) / 2 + aroundPadding,
            radius + aroundPadding,
            textPaint
        )
        textPaint.textSize = textSize2
        canvas.drawText(
            "分",
            radius - (measureText1 + measureText2) / 2 + measureText1 + aroundPadding,
            radius + aroundPadding,
            textPaint
        )
    }

    /**
     * 绘制圆弧
     */
    private fun drawArc(canvas: Canvas) {
        configArcPaint()
        val gradient =
            SweepGradient(radius, radius, intArrayOf(endColor, startColor), floatArrayOf(0.5f, 1f))
        val rectF = RectF().apply {
            top =  + aroundPadding
            bottom = radius * 2
            left =  + aroundPadding
            right = radius * 2  + aroundPadding
        }

        paint.shader = null
        paint.color = arcDefaultColor
        canvas.drawArc(rectF, 180f, 180f, false, paint)

        val radio = score.toFloat() / FULL_SCORE
        paint.shader = gradient
        canvas.drawArc(rectF, 180f, 180f * radio, false, paint)

        var pointX = 0f
        var pointY = 0f
        val matrix = Matrix()
        matrix.postRotate(180 * radio, 0f, 0f)
        matrix.postTranslate(
            radius + aroundPadding ,
            radius + aroundPadding
        )
        val floatArray = FloatArray(2)
        matrix.mapPoints(floatArray, floatArrayOf(-radius, 0f))
        pointX = floatArray[0]
        pointY = floatArray[1]

        val outerRadius = 20f
        val innerRadius = 10f
        val gradient2 =
            LinearGradient(0f, pointY + outerRadius, 0f, pointY - outerRadius, startColor, endColor, Shader.TileMode.CLAMP)
        paint.shader = gradient2
        paint.style = Paint.Style.FILL
        canvas.drawCircle(pointX, pointY, outerRadius, paint)

        paint.shader = null
        paint.color = Color.WHITE
        canvas.drawCircle(pointX, pointY, innerRadius, paint)
    }

    /**
     * 绘制底部文字
     */
    private fun drawBottomText(canvas: Canvas) {
        configPaint()
        val rectF = RectF().apply {
            top = radius + bottomPadding + aroundPadding
            bottom = radius + bottomPadding + bottomHeight + aroundPadding
            left = aroundPadding
            right = bottomWidth + aroundPadding
        }
        canvas.drawRoundRect(rectF, bottomHeight / 2, bottomHeight / 2, paint)

        configTextPaint()
        val textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, 14f, resources.displayMetrics
        )
        textPaint.apply {
            this.textSize = textSize
            color = Color.WHITE
        }
        val measureText = textPaint.measureText(desc)
        canvas.drawText(
            desc,
            (bottomWidth - measureText) / 2 + aroundPadding,
            (radius + bottomPadding + bottomHeight) - (bottomHeight - textSize) / 4 * 3 + aroundPadding, // 高度修正
            textPaint
        )
    }

    private fun configPaint() {
        paint.reset()
        paint.apply {
            isAntiAlias = true
            style = Paint.Style.FILL

            val gradient = LinearGradient(
                0f,
                0f,
                bottomWidth,
                0f,
                startColor,
                endColor,
                Shader.TileMode.CLAMP
            )
            shader = gradient
        }
    }

    private fun configArcPaint() {
        paint.reset()
        paint.apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = arcWidth
            strokeCap = Paint.Cap.ROUND
        }
    }

    private fun configTextPaint() {
        textPaint.reset()
        textPaint.apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }
    }


}