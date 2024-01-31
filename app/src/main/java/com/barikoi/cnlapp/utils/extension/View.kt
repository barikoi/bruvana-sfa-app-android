package com.barikoi.cnlapp.utils.extension

import android.app.Activity
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.Toast
import androidx.fragment.app.Fragment

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