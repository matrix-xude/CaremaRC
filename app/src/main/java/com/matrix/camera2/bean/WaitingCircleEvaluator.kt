package com.matrix.camera2.bean

import android.animation.TypeEvaluator

/**
 *    author : xxd
 *    date   : 2020/6/28
 *    desc   :
 */
class WaitingCircleEvaluator : TypeEvaluator<WaitingCircle> {

    override fun evaluate(
        fraction: Float,
        startValue: WaitingCircle?,
        endValue: WaitingCircle?
    ): WaitingCircle {
        return WaitingCircle(startValue!!.apply {
            currentRadius = startRadius + (endRadius - startRadius) * fraction
        })
    }
}