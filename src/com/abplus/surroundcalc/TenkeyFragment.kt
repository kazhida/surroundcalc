package com.abplus.surroundcalc

import android.app.DialogFragment
import android.os.Bundle
import android.app.Dialog
import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup

/**
 * Created by kazhida on 2014/01/04.
 */
class TenkeyFragment: DialogFragment() {

    public override fun onCreateView(inflater : LayoutInflater, container : ViewGroup?, savedInstanceState : Bundle?) : View? {
        return inflater.inflate(R.layout.tenkey, container, false)
    }

    public override fun onCreateDialog(savedInstanceState : Bundle?) : Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.setTitle("0")

        return dialog
    }

    public override fun onActivityCreated(savedInstanceState : Bundle?) : Unit {
        super.onActivityCreated(savedInstanceState)

        getView()?.findViewById(R.id.key_ent)?.setOnClickListener {(view: View): Unit ->
            dismiss()
        }
    }
}