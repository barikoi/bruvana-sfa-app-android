package com.barikoi.cnlapp.Utils.extension

import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation

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