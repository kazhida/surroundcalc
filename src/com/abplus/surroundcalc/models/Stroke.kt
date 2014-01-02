package com.abplus.surroundcalc.models

import android.graphics.PointF
import android.graphics.RectF

/**
 * Created by kazhida on 2013/12/27.
 */
class Stroke(val point: PointF, val tail: Stroke? = null): Iterable<Stroke> {

    enum class Side {
        UNKNOWN
        LEFT
        RIGHT
    }

    public val x: Float get() = point.x
    public val y: Float get() = point.y
    public fun isEmpty(): Boolean = (tail == null)

    public val origin: Stroke
        get() {
            for (stroke in this) {
                if (stroke.tail == null) return stroke
            }
            return this
        }

    public fun distance(other: Stroke): Float {
        val dx = x - other.x
        val dy = y - other.y
        return Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }

    public fun nearestDistance(other: Stroke, num: Int): Float {
        var min = java.lang.Float.MAX_VALUE
        var segment: Stroke? = this
        var count = num
        while (count > 0 && segment != null) {
            val d = segment!!.distance(other)
            if (d < min) min = d
            count--
            segment = segment!!.tail
        }
        return min
    }


    public val bounds: RectF
        get() {
            val rect = RectF()
            rect.top = y
            rect.left = x
            rect.right = x
            rect.bottom = y

            for (stroke in this) {
                if (rect.top > stroke.y) rect.top = stroke.y
                if (rect.left > stroke.x) rect.left = stroke.x
                if (rect.right < stroke.x) rect.right = stroke.x
                if (rect.bottom < stroke.y) rect.bottom = stroke.y
            }

            return rect
        }

    public override fun iterator(): Iterator<Stroke> {
        return StrokeIterator(this)
    }

    public class StrokeIterator(head: Stroke): Iterator<Stroke> {
        private var i = head

        public override fun next()  : Stroke {
            i = i.tail!!
            return i
        }
        public override fun hasNext() : Boolean {
            return i.tail != null
        }
    }

    public fun whichSide(p: PointF) : Side {
        if (tail == null) {
            return Side.UNKNOWN
        } else {
            val dx = (tail.x - x).toDouble()
            val dy = (tail.y - y).toDouble()
            val r = Math.sqrt(dx * dx + dy * dy)
            if (r == 0.0) {
                return Side.UNKNOWN
            } else {
                val s = dy / r
                val c = dx / r
                val y = -s * p.x + c * p.y
                if (y > 0) {
                    return Side.RIGHT
                } else if (y  < 0) {
                    return Side.LEFT
                } else {
                    return Side.UNKNOWN
                }
            }
        }
    }
}