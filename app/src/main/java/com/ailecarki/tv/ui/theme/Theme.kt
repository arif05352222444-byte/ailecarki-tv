package com.ailecarki.tv.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ailecarki.tv.domain.engine.TurkishAlphabet
import java.text.NumberFormat

object AppColors {
    val Navy = Color(0xFF070B2A)
    val NavyMid = Color(0xFF0C1650)
    val DeepBlue = Color(0xFF13277A)
    val Blue = Color(0xFF1E48C8)
    val BlueLight = Color(0xFF4F7DFF)
    val Purple = Color(0xFF5A2BA8)
    val Gold = Color(0xFFFFC52E)
    val GoldLight = Color(0xFFFFE58A)
    val GoldDark = Color(0xFFE08A00)
    val White = Color(0xFFFFFFFF)
    val TextMuted = Color(0xFFB9C4F2)
    val Disabled = Color(0xFF2E3558)
    val DisabledText = Color(0xFF6D7599)
    val Red = Color(0xFFE23B3B)
    val Green = Color(0xFF2FB457)
    val TileFace = Color(0xFFF4F6FF)
    val TileHidden = Color(0xFFDDE3F8)

    val PlayerColors = listOf(
        Color(0xFF1E5BE0), Color(0xFFC62828), Color(0xFF239A48),
        Color(0xFFE07A10), Color(0xFF7B3FD0), Color(0xFF0E9AA8),
    )

    /** Çark dilim renkleri (WheelSegment.colorIndex). 8-11: özel dilimler. */
    val Wheel = listOf(
        Color(0xFF2461EB), Color(0xFF2BB54F), Color(0xFFFF8A14), Color(0xFFE22E2E),
        Color(0xFF9A3DDE), Color(0xFFFFC21F), Color(0xFFE63AAE), Color(0xFF15AEE6),
        Color(0xFF15151F), Color(0xFF1B9ED6), Color(0xFFD93AC0), Color(0xFFFFB300),
    )

    val goldBrush = Brush.verticalGradient(listOf(GoldLight, Gold, GoldDark))
    val blueBrush = Brush.verticalGradient(listOf(Blue, DeepBlue))
    val disabledBrush = Brush.verticalGradient(listOf(Disabled, Disabled))
    val panelBrush = Brush.verticalGradient(listOf(Color(0xE6101C5E), Color(0xE60A1240)))
}

object Dimens {
    /** TV overscan güvenli alanı. */
    val SafeHorizontal = 48.dp
    val SafeVertical = 24.dp
}

private val trNumber: NumberFormat = NumberFormat.getIntegerInstance(TurkishAlphabet.LOCALE)
fun formatScore(value: Int): String = trNumber.format(value.toLong())

@Composable
fun AileCarkiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = AppColors.Gold,
            background = AppColors.Navy,
            surface = AppColors.NavyMid,
            onPrimary = AppColors.Navy,
            onBackground = AppColors.White,
            onSurface = AppColors.White,
        ),
        content = content,
    )
}
