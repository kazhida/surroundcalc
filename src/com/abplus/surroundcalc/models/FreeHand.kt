package com.abplus.surroundcalc.models

import android.graphics.Path

/**
 * Created by kazhida on 2013/12/27.
 */
open class FreeHand(stroke: Stroke) {

    public val path: Path = buildPath(stroke)

    private fun buildPath(stroke: Stroke): Path {
        val path = Path();
        path.moveTo(stroke.x, stroke.y);

        var s = stroke.tail;
        while (s != null) {
            path.lineTo(s!!.x, s!!.y);
            s = s!!.tail;
        }

        return path;
    }
}