package com.abplus.surroundcalc.utls

import android.content.Context
import com.abplus.surroundcalc.billing.BillingHelper
import android.util.Log
import android.preference.PreferenceManager
import android.app.Activity
import com.abplus.surroundcalc.billing.BillingHelper.OnPurchaseFinishedListener

/**
 * Created by kazhida on 2014/01/07.
 */
class Purchases private (val context: Context) {

    enum class State {
        UNKNOWN
        PURCHASED
        NOT_PURCHASED
    }

    public val billingHelper: BillingHelper = BillingHelper(context)

    public fun checkState(listener: BillingHelper.OnSetupFinishedListener): Unit {
        billingHelper.startSetup(listener)
    }

    public fun hasPurchase(sku: String): Boolean {
        try {
            val inventory = billingHelper.queryInventory(false)!!
            Log.d("surrountcalc", "Query inventory was successful.")
            val no_ad = inventory.hasPurchase(sku)
            if (no_ad) {
                billingHelper.savePurchase(sku, 1)
                return true
            } else {
                billingHelper.savePurchase(sku, -1)
                return false
            }
        } catch (e : BillingHelper.BillingException) {
            billingHelper.savePurchase(sku, -1)
            return false
        }
    }

    public fun storedPurchased(sku: String): Boolean {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)!!
        val purchased = preferences.getInt(sku, -1)
        return purchased < 0
    }

    public fun purchase(activity: Activity, sku: String, runnable: Runnable): Unit {
        val requestCode = 12016     //適当な値

        billingHelper.launchPurchaseFlow(activity, sku, requestCode) {
            (result : BillingHelper.Result?, info : BillingHelper.Purchase?) : Unit ->

            if (result!!.isSuccess()) {
                if (info!!.getSku().equals(sku)) {
                    billingHelper.savePurchase(sku, 1)
                    runnable.run()
                }
            }
        }
    }

    class object {
        private var shared: Purchases? = null

        public fun initInstance(context: Context): Unit {
            shared = Purchases(context)
        }

        public fun sharedInstance(): Purchases {
            return shared!!
        }
    }
}
