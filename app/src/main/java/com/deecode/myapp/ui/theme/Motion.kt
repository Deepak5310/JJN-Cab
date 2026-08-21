package com.deecode.myapp.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing

object MotionTokens {
    const val DurationShort = 150
    const val DurationMedium = 300
    const val DurationLong = 500

    val EasingStandard: Easing = FastOutSlowInEasing
    val EasingEmphasized: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val EasingDecelerate: Easing = LinearOutSlowInEasing
}
