package com.abplus.surroundcalc

import android.app.Activity
import android.app.ActionBar
import com.abplus.surroundcalc.models.Drawing
import com.abplus.surroundcalc.utls.Preferences
import com.abplus.surroundcalc.models.Drawing.KeyColor
import android.view.Menu
import android.view.MenuItem
import android.app.FragmentTransaction
import android.os.Bundle

/**
 * Created by kazhida on 2014/01/02.
 */
class DoodleActivity : Activity() {

    var uninitializedDoodleView : DoodleView? = null
    val doodleView : DoodleView get() = uninitializedDoodleView!!

    protected override fun onCreate(savedInstanceState: Bundle?) : Unit {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main)
        uninitializedDoodleView = (findViewById(R.id.doddle_view) as DoodleView)

        val actionBar = getActionBar()!!
        addTab(actionBar, Drawing.KeyColor.BLUE, true)
        addTab(actionBar, Drawing.KeyColor.GREEN, false)
        addTab(actionBar, Drawing.KeyColor.RED, false)
        addTab(actionBar, Drawing.KeyColor.YELLOW, false)
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS)
    }

    private fun addTab(actionBar : ActionBar, keyColor : Drawing.KeyColor, selected : Boolean) : Unit {
        val tab = actionBar.newTab()
        val resId = when (keyColor) {
            Drawing.KeyColor.BLUE -> {
                R.string.blue
            }
            Drawing.KeyColor.GREEN -> {
                R.string.green
            }
            Drawing.KeyColor.RED -> {
                R.string.red
            }
            Drawing.KeyColor.YELLOW -> {
                R.string.yellow
            }
        }
        val drawing = Drawing.holder.get(keyColor)
        tab.setText(resId)
        tab.setTabListener(TabListener())
        tab.setTag(drawing)
        actionBar.addTab(tab, selected)

        if (selected) {
            doodleView.setDrawing(drawing)
        }

    }
    public override fun onResume() : Unit {
        super.onResume()

        val keyColor = Preferences(this).currentColor

        if (keyColor != null) {
            val actionBar = getActionBar()!!
            val tab = actionBar.getTabAt(keyColor.ordinal());
            actionBar.selectTab(tab)
        }
    }

    public override fun onPause() : Unit {
        super.onPause()
        Preferences(this).currentColor = doodleView.getDrawing()?.keyColor
    }

    public override fun onCreateOptionsMenu(menu : Menu?) : Boolean {
        val inflater = getMenuInflater()
        inflater.inflate(R.menu.actions, menu)
        return super.onCreateOptionsMenu(menu)
    }

    public override fun onOptionsItemSelected(item : MenuItem?) : Boolean {
        return when (item?.getItemId()) {
            R.id.action_clear_drawing -> {
                doodleView.clear()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private inner class TabListener: ActionBar.TabListener {
        override fun onTabSelected(tab: ActionBar.Tab?, ft: FragmentTransaction?) {
            val drawing = (tab?.getTag() as Drawing)
            doodleView.setDrawing(drawing)
        }
        override fun onTabUnselected(tab: ActionBar.Tab?, ft: FragmentTransaction?) {}
        override fun onTabReselected(tab: ActionBar.Tab?, ft: FragmentTransaction?) {}
    }
}