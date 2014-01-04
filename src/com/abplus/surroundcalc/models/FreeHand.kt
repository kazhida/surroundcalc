package com.abplus.surroundcalc.models

import android.graphics.Path

/**
 * Created by kazhida on 2013/12/27.
 */
class FreeHand(stroke: Stroke) {

    public val path: Path = {(stroke: Stroke): Path ->
        val path = Path();

        for (segment in stroke) {
            if (segment == stroke) {
                path.moveTo(segment.x, segment.y)
            } else {
                path.lineTo(segment.x, segment.y)
            }
        }
        path
    }(stroke)
}