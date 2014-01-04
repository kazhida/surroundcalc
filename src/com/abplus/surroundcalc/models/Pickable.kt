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

    abstract public fun picked(p: PointF) : Boolean
    abstract public fun moveTo(p: PointF) : Unit
}