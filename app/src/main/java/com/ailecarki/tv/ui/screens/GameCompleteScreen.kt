package com.ailecarki.tv.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ailecarki.tv.R
import com.ailecarki.tv.domain.model.GameState
import com.ailecarki.tv.ui.components.Confetti
import com.ailecarki.tv.ui.components.PlayerScoreRow
import com.ailecarki.tv.ui.components.PuzzleBoard
import com.ailecarki.tv.ui.components.RequestFocus
import com.ailecarki.tv.ui.components.TvButton
import com.ailecarki.tv.ui.components.goldTextStyle
import com.ailecarki.tv.ui.theme.AppColors
import com.ailecarki.tv.ui.theme.Dimens
import com.ailecarki.tv.ui.viewmodel.GameViewModel

@Composable
fun GameCompleteScreen(s: GameState, vm: GameViewModel, onNewGame: () -> Unit, onMenu: () -> Unit) {
    val won = s.finalWon == true
    val finalist = s.players[s.finalistIndex ?: 0]
    val champion = s.players[vm.overallWinner()]
    val first = remember { FocusRequester() }
    RequestFocus(first, Unit)

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = Dimens.SafeHorizontal, vertical = Dimens.SafeVertical),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                stringResource(if (won) R.string.final_win_title else R.string.final_lose_title),
                style = goldTextStyle(44.sp),
            )
            Text(
                if (won) stringResource(R.string.final_win_sub, finalist.name) else stringResource(R.string.final_lose_sub),
                color = AppColors.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )
            key(s.puzzle.id) {
                PuzzleBoard(s.puzzle.answer, s.revealedLetters, Modifier.fillMaxWidth().height(130.dp))
            }
            Text(stringResource(R.string.champion, champion.name), style = goldTextStyle(30.sp))
            PlayerScoreRow(s.players, vm.overallWinner())
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.padding(top = 8.dp)) {
                TvButton(stringResource(R.string.menu_new_game), onNewGame, Modifier.width(280.dp), primary = true, focusRequester = first)
                TvButton(stringResource(R.string.main_menu), onMenu, Modifier.width(280.dp))
            }
        }
        if (won) Confetti(trigger = s.eventCounter, durationMs = 5000, count = 90)
    }
}
