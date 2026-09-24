package com.ailecarki.tv.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ailecarki.tv.ui.theme.AppColors

/**
 * Blur kullanmadan ucuz "glow": birkaç yarı saydam yuvarlak dikdörtgen.
 * Düşük donanımlı TV Box'larda akıcı kalır.
 */
fun Modifier.focusGlow(active: Boolean, corner: Dp = 14.dp, color: Color = AppColors.Gold): Modifier =
    if (!active) this else drawBehind {
        val layers = 4
        val step = 3.dp.toPx()
        for (i in layers downTo 1) {
            val grow = step * i
            drawRoundRect(
                color = color.copy(alpha = 0.10f + 0.04f * (layers - i)),
                topLeft = Offset(-grow, -grow),
                size = Size(size.width + grow * 2, size.height + grow * 2),
                cornerRadius = CornerRadius(corner.toPx() + grow),
            )
        }
    }
