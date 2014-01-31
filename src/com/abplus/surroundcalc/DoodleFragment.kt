package com.abplus.surroundcalc

import android.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import android.widget.PopupWindow
import com.google.ads.AdView
import com.google.ads.InterstitialAd
import android.os.Handler
import com.abplus.surroundcalc.utls.Purchases
import com.abplus.surroundcalc.models.Region
import com.abplus.surroundcalc.models.ValueLabel
import com.google.ads.AdSize
import android.widget.RelativeLayout
import android.widget.FrameLayout
import com.google.ads.AdRequest
import com.google.ads.AdListener
import com.google.ads.Ad
import android.util.Log
import android.graphics.PointF
import android.widget.TextView
import com.abplus.surroundcalc.utls.Preferences
import com.abplus.surroundcalc.billing.BillingHelper
import android.widget.Toast
import android.content.Context
import com.abplus.surroundcalc.models.Drawing
import com.u1aryz.android.lib.newpopupmenu.PopupMenu

/**
 * Created by kazhida on 2014/01/24.
 */
class DoodleFragment(val keyColor: Drawing.KeyColor): Fragment() {

    private var doodleView: DoodleView? = null
    private var tenkey: PopupWindow? = null
    private val handler = Handler()
    private val context: Context get() = getActivity()!!
    public val mainView: DoodleView get() = doodleView!!

    public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    public override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        doodleView = (getView()?.findViewById(R.id.doddle_view) as DoodleView)
        doodleView?.setDrawing(Drawing.holder.get(keyColor))

        doodleView?.setOnClickListener {
            val selected = doodleView!!.getPicked()
            if (selected is Region) {
                showMenu(selected)
            } else if (selected is ValueLabel) {
                showTenkey(selected)
            } else if (selected == null) {
                showTenkey(null)
            }
        }
    }

    private fun showMenu(region: Region): Unit {
        val anchor = getView()!!.findViewById(R.id.popup_anchor)!!
        val menu = when (region.labels.size()) {
            1 -> createMenu1(region.labels.get(0))
            2 -> createMenu2(region.labels.get(0), region.labels.get(1))
            else -> createMenuN(region.labels)
        }

        setViewMargin(anchor, doodleView!!.getTapped())
        menu.show(anchor)
    }

    private fun setViewMargin(view: View, p: PointF) {
        val x: Int = p.x.toInt()
        var y: Int = p.y.toInt()
        val params = RelativeLayout.LayoutParams(view.getLayoutParams()!!)

        params.setMargins(x, y, 0, 0)

        view.setLayoutParams(params)
        view.invalidate()
    }

    private fun initMenuItem(menu: PopupMenu, id: Int, value: Double, iconId: Int) {
//        val item = menu.findItem(id)
//        item?.setTitle(prefix + ":  " + value.toString())
        menu.add(id, value.toString())?.setIcon(getResources()!!.getDrawable(iconId))
    }

    private fun addResult(value: Double) : Boolean {
        doodleView!!.addResultLabel(doodleView!!.getTapped(), value)
        doodleView!!.redraw()
        return true
    }

    private fun createMenu1(value: ValueLabel): PopupMenu {
        val popupMenu = PopupMenu(context)

        val neg = -value.value
        val x2 = value.value * value.value
        val sqrt = Math.sqrt(value.value)

        initMenuItem(popupMenu, R.id.ope_neg,     neg,  R.drawable.ope_sub)
        initMenuItem(popupMenu, R.id.func_square, x2,   R.drawable.func_square)
        initMenuItem(popupMenu, R.id.func_sqrt,   sqrt, R.drawable.func_sqrt)

        popupMenu.setOnItemSelectedListener {
            when (it?.getItemId()) {
                R.id.ope_neg     -> addResult(neg)
                R.id.func_square -> addResult(x2)
                R.id.func_sqrt   -> addResult(sqrt)
                else -> false
            }
        }

        return popupMenu
    }

    private fun createMenu2(value1: ValueLabel, value2: ValueLabel): PopupMenu {
        val popupMenu = PopupMenu(context)

        val dx = value2.centerPoint.x - value1.centerPoint.x
        val dy = value1.centerPoint.y - value2.centerPoint.y

        Log.d("surroundcalc", "v1=" +  value1.value + " v2=" + value2.value)
        val v1 = if (dy > dx) value2 else value1
        val v2 = if (dy > dx) value1 else value2
        Log.d("surroundcalc", "v1=" +  v1.value + " v2=" + v2.value)

        val add = v1.value + v2.value
        val sub = v1.value - v2.value
        val mul = v1.value * v2.value
        val div = v1.value / v2.value
        val pow = Math.pow(v1.value, v2.value)

        initMenuItem(popupMenu, R.id.ope_add,  add, R.drawable.ope_add)
        initMenuItem(popupMenu, R.id.ope_sub,  sub, R.drawable.ope_sub)
        initMenuItem(popupMenu, R.id.ope_mul,  mul, R.drawable.ope_mul)
        initMenuItem(popupMenu, R.id.ope_div,  div, R.drawable.ope_div)
        initMenuItem(popupMenu, R.id.func_pow, pow, R.drawable.func_pow)

        popupMenu.setOnItemSelectedListener {
            when (it?.getItemId()) {
                R.id.ope_add  -> addResult(add)
                R.id.ope_sub  -> addResult(sub)
                R.id.ope_mul  -> addResult(mul)
                R.id.ope_div  -> addResult(div)
                R.id.func_pow -> addResult(pow)
                else -> false
            }
        }

        return popupMenu
    }

    private fun createMenuN(labels: List<ValueLabel>): PopupMenu {
        val popupMenu = PopupMenu(context)

        var sum = 0.0
        var sum2 = 0.0
        var count = 0
        for (label in labels) {
            sum += label.value
            sum2 += label.value * label.value
            count++
        }
        val avg = sum / count
        val sn = sum2 / count - avg * avg
        val sn1 = sn * count / (count - 1)

        initMenuItem(popupMenu, R.id.func_sum,  sum, R.drawable.func_sum)
        initMenuItem(popupMenu, R.id.func_varp, sn,  R.drawable.func_varp)  // "σ/n"
        initMenuItem(popupMenu, R.id.func_vars, sn1, R.drawable.func_vars)  // "σ/n-1"

        popupMenu.setOnItemSelectedListener {
            when (it?.getItemId()) {
                R.id.func_sum  -> addResult(sum)
                R.id.func_varp -> addResult(sn)
                R.id.func_vars -> addResult(sn1)
                else -> false
            }
        }

        return popupMenu
    }

    private fun showTenkey(label: ValueLabel?): Unit {
        val view = getActivity()!!.getLayoutInflater().inflate(R.layout.tenkey, null, false)!!
        val wrap = ViewGroup.LayoutParams.WRAP_CONTENT

        tenkey = PopupWindow(context)
        tenkey!!.setWindowLayoutMode(wrap, wrap)
        tenkey!!.setContentView(view)
        tenkey!!.setOutsideTouchable(true)
        tenkey!!.setFocusable(true)

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
        view.findViewById(R.id.key_bs)?.setOnClickListener(BackSpaceListener())
        view.findViewById(R.id.key_neg)?.setOnClickListener(NegativeListener())
        view.findViewById(R.id.key_ent)?.setOnClickListener {
            if (label != null) {
                label.value = getValue()
            } else {
                doodleView!!.addLabel(doodleView!!.getTapped(), getValue())
            }
            tenkey?.dismiss()
        }

        val text = getValueText()

        if (label != null) {
            text?.setText(label.text)
        } else {
            text?.setText("0")
            doodleView!!.setMark()
        }

        tenkey!!.setOnDismissListener {
            doodleView!!.resetMark()
            doodleView!!.redraw()
        }

        val anchor = getView()!!.findViewById(R.id.popup_anchor)!!
        setViewMargin(anchor, PointF(0.0f, 0.0f))

        handler.post {
            tenkey!!.showAsDropDown(anchor, doodleView!!.getWidth(), 0)
        }
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

    private inner class BackSpaceListener: View.OnClickListener {
        override fun onClick(v: View) {
            val text = getValueText()
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