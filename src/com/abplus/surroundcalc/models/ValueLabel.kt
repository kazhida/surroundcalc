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
    private var pickedPoint: PointF? = null

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

    override public fun picked(p: PointF) : Boolean {
        val b = bounds
        Log.d("surroundcalc", "TEXT=" + text + " RECT=(" + b.left + "," + b.top + ")-(" + b.right + "," + b.bottom + ") P=(" + p.x + "," + p.y + ")")


        pickedPoint = if (b.left <= p.x && p.x <= b.right && b.top <= p.y && p.y <= b.bottom) PointF(p.x, p.y) else null
        return pickedPoint != null
    }

    override public fun moveTo(p: PointF) : Unit {
        if (pickedPoint != null) {
            Log.d("surroundcalc", "p1=(" + p.x + "," + p.y + ") p1=(" + pickedPoint!!.x + "," + pickedPoint!!.y + ")")

            center.x += p.x - pickedPoint!!.x
            center.y += p.y - pickedPoint!!.y
        }
    }
}