package com.matrix.camera2.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Build
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import androidx.annotation.UiThread
import androidx.core.content.ContextCompat
import com.matrix.camera2.R
import com.matrix.camera2.bean.WaitingCircle
import com.matrix.camera2.bean.WaitingCircleEvaluator
import com.matrix.camera2.utils.LogUtil
import com.matrix.camera2.utils.PictureUtil
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt


/**
 *    author : xxd
 *    date   : 2020/6/28
 *    desc   : 封装动画的view
 */
class AISkinView : View {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context,
        attrs,
        0,
        0
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    /**
     * 当前的缩放倍数
     */
    private var currentScale = 1f

    /**
     * 画不断扩大圈的画笔
     */
    private val circlePaint = Paint()

    /**
     * 当前画面的矩阵
     */
    private val currentMatrix = Matrix()

    /**
     * 背景图，照片
     */
    private var backgroundBitmap: Bitmap? = null

    private var scheduledExecutorService: ScheduledExecutorService? = null

    init {
        // 初始化画笔
        circlePaint.apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = ContextCompat.getColor(context, R.color.gray)
        }
    }

    private fun initBitmap(path: String?) {
        if (TextUtils.isEmpty(path)) {
            // 初始化背景图，同时解决Android 5.0以上path图不能显示的问题
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                val vectorDrawable =
                    context.getDrawable(R.drawable.ic_test_fill)
                backgroundBitmap = Bitmap.createBitmap(
                    vectorDrawable!!.intrinsicWidth,
                    vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(backgroundBitmap!!)
                vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
                vectorDrawable.draw(canvas)
            } else {
                backgroundBitmap =
                    BitmapFactory.decodeResource(context.resources, R.drawable.ic_test_fill)
            }
            MIN_CIRCLE_X = backgroundBitmap!!.width * 0.2f
            MAX_CIRCLE_X = backgroundBitmap!!.width * 0.8f
            MIN_CIRCLE_Y = backgroundBitmap!!.height * 0.1f
            MAX_CIRCLE_Y = backgroundBitmap!!.height * 0.7f
        } else {
            backgroundBitmap = BitmapFactory.decodeFile(path)
            backgroundBitmap = PictureUtil.rotateBitmap(backgroundBitmap!!,PictureUtil.getRotateOrientation(path!!))
//            val rotateDegree = PictureUtil.getRotateDegree(path!!)
//            LogUtil.d("旋转了${rotateDegree}度")
//            backgroundBitmap = PictureUtil.rotate(
//                backgroundBitmap!!, rotateDegree, backgroundBitmap!!.width / 2f
//                , backgroundBitmap!!.height / 2f, true
//            )
            MIN_CIRCLE_X = backgroundBitmap!!.width * 0.2f
            MAX_CIRCLE_X = backgroundBitmap!!.width * 0.8f
            MIN_CIRCLE_Y = backgroundBitmap!!.height * 0.1f
            MAX_CIRCLE_Y = backgroundBitmap!!.height * 0.7f
        }
    }

    /**
     * view测量完毕才能调用此方法
     */
    fun initAfterMeasure(photoPath: String?) {
//        if (TextUtils.isEmpty(photoPath))
//            return
        post {
            initBitmap(photoPath)
            initMatrix()
            invalidate()
            startRandom()
        }
    }

    /**
     * 初始化矩阵，必须在拿到view和backgroundBitmap 宽高之后调用
     */
    private fun initMatrix() {
//        LogUtil.d("width=$width height=$height")
        if (width == 0 || height == 0 || backgroundBitmap == null)
            return

        val scaleX = width.toFloat() / backgroundBitmap!!.width
        val scaleY = height.toFloat() / backgroundBitmap!!.height

        currentScale = Math.min(scaleX, scaleY)

        currentMatrix.setScale(currentScale, currentScale, 0f, 0f)

        if (scaleX < scaleY)
            currentMatrix.postTranslate(
                0f,
                (height - backgroundBitmap!!.height * currentScale) / 2f
            )
        else
            currentMatrix.postTranslate((width - backgroundBitmap!!.width * currentScale) / 2f, 0f)
    }

    companion object {
        const val MIN_CIRCLE_RADIUS = 10f // 圆点半径的最小值
        const val MAX_CIRCLE_RADIUS = 30f // 圆点半径的最大值
        const val MIN_DURATION = 2000 // 最短持续时间
        const val MAX_DURATION = 4000 // 最长持续时间
        const val MIN_RANDOM_CIRCLE = 5 // 每次生成点的最小数目
        const val MAX_RANDOM_CIRCLE = 15 // 每次生成点的最大数据
        var MIN_CIRCLE_X = 0f
        var MAX_CIRCLE_X = 0f
        var MIN_CIRCLE_Y = 0f
        var MAX_CIRCLE_Y = 0f
    }

    private val circleList = mutableListOf<WaitingCircle>()

    /**
     * 开始生成随机数点
     */
    private fun startRandom() {

        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
        scheduledExecutorService!!.scheduleAtFixedRate({

            post {
                val randomAmount =
                    (Math.random() * (MAX_RANDOM_CIRCLE - MIN_RANDOM_CIRCLE)).toInt() + MIN_RANDOM_CIRCLE
                repeat(randomAmount) {
                    val circle = randomCircle()
                    circleList.add(circle)

                    val animator = ObjectAnimator.ofFloat(
                        circle,
                        "currentRadius",
                        circle.startRadius,
                        circle.endRadius,
                        circle.startRadius
                    )
                    animator.apply {
                        duration = circle.duration
                    }
                    animator.addUpdateListener {
                        postInvalidate()
                    }
                    animator.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            circleList.remove(circle)
                        }
                    })
                    animator.start()
                }
            }

        }, 0, 1, TimeUnit.SECONDS)

    }

    /**
     * 生成随机的圆圈
     */
    private fun randomCircle(): WaitingCircle {
        val radius =
            (MIN_CIRCLE_RADIUS + Math.random() * (MAX_CIRCLE_RADIUS - MIN_CIRCLE_RADIUS)).toFloat()
        val duration = (MIN_DURATION + Math.random() * (MAX_DURATION - MIN_DURATION)).toLong()
        val circleX = (MIN_CIRCLE_X + Math.random() * (MAX_CIRCLE_X - MIN_CIRCLE_X)).toFloat()
        val circleY = (MIN_CIRCLE_Y + Math.random() * (MAX_CIRCLE_Y - MIN_CIRCLE_Y)).toFloat()
        return WaitingCircle(circleX, circleY, 0f, radius / currentScale, duration)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.run {
            backgroundBitmap?.let {
                save()
                setMatrix(currentMatrix)
                drawBitmap(backgroundBitmap!!, 0f, 0f, circlePaint)
                for (circle in circleList) {
                    drawCircle(circle.circleX, circle.circleY, circle.currentRadius, circlePaint)
                }
                restore()
            }
        }
    }
}