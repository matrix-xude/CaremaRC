package com.matrix.camera2.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

/**
 *    author : xxd
 *    date   : 2020/7/14
 *    desc   : 测肤折线图的view
 */
class SkinView : View {


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
        // 以下单位都是dp,使用时会转化
        const val LEFT_PADDING = 20f  // 表格距离左边的间距,用来绘制文字，滑动的最大限度
        const val RIGHT_PADDING = 20f  // 表格距离右边的间距,用来绘制文字，滑动的最大限度
        const val TOP_PADDING = 25f  // 表格距离右边的间距,用来绘制文字
        const val BOTTOM_PADDING = 30f  // 表格距离右边的间距,用来绘制文字

        const val LANDSCAPE_INTERVAL = 50f // 横向每个间隔
        const val PORTRAIT_INTERVAL = 30f // 纵向每个间隔

        const val PER_INTERVAL_SCORE = 33.33f // 竖向每个间隔的分数

        const val LINE_COLOR = "#8D66FF"

        const val OUTER_CIRCLE_RADIUS = 15f  // 数据点的外圆半径
        const val INNER_CIRCLE_RADIUS = 8f // 数据点的内圆半径
    }

    // 以下6个参数先不对外提供赋值，如果需要可以修改
    private var landscapeInterval = 0f
    private var portraitInterval = 0f
    private var leftPadding = 0f
    private var rightPadding = 0f
    private var topPadding = 0f
    private var bottomPadding = 0f

    /**
     * 所有的数据
     */
    private var list = mutableListOf<SkinViewBean>()
    private val paint = Paint() // 画笔
    private var sumWidth = 0f // 所有的宽度
    private var sumHeight = 0f // 所有的高度度
    private var tableWidth = 0f // 表格的宽度
    private var tableHeight = 0f // 表格的高度
    private var viewWidth = 0 // view的宽度
    private var viewHeight = 0 // view的高度
    private var heightCount = 3 // 应该绘制的高度个数
    private var initDataSuccess = false // 是否初始化数据成功
    private var perIntervalScore = 0f // 每个竖向间隔表示多少分
    private var lineColor = 0

    private var lastX = 0f
    private val currentMatrix = Matrix()
    private var currentIndex = -1

    init {
        configParam()

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        // 此view高度是定死的，如果需要控制，在view外部包裹viewGroup
        val height = portraitInterval * heightCount + topPadding + bottomPadding
        setMeasuredDimension(width, height.toInt())
    }

    /**
     * 配置绘制的参数
     */
    private fun configParam() {
        landscapeInterval = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, LANDSCAPE_INTERVAL, resources.displayMetrics
        )
        portraitInterval = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, PORTRAIT_INTERVAL, resources.displayMetrics
        )
        leftPadding = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, LEFT_PADDING, resources.displayMetrics
        )
        rightPadding = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, RIGHT_PADDING, resources.displayMetrics
        )
        topPadding = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, TOP_PADDING, resources.displayMetrics
        )
        bottomPadding = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, BOTTOM_PADDING, resources.displayMetrics
        )
    }


    /**
     * 绑定绘图的数据
     */
    fun bindData(list: List<SkinViewBean>?) {
        initDataSuccess = false
        currentMatrix.reset()
        currentIndex = -1
        this.list.clear()
        if (list != null) {
            this.list.addAll(list)
        }
        visibility = if (this.list.size > 1) VISIBLE else GONE
        initData()
    }

    /**
     * 滑动到指定位置
     */
    fun scrollToIndex(position: Int) {
        if (!initDataSuccess || position >= list.size)
            return
        currentIndex = position
        val targetX = viewWidth / 2  // 想要滑动指定条目到达的位置
        val indexLeft = leftPadding + currentIndex * landscapeInterval  // 图像上距离左边的总长度
        if (indexLeft >= targetX) {  // 可以往左滑动
            var scrollX = targetX - indexLeft // 滑动的距离
            val indexRight =
                (list.size - 1 - currentIndex) * landscapeInterval + rightPadding // 图像上距离右边的总长度
            if (indexRight < viewWidth - targetX) { // 右边长度不够时，需要修正左滑的距离
                scrollX += viewWidth - targetX - indexRight
            }
            currentMatrix.reset()
            currentMatrix.postTranslate(scrollX, 0f)
        } else {  // 左边长度不足，不应滑动
            currentMatrix.reset()
        }
        invalidate()
    }

    /**
     * 初始化数据，计算数据的总（宽、高）等一些数据
     */
    private fun initData() {
        tableWidth = landscapeInterval * (list.size - 1)
        tableHeight = portraitInterval * heightCount
        sumWidth = leftPadding + rightPadding + tableWidth
        sumHeight = topPadding + bottomPadding + tableHeight

        perIntervalScore = PER_INTERVAL_SCORE
        lineColor = Color.parseColor(LINE_COLOR)

        post {
            viewWidth = width
            viewHeight = height
            initDataSuccess = true
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (initDataSuccess && list.size > 1 && canvas != null) {
            canvas.save()
            canvas.setMatrix(currentMatrix)
            drawDashPath(canvas)
            drawLinePath(canvas)
            drawGradient(canvas)
            drawCircle(canvas)
            drawText(canvas)
            canvas.restore()
        }
    }

    /**
     * 绘制文字部分
     */
    private fun drawText(canvas: Canvas) {
        val topPaint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
            style = Paint.Style.FILL
            textSize =
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 8f, resources.displayMetrics)
        }
        val bottomPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLUE
            style = Paint.Style.FILL
            textSize =
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10f, resources.displayMetrics)
        }
        val bottomPaint2 = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#999999")
            style = Paint.Style.FILL
            textSize =
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 8f, resources.displayMetrics)
        }
        val backgroundPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            style = Paint.Style.FILL
        }

        // 基线的Y轴坐标
        val baseLineY = topPadding + heightCount * portraitInterval
        for (index in list.indices) {
            // 分数与每个间隔的比例
            val bean = list[index]
            // 顶部文字
            if (!TextUtils.isEmpty(bean.topDesc)) {

                val textWidth = topPaint.measureText(bean.topDesc) // 文字的宽度
                val ratio = bean.score / perIntervalScore

                val frameWidth = 8 // 文字背景上下左右外边框的距离
                val pointY = baseLineY - (ratio * portraitInterval)  // 点圆心的位置
                val topOffset = 40 // 文字底部距圆心的偏移量

                val rectF = RectF().apply {
                    left = leftPadding + index * landscapeInterval - textWidth / 2 - frameWidth
                    right = leftPadding + index * landscapeInterval + textWidth / 2 + frameWidth
                    bottom = pointY - topOffset + frameWidth
                    // 文字绘制的上部空白比较多，frameWidth 边距上部 /2
                    top = pointY - topOffset - topPaint.textSize - frameWidth / 2
                }
                canvas.drawRoundRect(rectF, 8f, 8f, backgroundPaint)

                val arrowWidth = 10f
                val arrowHeight = 10f
                val path = Path().apply {
                    moveTo(
                        leftPadding + index * landscapeInterval - arrowWidth / 2,
                        pointY - topOffset + frameWidth
                    )
                    rLineTo(arrowWidth, 0f)
                    rLineTo(-arrowWidth / 2, arrowHeight)
                    close()
                }
                canvas.drawPath(path, backgroundPaint)

                canvas.drawText(
                    bean.topDesc!!,
                    leftPadding + index * landscapeInterval - textWidth / 2,
                    pointY - topOffset,
                    topPaint
                )
            }
            // 底部文字
            if (!TextUtils.isEmpty(bean.bottomDesc)) {
                val textWidth = bottomPaint.measureText(bean.bottomDesc)
                bottomPaint.color = if (index != currentIndex) Color.BLACK else lineColor
                canvas.drawText(
                    bean.bottomDesc!!,
                    leftPadding + index * landscapeInterval - textWidth / 2,
                    baseLineY + 30,
                    bottomPaint
                )
            }
            // 底部文字2
            if (!TextUtils.isEmpty(bean.bottomDesc2)) {
                val textWidth = bottomPaint2.measureText(bean.bottomDesc2)
                bottomPaint2.color =
                    if (index != currentIndex) Color.parseColor("#999999") else lineColor
                canvas.drawText(
                    bean.bottomDesc2!!,
                    leftPadding + index * landscapeInterval - textWidth / 2,
                    baseLineY + 30 + bottomPaint.textSize,
                    bottomPaint2
                )
            }
        }
    }

    /**
     * 画数据圆点
     */
    private fun drawCircle(canvas: Canvas) {

        configCirclePaint()
        // 基线的Y轴坐标
        val baseLineY = topPadding + heightCount * portraitInterval
        repeat(list.size) {
            // 分数与每个间隔的比例
            val ratio = list[it].score / perIntervalScore

            // 外圆
            paint.color = lineColor
            canvas.drawCircle(
                leftPadding + it * landscapeInterval, baseLineY - (ratio * portraitInterval),
                OUTER_CIRCLE_RADIUS, paint
            )
            // 内圆
            paint.color = Color.WHITE
            canvas.drawCircle(
                leftPadding + it * landscapeInterval, baseLineY - (ratio * portraitInterval),
                INNER_CIRCLE_RADIUS, paint
            )
        }
    }

    /**
     * 绘制渐变的背景颜色
     */
    private fun drawGradient(canvas: Canvas) {

        configGradientPaint()
        val path = Path()
        // 基线的Y轴坐标
        val baseLineY = topPadding + heightCount * portraitInterval
        // 上部数据线
        repeat(list.size) {
            // 分数与每个间隔的比例
            val ratio = list[it].score / perIntervalScore.toFloat()
            if (it == 0) {
                path.moveTo(
                    leftPadding + it * landscapeInterval,
                    baseLineY - (ratio * portraitInterval)
                )
            } else {
                path.lineTo(
                    leftPadding + it * landscapeInterval,
                    baseLineY - (ratio * portraitInterval)
                )
            }
        }
        path.lineTo(leftPadding + tableWidth, topPadding + tableHeight)
        path.lineTo(leftPadding, topPadding + tableHeight)
        path.close()
        canvas.drawPath(path, paint)
    }

    /**
     * 画实线
     * 1. data数据线
     * 2. 底部实线
     */
    private fun drawLinePath(canvas: Canvas) {
        configLinePaint()
        // 底部实线
        val path = Path().apply {
            moveTo(leftPadding, topPadding + heightCount * portraitInterval)
            rLineTo(tableWidth, 0f)
        }
        canvas.drawPath(path, paint)

        val pathData = Path()
        // 基线的Y轴坐标
        val baseLineY = topPadding + heightCount * portraitInterval
        // 上部数据线
        repeat(list.size) {
            // 分数与每个间隔的比例
            val ratio = list[it].score / perIntervalScore
            if (it == 0) {
                pathData.moveTo(
                    leftPadding + it * landscapeInterval,
                    baseLineY - (ratio * portraitInterval)
                )
            } else {
                pathData.lineTo(
                    leftPadding + it * landscapeInterval,
                    baseLineY - (ratio * portraitInterval)
                )
            }
        }
        canvas.drawPath(pathData, paint)

    }

    /**
     * 画虚线轮廓
     */
    private fun drawDashPath(canvas: Canvas) {
        configDashPaint()
        repeat(heightCount) {
            val path = Path().apply {
                moveTo(leftPadding, topPadding + (it + 1) * portraitInterval)
                rLineTo(tableWidth, 0f)
            }
            canvas.drawPath(path, paint)
        }
        repeat(list.size) {
            val path = Path().apply {
                moveTo(leftPadding + it * landscapeInterval, topPadding)
                rLineTo(0f, tableHeight)
            }
            canvas.drawPath(path, paint)
        }
    }

    /**
     * 配置虚线画笔
     */
    private fun configDashPaint() {
        paint.reset()
        paint.apply {
            isAntiAlias = true
            color = lineColor
            style = Paint.Style.STROKE
            strokeWidth = 1f
            pathEffect = DashPathEffect(floatArrayOf(4f, 4f), 0f)
        }
    }

    /**
     * 配置实线画笔
     */
    private fun configLinePaint() {
        paint.reset()
        paint.apply {
            isAntiAlias = true
            color = lineColor
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
    }

    /**
     * 配置实线画笔
     */
    private fun configCirclePaint() {
        paint.reset()
        paint.apply {
            isAntiAlias = true
            color = lineColor
            style = Paint.Style.FILL
        }
    }

    /**
     * 配置渐变的画笔
     */
    private fun configGradientPaint() {
        paint.reset()
        paint.apply {
            isAntiAlias = true
            color = lineColor
            style = Paint.Style.FILL

            val gradient = LinearGradient(
                0f,
                0f,
                0f,
                sumHeight,
                Color.parseColor("#558D66FF"),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
            shader = gradient
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastX = event.x
                }
                MotionEvent.ACTION_MOVE -> {
                    if (checkBoundary(event.x - lastX)) {
                        currentMatrix.postTranslate(event.x - lastX, 0f)
                        lastX = event.x
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    lastX = 0f
                }
            }
            invalidate()
        }
        return true
    }

    /**
     * 检测是否移动超过边界
     * @param diffX <0 左移  >0 右移
     */
    private fun checkBoundary(diffX: Float): Boolean {
        return if (diffX < 0) { // 左移，检测右边界
            val dst = FloatArray(2)
            currentMatrix.mapPoints(dst, floatArrayOf(sumWidth, 0f))
            dst[0] > viewWidth
        } else {// 右移，检测左边界
            val dst = FloatArray(2)
            currentMatrix.mapPoints(dst, floatArrayOf(0f, 0f))
            dst[0] <= 0
        }
    }

    /**
     * 图案需要配置的数据填充类
     */
    class SkinViewBean {
        /**
         * 分数
         */
        var score: Int = 0

        /**
         * 数据上面的描述字段
         */
        var topDesc: String? = null

        /**
         * 数据下部的描述字段
         */
        var bottomDesc: String? = null

        /**
         * 数据下部的描述文字2
         */
        var bottomDesc2: String? = null
    }
}