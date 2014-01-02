package com.abplus.surroundcalc.models

import android.graphics.PointF
import android.graphics.RectF
import com.abplus.surroundcalc.models.Stroke.Side

/**
 * Created by kazhida on 2014/01/02.
 */
class Region(stroke: Stroke) : FreeHand(stroke) {

    private val stroke = stroke

    private val initialized = {(): Boolean ->
        path.close()
        true
    }()

    private fun insideBounds(p: PointF): Boolean {
        val bounds = RectF()
        path.computeBounds(bounds, true)
        return bounds.left <= p.x && p.x <= bounds.right && bounds.top <= p.y && p.y <= bounds.bottom;
    }

    public fun inside(p: PointF) : Boolean {
        if (insideBounds(p)) {
            var side = Side.UNKNOWN
            for (segment in stroke) {
                if (side == Side.UNKNOWN) {
                    side = segment.whichSide(p)
                } else {
                    val side2 = segment.whichSide(p)
                    if (side2 != Side.UNKNOWN && side2 != side) return false
                }
            }
            return true
        } else {
            return false
        }
    }
}