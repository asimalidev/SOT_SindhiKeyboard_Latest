package com.sindhi.urdu.english.keybad.sindhikeyboard.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import com.sindhi.urdu.english.keybad.R

object DialogView {
    private var dialog: Dialog? = null

    fun showLoadingDialog(context: Context, message: String) {
        if (dialog?.isShowing == true) return

        // 1. Initialize the dialog
        dialog = Dialog(context)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setContentView(R.layout.dialog_loading)
        dialog?.setCancelable(false)

        dialog?.findViewById<TextView>(R.id.tvLoadingText)?.text = message

        // 2. Show the dialog FIRST before modifying window attributes
        dialog?.show()

        // 3. Set the layout parameters to MATCH_PARENT for full screen
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        // 4. (Optional but recommended) Remove default dialog margins/backgrounds
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    fun dismissDialog() {
        dialog?.dismiss()
        dialog = null // Good practice to avoid memory leaks
    }
}