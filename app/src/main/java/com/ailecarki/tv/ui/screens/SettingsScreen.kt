package com.ailecarki.tv.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ailecarki.tv.R
import com.ailecarki.tv.audio.SoundId
import com.ailecarki.tv.data.AppSettings
import com.ailecarki.tv.ui.components.LocalSoundPlayer
import com.ailecarki.tv.ui.components.RequestFocus
import com.ailecarki.tv.ui.components.TvButton
import com.ailecarki.tv.ui.components.focusGlow
import com.ailecarki.tv.ui.components.goldTextStyle
import com.ailecarki.tv.ui.theme.AppColors
import com.ailecarki.tv.ui.theme.Dimens
import com.ailecarki.tv.ui.viewmodel.SettingsViewModel

private fun <T> List<T>.cycle(current: T, step: Int): T {
    val i = indexOf(current).coerceAtLeast(0)
    return this[((i + step) % size + size) % size]
}

private fun levelBar(level: Int) = "■".repeat(level) + "□".repeat(AppSettings.MAX_LEVEL - level)

@Composable
fun SettingsScreen(vm: SettingsViewModel, onBack: () -> Unit) {
    val s by vm.settings.collectAsStateWithLifecycle()
    val first = remember { FocusRequester() }
    RequestFocus(first, Unit)
    BackHandler { onBack() }
    val on = stringResource(R.string.on)
    val off = stringResource(R.string.off)

    Column(
        Modifier.fillMaxSize().padding(horizontal = 140.dp, vertical = Dimens.SafeVertical),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(stringResource(R.string.settings_title), style = goldTextStyle(38.sp))
        SettingRow(stringResource(R.string.set_host), if (s.hostVoice) on else off,
            onChange = { vm.update { it.copy(hostVoice = !it.hostVoice) } }, requester = first)
        SettingRow(stringResource(R.string.set_music), levelBar(s.musicLevel),
            onChange = { d -> vm.update { it.copy(musicLevel = (it.musicLevel + d).coerceIn(0, AppSettings.MAX_LEVEL)) } })
        SettingRow(stringResource(R.string.set_effects), levelBar(s.effectsLevel),
            onChange = { d -> vm.update { it.copy(effectsLevel = (it.effectsLevel + d).coerceIn(0, AppSettings.MAX_LEVEL)) } })
        SettingRow(stringResource(R.string.set_ui), if (s.uiSounds) on else off,
            onChange = { vm.update { it.copy(uiSounds = !it.uiSounds) } })
        SettingRow(stringResource(R.string.set_rounds), s.rounds.toString(),
            onChange = { d -> vm.update { it.copy(rounds = AppSettings.ROUND_OPTIONS.cycle(it.rounds, d)) } })
        SettingRow(stringResource(R.string.set_vowel), s.vowelCost.toString(),
            onChange = { d -> vm.update { it.copy(vowelCost = AppSettings.VOWEL_COST_OPTIONS.cycle(it.vowelCost, d)) } })
        SettingRow(stringResource(R.string.set_final), stringResource(R.string.seconds, s.finalSeconds),
            onChange = { d -> vm.update { it.copy(finalSeconds = AppSettings.FINAL_SECONDS_OPTIONS.cycle(it.finalSeconds, d)) } })
        Text(stringResource(R.string.settings_hint), color = AppColors.TextMuted, fontSize = 14.sp)
        TvButton("◀ " + stringResource(R.string.back), onBack, Modifier.width(240.dp), height = 48.dp, fontSize = 18.sp)
    }
}

/** ◀ ▶ değeri değiştirir (onChange(-1/+1)), OK bir sonraki değere geçer. */
@Composable
private fun SettingRow(label: String, value: String, onChange: (Int) -> Unit, requester: FocusRequester? = null) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val sound = LocalSoundPlayer.current
    val scale by animateFloatAsState(if (focused) 1.03f else 1f, label = "settingScale")
    LaunchedEffect(focused) { if (focused) sound(SoundId.UI_MOVE) }
    val shape = RoundedCornerShape(12.dp)
    Row(
        Modifier
            .width(680.dp)
            .height(42.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .focusGlow(focused, 12.dp)
            .background(if (focused) AppColors.Blue else AppColors.DeepBlue, shape)
            .border(if (focused) 3.dp else 1.5.dp, if (focused) AppColors.Gold else AppColors.BlueLight.copy(alpha = 0.5f), shape)
            .onKeyEvent { e ->
                if (e.type != KeyEventType.KeyDown) return@onKeyEvent false
                when (e.key) {
                    Key.DirectionLeft -> { sound(SoundId.UI_SELECT); onChange(-1); true }
                    Key.DirectionRight -> { sound(SoundId.UI_SELECT); onChange(1); true }
                    else -> false
                }
            }
            .then(if (requester != null) Modifier.focusRequester(requester) else Modifier)
            .clickable(interactionSource = interaction, indication = null) { sound(SoundId.UI_SELECT); onChange(1) }
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = AppColors.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.weight(1f))
        Text(
            if (focused) "◀  $value  ▶" else value,
            color = if (focused) AppColors.GoldLight else AppColors.TextMuted,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
        )
    }
}
