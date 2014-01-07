package com.abplus.surroundcalc.exporters

import android.content.Context
import android.graphics.Bitmap
import java.io.OutputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import android.content.Intent
import android.net.Uri
import java.io.File
import com.abplus.surroundcalc.R

/**
 * Created by kazhida on 2014/01/07.
 */
class ActionSender {

    val filenameFormatter = SimpleDateFormat("yyyy-MMdd-hhmm-sszzz")
    val messageFormatter = SimpleDateFormat("yyyy/MM/dd hh:mm")

    fun startActivity(context: Context, bitmap: Bitmap): Unit {
        val now  = Calendar.getInstance().getTime()

        val dir = context.getExternalFilesDir("cache");
        val path = File(dir, filenameFormatter.format(now) + ".png")
        val out  = FileOutputStream(path)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        out.close()
        bitmap.recycle()

        val msg = context.getString(R.string.app_name) + "[" + messageFormatter.format(now) + "]"

        val intent = Intent(Intent.ACTION_SEND, null)
        intent.setType("image/png")
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(path));
        intent.putExtra(Intent.EXTRA_TEXT, msg);
        intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name));
        intent.putExtra("sms_body", msg);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)))
    }
}