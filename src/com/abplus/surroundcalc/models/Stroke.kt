package com.abplus.surroundcalc.models

import android.graphics.PointF

/**
 * Created by kazhida on 2013/12/27.
 */
class Stroke(val point: PointF, val tail: Stroke? = null) {

    val x: Float get() = point.x
    val y: Float get() = point.y
}