package com.ailecarki.tv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ailecarki.tv.R
import com.ailecarki.tv.domain.model.GamePhase
import com.ailecarki.tv.domain.model.GameState
import com.ailecarki.tv.ui.components.CategoryPill
import com.ailecarki.tv.ui.components.LetterKeyboard
import com.ailecarki.tv.ui.components.PuzzleBoard
import com.ailecarki.tv.ui.components.RequestFocus
import com.ailecarki.tv.ui.components.TextInputDialog
import com.ailecarki.tv.ui.components.TitleMarquee
import com.ailecarki.tv.ui.components.TvButton
import com.ailecarki.tv.ui.components.revealDurationMs
import com.ailecarki.tv.ui.theme.AppColors
import com.ailecarki.tv.ui.theme.Dimens
import com.ailecarki.tv.ui.viewmodel.GameViewModel
import kotlinx.coroutines.delay

@Composable
fun FinalScreen(s: GameState, vm: GameViewModel, refocusKey: Any? = null) {
    val secondsLeft by vm.finalSecondsLeft.collectAsStateWithLifecycle()
    val finalist = s.players[s.finalistIndex ?: s.currentPlayerIndex]
    var showInput by remember { mutableStateOf(false) }

    LaunchedEffect(s.phase) {
        when (s.phase) {
            GamePhase.FINAL_REVEAL -> {
                val newTiles = s.puzzle.answer.count { it in s.finalPicks }
                delay(revealDurationMs(newTiles, singleLetter = false) + 600)
                vm.finishFinalReveal()
            }
            GamePhase.FINAL_SOLVING -> showInput = true
            else -> Unit
        }
    }

    Column(
        Modifier.fillMaxSize().padding(horizontal = Dimens.SafeHorizontal, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TitleMarquee(stringResource(R.string.final_label), fontSize = 34.sp)
        Spacer(Modifier.height(6.dp))
        Text(stringResource(R.string.final_finalist, finalist.name), color = AppColors.GoldLight, fontSize = 24.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(6.dp))
        CategoryPill(s.puzzle.category)
        key(s.puzzle.id) {
            PuzzleBoard(s.puzzle.answer, s.revealedLetters, Modifier.fillMaxWidth().weight(1f).padding(vertical = 10.dp))
        }
        Text(
            stringResource(R.string.final_given, vm.rules.finalGivenLetters.joinToString(" ")),
            color = AppColors.TextMuted,
            fontSize = 17.sp,
        )
        Spacer(Modifier.height(8.dp))
        when (s.phase) {
            GamePhase.FINAL_LETTER_SELECTION -> {
                Text(
                    stringResource(
                        R.string.final_pick_status,
                        vm.rules.finalConsonantPicks, vm.rules.finalVowelPicks,
                        vm.finalConsonantsPicked(), vm.finalVowelsPicked(),
                    ),
                    color = AppColors.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(8.dp))
                LetterKeyboard(
                    isEnabled = { c -> vm.canSelectLetter(c) },
                    onLetter = vm::chooseLetter,
                    focusKey = s.phase to refocusKey,
                )
            }
            GamePhase.FINAL_SOLVING -> {
                val answerBtn = remember { FocusRequester() }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(30.dp)) {
                    TimerBadge(secondsLeft)
                    TvButton(stringResource(R.string.final_enter_answer), { showInput = true }, Modifier.width(320.dp), primary = true, focusRequester = answerBtn)
                }
                RequestFocus(answerBtn, showInput, refocusKey)
            }
            else -> Box(Modifier.height(120.dp))
        }
    }

    if (s.phase == GamePhase.FINAL_SOLVING && showInput) {
        TextInputDialog(
            title = stringResource(R.string.solve_title),
            subtitle = stringResource(R.string.final_time_left, secondsLeft),
            initial = "",
            maxLength = 40,
            allowSpace = true,
            onConfirm = { vm.submitFinal(it) },
            onDismiss = { showInput = false },
        )
    }
}

@Composable
private fun TimerBadge(seconds: Int) {
    val shape = RoundedCornerShape(50)
    val urgent = seconds <= 5
    Box(
        Modifier
            .background(if (urgent) AppColors.Red else AppColors.DeepBlue, shape)
            .border(3.dp, AppColors.Gold, shape)
            .padding(horizontal = 30.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(stringResource(R.string.final_time_left, seconds), color = AppColors.White, fontSize = 40.sp, fontWeight = FontWeight.Black)
    }
}
