package com.abplus.surroundcalc.utls

import android.content.Context
import com.abplus.surroundcalc.billing.BillingHelper
import android.util.Log
import android.preference.PreferenceManager
import android.app.Activity
import com.abplus.surroundcalc.billing.BillingHelper.OnPurchaseFinishedListener
import com.abplus.surroundcalc.billing.BillingHelper.Result
import com.abplus.surroundcalc.billing.BillingHelper.Inventory

/**
 * Created by kazhida on 2014/01/07.
 */
class Purchases (val context: Context) {

    public val billingHelper: BillingHelper = BillingHelper(context)

    public fun checkState(sku: String, listener: BillingHelper.QueryInventoryFinishedListener): Unit {
        billingHelper.startSetup {
            if (it!!.isSuccess()) {
                val inventory = billingHelper.queryInventory(false)
                if (inventory!!.hasPurchase(sku)) {
                    billingHelper.savePurchase(sku, 1)
                } else {
                    billingHelper.savePurchase(sku, -1)
                }
                listener.onQueryInventoryFinished(it, inventory)
            }
        }
    }

    public fun isPurchased(sku: String): Boolean {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)!!
        val purchased = preferences.getInt(sku, 0)
        Log.d("surroudcalc", "SKU:" + sku + "(" + purchased.toString() + ")")
        return purchased > 0
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
}
