package com.matrix.camera2.bean

/**
 *    author : xxd
 *    date   : 2020/6/28
 *    desc   : 持续出现的圆环
 */
class WaitingCircle(
    var circleX: Float, // 圆心x轴坐标
    var circleY: Float, // 圆心y轴坐标
    var startRadius: Float,  // 圆最小半径
    var endRadius: Float,  // 圆最大半径
    var duration: Long  // 持续时间
) {
    constructor(waitingCircle: WaitingCircle) : this(
        waitingCircle.circleX,
        waitingCircle.circleY,
        waitingCircle.startRadius,
        waitingCircle.endRadius,
        waitingCircle.duration
    )

    var currentRadius: Float = 0f // 当前的半径
    var isFinish = false // 动画是否结束

}