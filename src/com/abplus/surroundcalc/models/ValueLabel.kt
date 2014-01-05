package com.abplus.surroundcalc.models

import android.graphics.PointF
import android.graphics.Paint
import java.nio.channels.FileLock
import android.graphics.Rect
import android.graphics.RectF
import android.util.Log

/**
 * Created by kazhida on 2014/01/04.
 */
class ValueLabel(center: PointF, value: Double, val paint: Paint): Pickable() {

    private var center = center

    public var value: Double = value
    public val text: String get() = value.toString()
    public val centerPoint: PointF get() = center

    val bounds: RectF
        get() {
            val result = RectF()
            val rect = Rect()

            paint.getTextBounds(text, 0, text.length, rect)

            val w = rect.right - rect.left
            val h = rect.bottom - rect.top

            result.top = center.y - h / 2
            result.left = center.x - w / 2
            result.right = center.x + w /2
            result.bottom = center.y + h / 2

            return result
        }


    override protected fun isInside(p: PointF): Boolean {
        val b = bounds
        return b.left <= p.x && p.x <= b.right && b.top <= p.y && p.y <= b.bottom
    }

    override public fun moveTo(p: PointF) : Unit {
        center.x += p.x - pickedPoint.x
        center.y += p.y - pickedPoint.y
    }

    public fun unbind(regions: List<Region>): ValueLabel {
        for (region in regions) region.unbind(this)
        return this
    }

    public fun bind(regions: List<Region>): ValueLabel {
        for (region in regions) region.bind(this)
        return this
    }
}