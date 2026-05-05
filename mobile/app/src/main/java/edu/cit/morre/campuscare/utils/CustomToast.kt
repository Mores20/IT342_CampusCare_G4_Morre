package edu.cit.morre.campuscare.utils

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import edu.cit.morre.campuscare.R

object CustomToast {

    fun success(context: Context, message: String) {
        show(context, message, R.layout.toast_success)
    }

    fun error(context: Context, message: String) {
        show(context, message, R.layout.toast_error)
    }

    fun info(context: Context, message: String) {
        show(context, message, R.layout.toast_info)
    }

    private fun show(context: Context, message: String, layoutRes: Int) {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(layoutRes, null)
        layout.findViewById<TextView>(R.id.toastMessage).text = message

        Toast(context).apply {
            duration = Toast.LENGTH_LONG
            view = layout
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 120)
            show()
        }
    }
}