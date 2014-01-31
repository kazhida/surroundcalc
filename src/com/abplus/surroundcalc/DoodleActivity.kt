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

    var purchases: Purchases? = null
    var adView: AdView? = null
    var interstitial: InterstitialAd? = null
    val sku_basic: String get() = getString(R.string.sku_basic)

    protected override fun onCreate(savedInstanceState: Bundle?) : Unit {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_activity)

        purchases = Purchases(this)

        adView = AdView(this, AdSize.BANNER, getString(R.string.banner_unit_id))
        val params = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        adView!!.setLayoutParams(params)
        val frame = (findViewById(R.id.ad_frame) as FrameLayout?)
        frame?.addView(adView!!)

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

        val actionBar = getActionBar()!!
        addTab(actionBar, Drawing.KeyColor.BLUE, true)
        addTab(actionBar, Drawing.KeyColor.GREEN, false)
        addTab(actionBar, Drawing.KeyColor.RED, false)
        addTab(actionBar, Drawing.KeyColor.YELLOW, false)
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS)
    }

    protected override fun onActivityResult(requestCode : Int, resultCode : Int, data : Intent?) : Unit {
        if (! purchases!!.billingHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    protected override fun onResume() {
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

    public override fun onPause() {
        super.onPause()
        adView?.stopLoading()
    }

    public override fun onDestroy() {
        adView?.destroy()
        purchases?.billingHelper?.dispose()
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
                doodleView.clear()
                true
            }
            R.id.action_content_undo -> {
                doodleView.undo()
                true
            }
            R.id.action_social_share -> {
                if (purchases!!.isPurchased(sku_basic)) {
                    ActionSender().startActivity(this, doodleView.createBitmap())
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

    private val doodleView: DoodleView get() {
        val fragment = getFragmentManager().findFragmentById(R.id.fragment_container) as DoodleFragment
        return fragment.mainView
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
        tab.setText(resId)
        tab.setTabListener(TabListener(getString(resId), keyColor))
        actionBar.addTab(tab, selected)
    }

    private fun setKeyColor(keyColor: Drawing.KeyColor) {
        Preferences(this).currentColor = keyColor
    }

    private inner class TabListener(val tag: String, val keyColor: Drawing.KeyColor): ActionBar.TabListener {

        var fragment: DoodleFragment? = null

        override fun onTabSelected(tab: ActionBar.Tab?, ft: FragmentTransaction?) {
            if (fragment == null) {
                fragment = DoodleFragment(keyColor)
                ft?.add(R.id.fragment_container, fragment!!, tag)
            } else {
                ft?.attach(fragment)
            }
            setKeyColor(keyColor)
            if (! purchases!!.isPurchased(sku_basic)) {
                interstitial?.show()
            }
        }

        override fun onTabUnselected(tab: ActionBar.Tab?, ft: FragmentTransaction?) {
            if (fragment != null) {
                ft?.detach(fragment)
            }
        }

        override fun onTabReselected(tab: ActionBar.Tab?, ft: FragmentTransaction?) {}
    }
}