package com.abplus.surroundcalc.models

import java.util.ArrayList
import android.util.Log

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

    val freeHands: MutableList<FreeHand> = ArrayList<FreeHand>()
    val regions: MutableList<Region> = ArrayList<Region>()

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
            Log.d("SurroundCALC", "Region")
            regions.add(Region(stroke))
        } else {
            Log.d("SurroundCALC", "FreeHand")
            freeHands.add(FreeHand(stroke))
        }
    }

    fun clear(): Unit {
        freeHands.clear()
        regions.clear()
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