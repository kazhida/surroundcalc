package com.abplus.surroundcalc.models

import android.graphics.PointF
import android.graphics.RectF
import com.abplus.surroundcalc.models.Stroke.Side
import android.graphics.Matrix
import android.graphics.Path

/**
 * Created by kazhida on 2014/01/02.
 */
class Region(stroke: Stroke) : Pickable() {

    public val path: Path = {(stroke: Stroke): Path ->
        val path = Path();

        for (segment in stroke) {
            if (segment == stroke) {
                path.moveTo(segment.x, segment.y)
            } else {
                path.lineTo(segment.x, segment.y)
            }
        }
        path.close()
        path
    }(stroke)

    private val stroke = stroke
    private var pickedPoint: PointF? = null

    private fun insideBounds(p: PointF): Boolean {
        val bounds = RectF()
        path.computeBounds(bounds, true)
        return bounds.left <= p.x && p.x <= bounds.right && bounds.top <= p.y && p.y <= bounds.bottom;
    }

    override public fun picked(p: PointF) : Boolean {
        if (insideBounds(p)) {
            //            var side = Side.UNKNOWN
            //            for (segment in stroke) {
            //                if (side == Side.UNKNOWN) {
            //                    side = segment.whichSide(p)
            //                } else {
            //                    val side2 = segment.whichSide(p)
            //                    if (side2 != Side.UNKNOWN && side2 != side) return false
            //                }
            //            }
            pickedPoint = PointF(p.x, p.y)
            return true
        } else {
            return false
        }
    }

    override public fun moveTo(p: PointF) : Unit {
        if (pickedPoint != null) {
            val matrix = Matrix();
            matrix.setTranslate(p.x - pickedPoint!!.x, p.y - pickedPoint!!.y)
            path.transform(matrix)
        }
    }
}