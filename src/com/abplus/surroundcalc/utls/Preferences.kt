package com.abplus.surroundcalc.utls

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.abplus.surroundcalc.models.Drawing.KeyColor

/**
 * Created by kazhida on 2014/01/02.
 */
class Preferences(context: Context) {

    val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    private val CURRENT_COLOR = "CURRENT_COLOR"

    var currentColor: KeyColor?
        get() {
            val keyColor = preferences!!.getInt(CURRENT_COLOR, -1)
            return if (keyColor < 0) {
                null
            } else {
                KeyColor.values().get(keyColor)
            }
        }
        set(keyColor: KeyColor?) {
            val editor = preferences!!.edit()
            if (keyColor == null) {
                editor.remove(CURRENT_COLOR)
            } else {
                editor.putInt(CURRENT_COLOR, keyColor.ordinal())
            }
            editor.commit();
        }
}