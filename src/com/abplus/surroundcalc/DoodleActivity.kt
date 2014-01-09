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
import android.widget.PopupMenu
import android.graphics.Point
import android.widget.RelativeLayout
import android.graphics.PointF
import com.google.ads.AdView
import com.google.ads.AdSize
import android.widget.FrameLayout
import com.google.ads.AdRequest
import com.google.ads.InterstitialAd
import com.google.ads.AdListener
import com.google.ads.Ad
import android.os.Handler
import com.abplus.surroundcalc.exporters.ActionSender
import com.abplus.surroundcalc.utls.Purchases
import com.abplus.surroundcalc.billing.BillingHelper
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import com.abplus.surroundcalc.billing.BillingHelper.Result
import android.widget.Toast

/**
 * Created by kazhida on 2014/01/02.
 */
class DoodleActivity : Activity() {

    var doodleView: DoodleView? = null
    var tenkey: PopupWindow? = null
    var adView: AdView? = null
    var interstitial: InterstitialAd? = null
    val handler = Handler()
    var purchases: Purchases? = null
    val sku_basic: String get() = getString(R.string.sku_basic)

    protected override fun onCreate(savedInstanceState: Bundle?) : Unit {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main)
        doodleView = (findViewById(R.id.doddle_view) as DoodleView)

        doodleView!!.setOnClickListener {
            val selected = doodleView!!.getPicked()
            if (selected is Region) {
                showMenu(selected)
            } else if (selected is ValueLabel) {
                showTenkey(selected)
            } else if (selected == null) {
                showTenkey(null)
            }
        }

        purchases = Purchases(this)

        val actionBar = getActionBar()!!
        addTab(actionBar, Drawing.KeyColor.BLUE, true)
        addTab(actionBar, Drawing.KeyColor.GREEN, false)
        addTab(actionBar, Drawing.KeyColor.RED, false)
        addTab(actionBar, Drawing.KeyColor.YELLOW, false)
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS)

        adView = AdView(this, AdSize.BANNER, getString(R.string.banner_unit_id))
        val params = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        adView!!.setLayoutParams(params)
        val frame = (findViewById(R.id.ad_frame) as FrameLayout)
        frame.addView(adView!!)

        interstitial = InterstitialAd(this, getString(R.string.interstitial_unit_id))
        interstitial?.loadAd(AdRequest());
        interstitial?.setAdListener(object: AdListener{
            override fun onReceiveAd(p0: Ad?) {
                Log.d("surroundcalc", "Received")
            }
            override fun onFailedToReceiveAd(p0: Ad?, p1: AdRequest.ErrorCode?) {
                Log.d("surroundcalc", "Failed")
            }
            override fun onPresentScreen(p0: Ad?) {
                Log.d("surroundcalc", "PresentScreen")
            }
            override fun onDismissScreen(p0: Ad?) {
                Log.d("surroundcalc", "DismissScreen")
                interstitial?.loadAd(AdRequest())
            }
            override fun onLeaveApplication(p0: Ad?) {
                Log.d("surroundcalc", "LeaveApplication")
            }
        });
    }

    public override fun onResume() : Unit {
        super.onResume()

        adView?.loadAd(AdRequest())

        val keyColor = Preferences(this).currentColor

        if (keyColor != null) {
            val actionBar = getActionBar()!!
            val tab = actionBar.getTabAt(keyColor.ordinal());
            actionBar.selectTab(tab)
        }

        if (purchases!!.isPurchased(sku_basic)){
            findViewById(R.id.ad_frame)?.setVisibility(View.GONE)
        }

        purchases!!.checkState(sku_basic, object : BillingHelper.QueryInventoryFinishedListener {
            override fun onQueryInventoryFinished(result: BillingHelper.Result?, inventory: BillingHelper.Inventory?) {
                if (result!!.isSuccess()) {
                    if (inventory!!.hasPurchase(sku_basic)) {
                        findViewById(R.id.ad_frame)?.setVisibility(View.GONE)
                    } else {
                        findViewById(R.id.ad_frame)?.setVisibility(View.VISIBLE)
                    }
                } else {
                    findViewById(R.id.ad_frame)?.setVisibility(View.VISIBLE)
                    showErrorToast(R.string.err_inventory)
                }
            }
        })
    }

    protected override fun onActivityResult(requestCode : Int, resultCode : Int, data : Intent?) : Unit {
        if (! purchases!!.billingHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    public override fun onPause() : Unit {
        super.onPause()
        adView?.stopLoading()
        Preferences(this).currentColor = doodleView!!.getDrawing()?.keyColor
    }

    public override fun onStop() {
        super.onStop()
        purchases?.billingHelper?.dispose()
    }


    public override fun onDestroy() {
        adView?.destroy()
        super.onDestroy()
    }

    public override fun onCreateOptionsMenu(menu : Menu?) : Boolean {
        val inflater = getMenuInflater()
        inflater.inflate(R.menu.actions, menu)

        return super.onCreateOptionsMenu(menu)
    }

    public override fun onOptionsItemSelected(item : MenuItem?) : Boolean {
        return when (item?.getItemId()) {
            R.id.action_clear_drawing -> {
                doodleView!!.clear()
                true
            }
            R.id.action_content_undo -> {
                doodleView!!.undo()
                true
            }
            R.id.action_social_share -> {
                if (purchases!!.isPurchased(sku_basic)) {
                    ActionSender().startActivity(this, doodleView!!.createBitmap())
                } else {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle(R.string.upgrade_title)
                    builder.setMessage(R.string.upgrade_message)
                    builder.setPositiveButton(R.string.upgrade) {(dialog: DialogInterface, which: Int) ->
                        purchases!!.purchase(this, getString(R.string.sku_basic), object : Runnable {
                            override fun run() {
                                findViewById(R.id.ad_frame)?.setVisibility(View.GONE)
                            }
                        })
                    }
                    builder.setNegativeButton(R.string.close, null)
                    builder.setCancelable(true)
                    builder.create().show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showErrorToast(msgId: Int) {
        Toast.makeText(this, msgId, Toast.LENGTH_SHORT).show()
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
            doodleView!!.setDrawing(drawing)
        }
    }

    private inner class TabListener: ActionBar.TabListener {
        override fun onTabSelected(tab: ActionBar.Tab?, ft: FragmentTransaction?) {
            val drawing = (tab?.getTag() as Drawing)
            doodleView!!.setDrawing(drawing)
            if (! purchases!!.isPurchased(sku_basic)) {
                interstitial?.show()
            }
        }
        override fun onTabUnselected(tab: ActionBar.Tab?, ft: FragmentTransaction?) {}
        override fun onTabReselected(tab: ActionBar.Tab?, ft: FragmentTransaction?) {}
    }

    private fun showMenu(region: Region): Unit {
        val anchor = findViewById(R.id.popup_anchor)!!
        val menu = when (region.labels.size()) {
            1 -> createMenu1(anchor, region.labels.get(0))
            2 -> createMenu2(anchor, region.labels.get(0), region.labels.get(1))
            else -> createMenuN(anchor, region.labels)
        }

        setViewMargin(anchor, doodleView!!.getTapped())
        menu.show()
    }

    private fun setViewMargin(view: View, p: PointF) {
        val x: Int = p.x.toInt()
        var y: Int = p.y.toInt()
        val params = RelativeLayout.LayoutParams(view.getLayoutParams()!!)

        params.setMargins(x, y, 0, 0)

        view.setLayoutParams(params)
        view.invalidate()
    }

    private fun initMenuItem(menu: Menu, id: Int, value: Double, prefix: String) {
        val item = menu.findItem(id)
        item?.setTitle(prefix + ":  " + value.toString())

    }

    private fun addResult(value: Double) : Boolean {
        doodleView!!.addResultLabel(doodleView!!.getTapped(), value)
        doodleView!!.redraw()
        return true
    }

    private fun createMenu1(anchor: View, value: ValueLabel): PopupMenu {
        val popupMenu = PopupMenu(this, anchor)

        popupMenu.inflate(R.menu.calc_1)
        val menu = popupMenu.getMenu()!!

        val neg = -value.value
        val x2 = value.value * value.value
        val sqrt = Math.sqrt(value.value)

        initMenuItem(menu, R.id.ope_neg,     neg,  "-")
        initMenuItem(menu, R.id.func_square, x2,   "x2")
        initMenuItem(menu, R.id.func_sqrt,   sqrt, "√")

        popupMenu.setOnMenuItemClickListener {
            when (it?.getItemId()) {
                R.id.ope_neg     -> addResult(neg)
                R.id.func_square -> addResult(x2)
                R.id.func_sqrt   -> addResult(sqrt)
                else -> false
            }
        }

        return popupMenu
    }

    private fun createMenu2(anchor: View, value1: ValueLabel, value2: ValueLabel): PopupMenu {
        val popupMenu = PopupMenu(this, anchor)

        popupMenu.inflate(R.menu.calc_2)

        val menu = popupMenu.getMenu()!!

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

        initMenuItem(menu, R.id.ope_add,  add, "+")
        initMenuItem(menu, R.id.ope_sub,  sub, "+")
        initMenuItem(menu, R.id.ope_mul,  mul, "×")
        initMenuItem(menu, R.id.ope_div,  div, "÷")
        initMenuItem(menu, R.id.func_pow, pow, "pow")

        popupMenu.setOnMenuItemClickListener {
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

    private fun createMenuN(anchor: View, labels: List<ValueLabel>): PopupMenu {
        val popupMenu = PopupMenu(this, anchor)

        popupMenu.inflate(R.menu.calc_n)

        val menu = popupMenu.getMenu()!!

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

        initMenuItem(menu, R.id.func_sum,  sum, "sum")
        initMenuItem(menu, R.id.func_varp, sn, "σ/n")
        initMenuItem(menu, R.id.func_vars, sn1, "σ/n-1")

        popupMenu.setOnMenuItemClickListener {
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
        val view = getLayoutInflater().inflate(R.layout.tenkey, null, false)!!
        val wrap = ViewGroup.LayoutParams.WRAP_CONTENT

        tenkey = PopupWindow(this)
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

        val anchor = findViewById(R.id.popup_anchor)!!
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