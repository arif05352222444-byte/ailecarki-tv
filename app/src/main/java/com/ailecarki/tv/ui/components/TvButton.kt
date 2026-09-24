package com.ailecarki.tv.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ailecarki.tv.audio.SoundId
import com.ailecarki.tv.ui.theme.AppColors

/**
 * Kumanda için buton: normal / focused / pressed / disabled durumları.
 * Disabled buton da odaklanabilir (D-pad gezintisi tahmin edilebilir kalsın) ama OK işlemez.
 */
@Composable
fun TvButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    primary: Boolean = false,
    focusRequester: FocusRequester? = null,
    height: Dp = 60.dp,
    fontSize: TextUnit = 22.sp,
    corner: Dp = 14.dp,
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val pressed by interaction.collectIsPressedAsState()
    val sound = LocalSoundPlayer.current
    val scale by animateFloatAsState(
        targetValue = when {
            pressed && enabled -> 0.95f
            focused -> 1.07f
            else -> 1f
        },
        label = "tvButtonScale",
    )
    LaunchedEffect(focused) { if (focused) sound(SoundId.UI_MOVE) }

    val shape = RoundedCornerShape(corner)
    val background = when {
        !enabled -> AppColors.disabledBrush
        focused || primary -> AppColors.goldBrush
        else -> AppColors.blueBrush
    }
    val borderColor = when {
        focused && enabled -> AppColors.GoldLight
        focused -> AppColors.Gold.copy(alpha = 0.6f)
        !enabled -> AppColors.DisabledText.copy(alpha = 0.4f)
        else -> AppColors.BlueLight.copy(alpha = 0.7f)
    }
    val textColor = when {
        !enabled -> AppColors.DisabledText
        focused || primary -> AppColors.Navy
        else -> Color.White
    }

    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .focusGlow(focused && enabled, corner)
            .height(height)
            .clip(shape)
            .background(background)
            .border(if (focused) 4.dp else 2.dp, borderColor, shape)
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .clickable(interactionSource = interaction, indication = null) {
                if (enabled) {
                    sound(SoundId.UI_SELECT)
                    onClick()
                } else {
                    sound(SoundId.UI_DISABLED)
                }
            }
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = fontSize,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}
