package com.ailecarki.tv.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ailecarki.tv.R
import com.ailecarki.tv.domain.model.Player
import com.ailecarki.tv.ui.theme.AppColors
import com.ailecarki.tv.ui.theme.formatScore

@Composable
fun PlayerScoreCard(player: Player, index: Int, active: Boolean, compact: Boolean, modifier: Modifier = Modifier) {
    val base = AppColors.PlayerColors[index % AppColors.PlayerColors.size]
    val shownScore by animateIntAsState(player.score, tween(700), label = "score")
    val scale by animateFloatAsState(if (active) 1.06f else 1f, label = "cardScale")
    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .focusGlow(active, 14.dp)
            .background(Brush.verticalGradient(listOf(base, base.copy(alpha = 0.75f))), shape)
            .border(if (active) 4.dp else 2.dp, if (active) AppColors.Gold else base.copy(alpha = 0.5f), shape)
            .height(if (compact) 70.dp else 80.dp)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                player.name,
                color = AppColors.White,
                fontSize = if (compact) 16.sp else 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            formatScore(shownScore),
            color = if (active) AppColors.GoldLight else AppColors.White,
            fontSize = if (compact) 24.sp else 28.sp,
            fontWeight = FontWeight.Black,
        )
        if (active) {
            Text(stringResource(R.string.your_turn), color = AppColors.GoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PlayerScoreRow(players: List<Player>, activeIndex: Int?, modifier: Modifier = Modifier) {
    val compact = players.size > 4
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        players.forEachIndexed { i, p ->
            PlayerScoreCard(p, i, i == activeIndex, compact, Modifier.weight(1f))
        }
    }
}
