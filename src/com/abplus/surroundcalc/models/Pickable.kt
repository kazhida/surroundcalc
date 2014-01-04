package com.abplus.surroundcalc.models

import android.graphics.PointF

/**
 * Created by kazhida on 2014/01/04.
 */
trait Pickable {

    protected var pickedPoint: PointF?

    public fun picked(p: PointF) : Boolean {
        pickedPoint = if (inside(p)) {
            p
        } else {
            null
        }
        return pickedPoint != null
    }

    protected fun inside(p: PointF) : Boolean
    public fun moveTo(p: PointF) : Unit
}