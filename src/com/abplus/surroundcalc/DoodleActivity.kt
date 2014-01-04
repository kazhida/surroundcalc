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
import android.widget.PopupWindow
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.view.View
import android.view.WindowManager.LayoutParams
import com.abplus.surroundcalc.models.Region
import com.abplus.surroundcalc.models.ValueLabel
import android.widget.TextView
import android.graphics.Rect
import android.util.Log

/**
 * Created by kazhida on 2014/01/02.
 */
class DoodleActivity : Activity() {

    var uninitializedDoodleView : DoodleView? = null
    val doodleView : DoodleView get() = uninitializedDoodleView!!
    var tenkey: PopupWindow? = null

    protected override fun onCreate(savedInstanceState: Bundle?) : Unit {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main)
        uninitializedDoodleView = (findViewById(R.id.doddle_view) as DoodleView)

        doodleView.setOnClickListener {
            val selected = doodleView.getSelected()
            if (selected is Region) {
                showMenu(selected)
            } else if (selected is ValueLabel) {
                showTenkey(selected)
            } else if (selected == null) {
                showTenkey(null)
            }
        }

        val tenkeyMask = findViewById(R.id.mask)
        tenkeyMask?.setOnClickListener {
            dismissTenkey()
        }

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

    private fun showMenu(region: Region): Unit {

    }

    private fun showTenkey(label: ValueLabel?): Unit {
        val mask = findViewById(R.id.mask)
        mask!!.setVisibility(View.VISIBLE)

        val view = getLayoutInflater().inflate(R.layout.tenkey, null, false);
        val wrap = ViewGroup.LayoutParams.WRAP_CONTENT

        view!!.setLayoutParams(ViewGroup.LayoutParams(wrap, wrap))
        tenkey = PopupWindow(view, wrap, wrap, false)
        tenkey!!.setOutsideTouchable(true)
        view.findViewById(R.id.key_0)?.setOnClickListener(NumberListener(0))
        view.findViewById(R.id.key_1)?.setOnClickListener(NumberListener(1))
        view.findViewById(R.id.key_2)?.setOnClickListener(NumberListener(2))
        view.findViewById(R.id.key_3)?.setOnClickListener(NumberListener(3))
        view.findViewById(R.id.key_4)?.setOnClickListener(NumberListener(4))
        view.findViewById(R.id.key_5)?.setOnClickListener(NumberListener(5))
        view.findViewById(R.id.key_6)?.setOnClickListener(NumberListener(6))
        view.findViewById(R.id.key_7)?.setOnClickListener(NumberListener(7))
        view.findViewById(R.id.key_8)?.setOnClickListener(NumberListener(8))
        view.findViewById(R.id.key_9)?.setOnClickListener(NumberListener(9))
        view.findViewById(R.id.key_dot)?.setOnClickListener(DotListener())
        view.findViewById(R.id.key_bs)?.setOnClickListener(ClearListener(false))
        view.findViewById(R.id.key_ca)?.setOnClickListener(ClearListener(true))
        view.findViewById(R.id.key_neg)?.setOnClickListener(NegativeListener())
        view.findViewById(R.id.key_ent)?.setOnClickListener {
            if (label != null) {
                label.value = getValue()
            } else {
                doodleView.addLabel(doodleView.getTapped(), getValue())
            }
            dismissTenkey()
        }

        val text = getValueText()

        if (label != null) {
            text?.setText(label.text)
        } else {
            text?.setText("0")
        }

        Log.d("surroundcalc", "H=" + heightInGlobal(doodleView) + " h=" + doodleView.getHeight())
        val w = doodleView.getWidth() - view.getWidth()
        val h = doodleView.getHeight() - view.getHeight()
        val x: Int = Math.min(doodleView.getTapped().x.toInt(), w)
        val y: Int = Math.min(doodleView.getTapped().y.toInt(), h)

        val offset = topInGlobal(doodleView)

        tenkey?.showAtLocation(doodleView, Gravity.LEFT + Gravity.TOP, x, y + offset)
    }

    private fun topInGlobal(view: View): Int {
        var rect = Rect()
        view.getGlobalVisibleRect(rect)
        return rect.top
    }

    private fun heightInGlobal(view: View): Int {
        var rect = Rect()
        view.getGlobalVisibleRect(rect)
        return rect.bottom - rect.top
    }

    private fun dismissTenkey() {
        tenkey?.dismiss()
        tenkey = null
        findViewById(R.id.mask)?.setVisibility(View.GONE)
        doodleView.redraw()
    }

    private fun getValueText(): TextView? {
        return tenkey?.getContentView()?.findViewById(R.id.value_text) as TextView?
    }

    private fun getValue(): Double {
        val v = getValueText()?.getText()?.toString()?.toDouble()
        return if (v == null)  0.0 else v
    }

    private inner class NumberListener(val n: Int): View.OnClickListener {
        override fun onClick(v: View) {
            val text = getValueText()
            val s = text?.getText()?.toString()
            if (s != null && s.equals("0")) {
                text?.setText(n.toString())
            } else if (text != null) {
                text.setText(text.getText().toString() + n.toString())
            }
        }
    }

    private inner class DotListener: View.OnClickListener {
        override fun onClick(v: View) {
            val text = getValueText()
            val s = text?.getText()?.toString()
            if (s == null || !s.contains(".")) {
                text?.setText(s + ".")
            }
        }
    }

    private inner class ClearListener(val all: Boolean): View.OnClickListener {
        override fun onClick(v: View) {
            val text = getValueText()
            if (all) {
                text?.setText("0")
            } else {
                var s = text?.getText()?.toString()
                if (s != null) {
                    if (s!!.length == 1) {
                        text?.setText("0")
                    } else {
                        s = s!!.substring(0, s!!.length - 1)
                        text?.setText(s)
                    }
                }
            }

        }
    }

    private inner class NegativeListener: View.OnClickListener {
        override fun onClick(v: View) {
            val text = getValueText()
            var s = text?.getText()?.toString()
            if (s != null && s!!.substring(0, 1).equals("-")) {
                s = s!!.substring(1, s!!.length)
            } else {
                s = "-" + s
            }
            text?.setText(s)
        }
    }
}