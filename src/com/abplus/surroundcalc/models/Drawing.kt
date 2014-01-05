package com.abplus.surroundcalc.models

import java.util.ArrayList
import android.util.Log
import android.graphics.PointF
import android.graphics.Paint

/**
 * Created by kazhida on 2013/12/27.
 */

class Drawing private (val keyColor: Drawing.KeyColor) {

    enum class KeyColor {
        BLUE
        GREEN
        RED
        YELLOW
    }

    public trait KeyHolder {
        fun get(color: KeyColor): Drawing
    }

    public val freeHands: MutableList<FreeHand> = ArrayList<FreeHand>()
    public val regions: MutableList<Region> = ArrayList<Region>()
    public val valueLabels: MutableList<ValueLabel> = ArrayList<ValueLabel>()

    fun detect(stroke: Stroke): Unit {
        //  todo: いろいろ検出


        Log.d("SurroundCALC", "Start: (" + stroke.x + ", " + stroke.y)
        val origin = stroke.origin
        Log.d("SurroundCALC", "Origin: (" + origin.x + ", " + origin.y)
        val bounds = stroke.bounds
        val width = bounds.right - bounds.left
        val height = bounds.bottom - bounds.top
        val limit = if (width > height) {
            height / 4
        } else {
            width / 4
        }
        Log.d("SurroundCALC", "Bounds = (" + bounds.left + ", " + bounds.top + ")-(" + bounds.right + "," + bounds.bottom)
        Log.d("SurroundCALC", "Limit = " + limit)

        val distance = stroke.nearestDistance(origin, 3)
        Log.d("SurroundCALC", "Distance = " + distance)

        if (stroke.distance(origin) < limit) {
            regions.add(Region(stroke).bind(valueLabels))
        } else {
            freeHands.add(FreeHand(stroke))
        }
    }

    public fun pick(p: PointF): Pickable? {
        for (label in valueLabels) {
            if (label.picked(p)) return label
        }
        for (region in regions) {
            if (region.picked(p)) return region
        }
        return null
    }

    public fun addLabel(p: PointF, value: Double, paint: Paint) : Unit {
        valueLabels.add(ValueLabel(p, value, paint).bind(regions))
    }

    public fun clear(): Unit {
        freeHands.clear()
        regions.clear()
    }

    public fun unbind(picked: Pickable) {
        if (picked is Region) {
            picked.unbind()
        }
        if (picked is ValueLabel) {
            picked.unbind(regions)
        }
    }

    public fun bind(picked: Pickable) {
        if (picked is Region) {
            picked.bind(valueLabels)
        }
        if (picked is ValueLabel) {
            picked.bind(regions)
        }
    }

    public fun undo() : Unit {
        freeHands.remove(freeHands.size() - 1)
    }

    public class object {

        private val drawings = { (): List<Drawing> ->
            val builder = ImmutableArrayListBuilder<Drawing>()
            builder.add(Drawing(KeyColor.BLUE))
            builder.add(Drawing(KeyColor.GREEN))
            builder.add(Drawing(KeyColor.RED))
            builder.add(Drawing(KeyColor.YELLOW))
            builder.build()
        }()


        val holder = object: KeyHolder {
            override fun get(color: KeyColor): Drawing {
                return drawings.get(color.ordinal())
            }
        }
    }
}