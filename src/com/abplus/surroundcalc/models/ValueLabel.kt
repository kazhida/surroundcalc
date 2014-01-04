package com.abplus.surroundcalc.models

import android.graphics.PointF
import android.graphics.Paint
import java.nio.channels.FileLock
import android.graphics.Rect
import android.graphics.RectF

/**
 * Created by kazhida on 2014/01/04.
 */
class ValueLabel(center: PointF, val value: Double, val paint: Paint): Pickable {


    override var pickedPoint: PointF? = null
    private val text = value.toString()
    private var center = center

    public val centerPoint: PointF get() = center

    val bounds = {(): RectF ->
        val result = RectF()
        val rect = Rect()

        paint.getTextBounds(text, 0, text.length, rect)

        val w = rect.right - rect.left
        val h = rect.bottom - rect.top

        result.top = center.y - h / 2
        result.left = center.x - w / 2
        result.right = center.x - w /2
        result.bottom = center.y - h / 2

        result
    }()

    protected override fun inside(p: PointF) : Boolean {
        return bounds.left <= p.x && p.x <= bounds.right && bounds.top <= p.y && p.y <= bounds.bottom
    }

    public override fun moveTo(p: PointF) : Unit {
        if (pickedPoint != null) {
            center.x += p.x - pickedPoint!!.x
            center.y += p.y - pickedPoint!!.y
        }
    }
}