package com.barikoi.cnlapp.utils.extension

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Parcelable
import android.os.SystemClock
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.barikoi.cnlapp.databinding.DialogLoadingBinding
import java.util.Locale
import androidx.core.graphics.drawable.toDrawable


fun View.rotateViewAnimation(fromDegrees: Float, toDegrees: Float) {
    val an: Animation = RotateAnimation(
        fromDegrees, toDegrees, (this.width / 2).toFloat(),
        (this.height / 2).toFloat()
    )
    an.duration = 500
    an.fillAfter = true
    an.repeatMode = Animation.RESTART
    this.startAnimation(an)
}

fun Fragment.toast(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}

fun Activity.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

inline fun <reified T : Parcelable> Intent.getParcelableArrayListCompat(key: String): ArrayList<T>? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableArrayListExtra(key, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelableArrayListExtra(key)
    }
}

inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(key, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelableExtra(key)
    }
}

fun View.isViewEnable(isEnable: Boolean) {
    if (isEnable) {
        this.isEnabled = true
        this.alpha = 1f
    } else {
        this.isEnabled = false
        this.alpha = 0.7f
    }
}


fun View.setHapticClickListener(f: () -> Unit) {
    var lastTimeClicked: Long = 0
    this.setOnClickListener {
        performTapHaptic()
        if (SystemClock.elapsedRealtime() - lastTimeClicked > 500) {
            lastTimeClicked = SystemClock.elapsedRealtime()
            f()
        }
    }
}

fun View.performTapHaptic() = this.performHapticFeedback(
    HapticFeedbackConstants.VIRTUAL_KEY,
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING else HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
)

fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Context.loadingDialog(onShow: (dialog: Dialog) -> Unit) {
    val bindingView = DialogLoadingBinding.inflate(LayoutInflater.from(this))
    val dialog = Dialog(this)
    dialog.window!!.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
    dialog.setCancelable(true)
    dialog.setContentView(bindingView.root)
    onShow(dialog)
}

object AppLocale {
    fun getCurrentLocale(context: Context): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            context.resources.configuration.locale
        }
    }
}