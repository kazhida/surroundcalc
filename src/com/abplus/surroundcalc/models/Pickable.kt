package com.abplus.surroundcalc.models

import android.graphics.PointF

/**
 * Created by kazhida on 2014/01/04.
 */
abstract class Pickable {
    class object {
        var id_seed = 0;
    }
    public val id: Int = ++id_seed;
    protected val pickedPoint: PointF = PointF(0.0f, 0.0f)

    abstract protected fun isInside(p: PointF): Boolean

    public fun picked(p: PointF) : Boolean {
        if (isInside(p)) {
            pickedPoint.x = p.x
            pickedPoint.y = p.y
            return true
        } else {
            return false
        }
    }

    abstract public fun moveTo(p: PointF) : Unit
}
