package com.outsidesource.oskitcompose.context

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.Window

tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

tailrec fun Context.findApplicationContext(): Context? = when (this) {
    is Activity -> applicationContext
    is ContextWrapper -> baseContext.findApplicationContext()
    else -> null
}

tailrec fun Context.findWindow(): Window? = when (this) {
    is Activity -> window
    is ContextWrapper -> baseContext.findWindow()
    else -> null
}