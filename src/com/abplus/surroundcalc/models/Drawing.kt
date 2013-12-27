package com.abplus.surroundcalc.models

import java.util.ArrayList
import com.abplus.surroundcalc.models.Drawing.KeyColor

/**
 * Created by kazhida on 2013/12/27.
 */
class Drawing(val keyColor: KeyColor) {

    enum class KeyColor {
        BLUE
        GREEN
        RED
        YELLOW
    }

    val freeHands: MutableList<FreeHand> = ArrayList<FreeHand>();

    fun detect(stroke: Stroke): Unit {
        //  todo: いろいろ検出


        freeHands.add(FreeHand(stroke));
    }

    fun clear(): Unit {
        freeHands.clear()
    }
}