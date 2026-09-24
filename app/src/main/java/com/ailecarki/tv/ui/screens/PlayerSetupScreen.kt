package com.ailecarki.tv.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ailecarki.tv.R
import com.ailecarki.tv.audio.SoundId
import com.ailecarki.tv.ui.components.LocalSoundPlayer
import com.ailecarki.tv.ui.components.RequestFocus
import com.ailecarki.tv.ui.components.TextInputDialog
import com.ailecarki.tv.ui.components.TitleMarquee
import com.ailecarki.tv.ui.components.TvButton
import com.ailecarki.tv.ui.components.focusGlow
import com.ailecarki.tv.ui.components.goldTextStyle
import com.ailecarki.tv.ui.theme.AppColors
import com.ailecarki.tv.ui.theme.Dimens
import com.ailecarki.tv.ui.viewmodel.GameViewModel

@Composable
fun PlayerSetupScreen(vm: GameViewModel, onBack: () -> Unit, onStarted: () -> Unit) {
    val names by vm.names.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<Int?>(null) }
    var triedStart by remember { mutableStateOf(false) }
    val rowRequesters = remember { List(6) { FocusRequester() } }
    var lastEdited by remember { mutableStateOf(0) }
    // İlk açılışta ve isim diyaloğu kapanınca odak düzenlenen satıra döner.
    LaunchedEffect(editing == null) {
        if (editing == null) {
            kotlinx.coroutines.delay(80)
            runCatching { rowRequesters[lastEdited.coerceIn(0, names.size - 1)].requestFocus() }
        }
    }
    BackHandler { onBack() }

    Column(
        Modifier.fillMaxSize().padding(horizontal = Dimens.SafeHorizontal, vertical = Dimens.SafeVertical),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TitleMarquee(stringResource(R.string.title), fontSize = 34.sp)
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            // Oyuncu listesi paneli
            val panel = RoundedCornerShape(20.dp)
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(AppColors.panelBrush, panel)
                    .border(2.dp, AppColors.BlueLight.copy(alpha = 0.7f), panel)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(stringResource(R.string.setup_title), style = goldTextStyle(30.sp))
                Text(stringResource(R.string.setup_subtitle), color = AppColors.TextMuted, fontSize = 15.sp, fontStyle = FontStyle.Italic)
                val compact = names.size > 4
                names.forEachIndexed { i, name ->
                    NameRow(
                        index = i,
                        name = name,
                        compact = compact,
                        requester = rowRequesters.getOrNull(i),
                        onClick = { lastEdited = i; editing = i },
                    )
                }
                val warning = when {
                    triedStart && !vm.namesValid() -> stringResource(R.string.setup_empty_warning)
                    vm.hasDuplicateNames() -> stringResource(R.string.setup_duplicate_warning)
                    else -> null
                }
                if (warning != null) Text(warning, color = AppColors.GoldLight, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            // Butonlar + kumanda yardımı
            Column(Modifier.width(290.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val w = Modifier.fillMaxWidth()
                TvButton(stringResource(R.string.setup_add), vm::addPlayer, w, enabled = names.size < vm.rules.maxPlayers, height = 48.dp, fontSize = 18.sp)
                TvButton(stringResource(R.string.setup_remove), vm::removePlayer, w, enabled = names.size > vm.rules.minPlayers, height = 48.dp, fontSize = 18.sp)
                TvButton(stringResource(R.string.setup_random), vm::fillRandomNames, w, height = 48.dp, fontSize = 18.sp)
                TvButton(
                    stringResource(R.string.setup_start),
                    onClick = { triedStart = true; vm.startNewGame(onStarted) },
                    modifier = w,
                    primary = true,
                    height = 56.dp,
                    fontSize = 21.sp,
                )
                RemoteHelp()
                TvButton("◀ " + stringResource(R.string.back), onBack, w, height = 46.dp, fontSize = 18.sp)
            }
        }
    }

    // Boş isimle başlatmaya çalışılınca uyarı; buton enable kalır ki odak tahmin edilebilir olsun.
    LaunchedEffect(names) { if (vm.namesValid()) triedStart = false }

    editing?.let { i ->
        TextInputDialog(
            title = stringResource(R.string.setup_name_dialog, i + 1),
            initial = names.getOrElse(i) { "" },
            maxLength = vm.rules.maxNameLength,
            allowSpace = true,
            onConfirm = { vm.setName(i, it); editing = null },
            onDismiss = { editing = null },
        )
    }
}

@Composable
private fun NameRow(index: Int, name: String, compact: Boolean, requester: FocusRequester?, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val sound = LocalSoundPlayer.current
    val scale by animateFloatAsState(if (focused) 1.03f else 1f, label = "rowScale")
    LaunchedEffect(focused) { if (focused) sound(SoundId.UI_MOVE) }
    val shape = RoundedCornerShape(14.dp)
    val color = AppColors.PlayerColors[index % AppColors.PlayerColors.size]
    Row(
        Modifier
            .fillMaxWidth()
            .height(if (compact) 40.dp else 50.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .focusGlow(focused, 14.dp)
            .background(AppColors.DeepBlue, shape)
            .border(if (focused) 3.dp else 1.5.dp, if (focused) AppColors.Gold else AppColors.BlueLight.copy(alpha = 0.6f), shape)
            .then(if (requester != null) Modifier.focusRequester(requester) else Modifier)
            .clickable(interactionSource = interaction, indication = null) { sound(SoundId.UI_SELECT); onClick() }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(if (compact) 28.dp else 34.dp).background(color, CircleShape).border(2.dp, AppColors.White, CircleShape))
        Spacer(Modifier.width(12.dp))
        Text(
            stringResource(R.string.setup_player, index + 1),
            color = if (focused) AppColors.GoldLight else AppColors.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(120.dp),
        )
        val fieldShape = RoundedCornerShape(8.dp)
        Box(
            Modifier
                .weight(1f)
                .height(if (compact) 30.dp else 38.dp)
                .background(if (name.isBlank()) AppColors.Navy else AppColors.TileFace, fieldShape)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (name.isBlank()) {
                Text(stringResource(R.string.setup_name_hint), color = AppColors.DisabledText, fontSize = 18.sp, fontStyle = FontStyle.Italic)
            } else {
                Text(name, color = AppColors.Navy, fontSize = 20.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun RemoteHelp() {
    val shape = RoundedCornerShape(14.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .background(AppColors.panelBrush, shape)
            .border(1.5.dp, AppColors.BlueLight.copy(alpha = 0.6f), shape)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(R.string.remote_help_title), color = AppColors.White, fontSize = 15.sp, fontWeight = FontWeight.Black)
        Text("▲ ▼ ◀ ▶  " + stringResource(R.string.remote_help_move), color = AppColors.TextMuted, fontSize = 13.sp)
        Text("● " + stringResource(R.string.remote_help_ok), color = AppColors.TextMuted, fontSize = 13.sp)
        Text(stringResource(R.string.remote_help_min), color = AppColors.GoldLight, fontSize = 13.sp)
    }
}
