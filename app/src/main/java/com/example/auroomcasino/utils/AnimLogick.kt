package com.timelysoft.tsjdomcom.utils

import android.app.Activity
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.example.auroomcasino.R


fun animOne(activity: Activity, id: ImageView) {
    val animation: Animation = AnimationUtils.loadAnimation(activity.applicationContext, R.anim.scale_fab_one)
    id.startAnimation(animation)
}

fun animTwo(activity: Activity, id: ImageView) {
    val animation: Animation = AnimationUtils.loadAnimation(activity.applicationContext, R.anim.scale_fab_two)
    id.startAnimation(animation)
}

fun animThree(activity: Activity, id: ImageView) {
    val animation: Animation = AnimationUtils.loadAnimation(activity.applicationContext, R.anim.scale_fab_three)
    id.startAnimation(animation)
}