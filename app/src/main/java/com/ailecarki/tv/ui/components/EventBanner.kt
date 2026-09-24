package com.ailecarki.tv.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ailecarki.tv.R
import com.ailecarki.tv.domain.model.GameEvent
import com.ailecarki.tv.domain.model.GameState
import com.ailecarki.tv.ui.theme.AppColors
import com.ailecarki.tv.ui.theme.formatScore
import kotlinx.coroutines.delay

private const val BANNER_MS = 1800L

/** Oyun olaylarını (harf bulundu, iflas, doğru cevap…) kısa süre büyük gösterir. Eski olayları tekrar oynatmaz. */
@Composable
fun EventBanner(s: GameState, modifier: Modifier = Modifier) {
    var seen by remember { mutableIntStateOf(s.eventCounter) }
    var shown by remember { mutableStateOf<GameEvent?>(null) }
    var shownKey by remember { mutableIntStateOf(0) }
    val shake = remember { Animatable(0f) }

    LaunchedEffect(s.eventCounter) {
        if (s.eventCounter != seen) {
            seen = s.eventCounter
            val e = s.lastEvent
            if (e != null && e !is GameEvent.FinalResult) {
                shown = e
                shownKey = s.eventCounter
                if (e is GameEvent.Bankrupt || e is GameEvent.LetterMissing || e is GameEvent.WrongAnswer) {
                    for (x in listOf(-24f, 22f, -18f, 14f, -8f, 0f)) shake.animateTo(x, tween(55))
                }
                delay(BANNER_MS)
                shown = null
            }
        }
    }

    val e = shown
    val next = stringResource(R.string.ev_next_turn, s.currentPlayer.name)
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AnimatedVisibility(
            visible = e != null,
            enter = fadeIn(tween(150)) + scaleIn(tween(220), initialScale = 0.6f),
            exit = fadeOut(tween(200)) + scaleOut(tween(200), targetScale = 0.9f),
        ) {
            if (e != null) {
                val (title, sub, color) = bannerTexts(e, next)
                BannerCard(title, sub, color, Modifier.graphicsLayer { translationX = shake.value * density })
            }
        }
        if (shown is GameEvent.CorrectAnswer) Confetti(trigger = shownKey)
    }
}

@Composable
private fun bannerTexts(e: GameEvent, next: String): Triple<String, String?, Color> = when (e) {
    is GameEvent.LetterFound -> Triple(
        stringResource(R.string.ev_letters_found, e.count),
        stringResource(R.string.ev_plus_points, formatScore(e.points)),
        AppColors.Gold,
    )
    is GameEvent.LetterMissing -> Triple(stringResource(R.string.ev_letter_missing, e.letter.toString()), next, AppColors.Red)
    is GameEvent.VowelBought -> Triple(
        if (e.count > 0) stringResource(R.string.ev_letters_found, e.count)
        else stringResource(R.string.ev_letter_missing, e.letter.toString()),
        stringResource(R.string.ev_vowel_cost, e.cost),
        if (e.count > 0) AppColors.Gold else AppColors.Red,
    )
    GameEvent.Bankrupt -> Triple(stringResource(R.string.ev_bankrupt), next, AppColors.Red)
    GameEvent.LoseTurn -> Triple(stringResource(R.string.ev_lose_turn), next, AppColors.BlueLight)
    GameEvent.DoubleActivated -> Triple(stringResource(R.string.ev_double), stringResource(R.string.double_hint), AppColors.Gold)
    is GameEvent.CorrectAnswer -> Triple(stringResource(R.string.ev_correct), null, AppColors.Green)
    is GameEvent.WrongAnswer -> Triple(stringResource(R.string.ev_wrong), next, AppColors.Red)
    is GameEvent.FinalResult -> Triple("", null, AppColors.Gold)
}

@Composable
fun BannerCard(title: String, subtitle: String?, accent: Color, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier
            .background(Brush.verticalGradient(listOf(Color(0xF2152A8A), Color(0xF2080F3A))), shape)
            .border(4.dp, accent, shape)
            .padding(horizontal = 48.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, color = accent, fontSize = 56.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
        if (subtitle != null) {
            Text(subtitle, color = AppColors.White, fontSize = 26.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}
