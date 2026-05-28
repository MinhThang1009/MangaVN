package com.example.mybookslibrary.ui.screens.reader

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color as AndroidColor
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

@Composable
internal fun ConfigureReaderSystemBars(
    activity: ComponentActivity?,
    backgroundIsLight: Boolean
) {
    DisposableEffect(activity, backgroundIsLight) {
        val lightStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
        val darkStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)

        activity?.enableEdgeToEdge(
            statusBarStyle = darkStyle,
            navigationBarStyle = darkStyle
        )

        onDispose {
            activity?.enableEdgeToEdge(
                statusBarStyle = if (backgroundIsLight) lightStyle else darkStyle,
                navigationBarStyle = if (backgroundIsLight) lightStyle else darkStyle
            )
        }
    }
}

internal tailrec fun Context.findActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
