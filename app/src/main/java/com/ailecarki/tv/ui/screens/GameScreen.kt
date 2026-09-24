package com.ailecarki.tv.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ailecarki.tv.R
import com.ailecarki.tv.domain.engine.PuzzleEngine
import com.ailecarki.tv.domain.engine.WheelEngine
import com.ailecarki.tv.domain.model.GamePhase
import com.ailecarki.tv.domain.model.GameState
import com.ailecarki.tv.domain.model.SegmentType
import com.ailecarki.tv.domain.rules.WheelConfig
import com.ailecarki.tv.ui.components.BannerCard
import com.ailecarki.tv.ui.components.CategoryPill
import com.ailecarki.tv.ui.components.EventBanner
import com.ailecarki.tv.ui.components.GameDialog
import com.ailecarki.tv.ui.components.LetterKeyboard
import com.ailecarki.tv.ui.components.PlayerScoreRow
import com.ailecarki.tv.ui.components.PuzzleBoard
import com.ailecarki.tv.ui.components.RequestFocus
import com.ailecarki.tv.ui.components.TextInputDialog
import com.ailecarki.tv.ui.components.TitleMarquee
import com.ailecarki.tv.ui.components.TvButton
import com.ailecarki.tv.ui.components.WheelView
import com.ailecarki.tv.ui.components.goldTextStyle
import com.ailecarki.tv.ui.components.revealDurationMs
import com.ailecarki.tv.ui.theme.AppColors
import com.ailecarki.tv.ui.theme.Dimens
import com.ailecarki.tv.ui.theme.formatScore
import com.ailecarki.tv.ui.viewmodel.GameViewModel
import kotlinx.coroutines.delay

private val SpinEasing = CubicBezierEasing(0.12f, 0.8f, 0.2f, 1f)
private const val WHEEL_ENTER_MS = 450L
private const val WHEEL_RESULT_MS = 1600L
private const val ROUND_DIALOG_DELAY_MS = 1800L

private val ACTION_PHASES = setOf(GamePhase.PLAYER_TURN, GamePhase.PLAYER_ACTION)
private val SELECTION_PHASES = setOf(GamePhase.LETTER_SELECTION, GamePhase.VOWEL_SELECTION)
private val WHEEL_PHASES = setOf(GamePhase.WHEEL_SPINNING, GamePhase.WHEEL_RESULT)
private val BUSY_PHASES = setOf(GamePhase.WHEEL_SPINNING, GamePhase.WHEEL_RESULT, GamePhase.LETTER_REVEAL, GamePhase.FINAL_REVEAL)

@Composable
fun GameScreen(vm: GameViewModel, onExitToMenu: () -> Unit, onNewGame: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    // Ekrandan çıkılınca final sayacı temizlenir (kayıttan devamda yeniden başlar).
    DisposableEffect(Unit) { onDispose { vm.leaveGame() } }
    val s = state ?: return
    var showExit by remember { mutableStateOf(false) }
    // Çıkış diyaloğu kapanınca odak yeniden verilir (diyalog açıkken arka pencereye odak istenmez).
    var exitClosedCount by remember { mutableIntStateOf(0) }
    val closeExit: () -> Unit = { showExit = false; exitClosedCount += 1 }

    BackHandler {
        when (s.phase) {
            GamePhase.VOWEL_SELECTION -> vm.cancelVowel()
            in BUSY_PHASES -> Unit // animasyon sırasında geri tuşu yok sayılır
            GamePhase.GAME_COMPLETE -> onExitToMenu()
            else -> showExit = true
        }
    }

    when {
        s.phase == GamePhase.GAME_COMPLETE -> GameCompleteScreen(s, vm, onNewGame = onNewGame, onMenu = onExitToMenu)
        s.isFinal -> FinalScreen(s, vm, refocusKey = exitClosedCount)
        else -> MainGameContent(s, vm, refocusKey = exitClosedCount)
    }

    if (showExit) {
        val stay = remember { FocusRequester() }
        GameDialog(onDismiss = closeExit) {
            Text(stringResource(R.string.exit_title), style = goldTextStyle(32.sp))
            Text(stringResource(R.string.exit_body), color = AppColors.White, fontSize = 20.sp, textAlign = TextAlign.Center)
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                TvButton(stringResource(R.string.exit_stay), closeExit, Modifier.width(240.dp), focusRequester = stay)
                TvButton(stringResource(R.string.exit_leave), { showExit = false; vm.leaveGame(); onExitToMenu() }, Modifier.width(240.dp))
            }
            RequestFocus(stay, Unit)
        }
    }
}

@Composable
private fun MainGameContent(s: GameState, vm: GameViewModel, refocusKey: Any?) {
    // Çarkın açısı turlar arasında korunur (çark kaldığı yerden döner).
    val rotation = remember { Animatable(0f) }

    // Çevirme animasyonu: sonuç engine tarafından önceden belirlendi, animasyon onu gösterir.
    LaunchedEffect(s.spinCount) {
        if (s.phase == GamePhase.WHEEL_SPINNING) {
            val plan = vm.spinPlan() ?: return@LaunchedEffect
            val current = WheelEngine.positiveMod(rotation.value, 360f)
            rotation.snapTo(current)
            val target = vm.wheel.targetRotation(current, plan.targetIndex, plan.extraTurns, plan.jitter)
            delay(WHEEL_ENTER_MS)
            rotation.animateTo(target, tween(plan.durationMs, easing = SpinEasing))
            vm.onSpinFinished()
        }
    }
    // Sonuç gösterimi → etkisini uygula (harf seçimi / iflas / sıra geç / 2x).
    LaunchedEffect(s.phase, s.spinCount) {
        if (s.phase == GamePhase.WHEEL_RESULT) {
            val idx = s.spinSegmentIndex
            if (idx != null && vm.wheel.segmentIndexAt(rotation.value) != idx) {
                rotation.snapTo(vm.wheel.targetRotation(0f, idx, 0)) // kayıttan dönüldüyse
            }
            delay(WHEEL_RESULT_MS)
            vm.resolveWheelResult()
        }
    }
    // Harfler tek tek açılınca devam et.
    LaunchedEffect(s.phase, s.eventCounter) {
        if (s.phase == GamePhase.LETTER_REVEAL) {
            val letter = s.lastRevealedLetter
            val count = if (letter != null) PuzzleEngine.letterCount(s.puzzle.answer, letter) else 1
            delay(revealDurationMs(count, singleLetter = true))
            vm.finishReveal()
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = Dimens.SafeHorizontal, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.fillMaxWidth()) {
                Text(
                    stringResource(R.string.round_label, s.round, s.totalRounds),
                    color = AppColors.GoldLight,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.align(Alignment.CenterStart),
                )
                TitleMarquee(stringResource(R.string.title), Modifier.align(Alignment.Center), fontSize = 30.sp)
                if (s.doubleActive) {
                    Text(
                        stringResource(R.string.double_active),
                        color = AppColors.Navy,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .background(AppColors.Gold, RoundedCornerShape(10.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            CategoryPill(s.puzzle.category)
            key(s.puzzle.id) {
                PuzzleBoard(
                    s.puzzle.answer,
                    s.revealedLetters,
                    Modifier.fillMaxWidth().weight(1f).padding(vertical = 10.dp),
                )
            }
            Text(
                stringResource(R.string.turn_of, s.currentPlayer.name),
                color = AppColors.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().height(70.dp), contentAlignment = Alignment.Center) {
                if (s.phase in ACTION_PHASES) ActionButtons(s, vm, refocusKey)
            }
            Spacer(Modifier.height(12.dp))
            PlayerScoreRow(s.players, s.currentPlayerIndex)
        }

        // Harf kartelası alttan kayarak gelir.
        AnimatedVisibility(
            visible = s.phase in SELECTION_PHASES,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(tween(320)) { it } + fadeIn(tween(200)),
            exit = slideOutVertically(tween(260)) { it } + fadeOut(tween(200)),
        ) {
            KeyboardPanel(s, vm, refocusKey)
        }

        // Çark overlay: yalnızca çevirme sırasında ekranın ortasına gelir.
        AnimatedVisibility(
            visible = s.phase in WHEEL_PHASES,
            modifier = Modifier.fillMaxSize(),
            enter = fadeIn(tween(250)) + scaleIn(tween(WHEEL_ENTER_MS.toInt()), initialScale = 0.35f),
            exit = fadeOut(tween(300)) + scaleOut(tween(300), targetScale = 0.4f),
        ) {
            Box(Modifier.fillMaxSize().background(Color(0xB3050820)), contentAlignment = Alignment.Center) {
                WheelView(vm.wheel.segments, rotation = { rotation.value }, size = 400.dp)
                if (s.phase == GamePhase.WHEEL_RESULT) {
                    WheelResultBanner(s, vm, Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp))
                }
            }
        }

        EventBanner(s)
    }

    if (s.phase == GamePhase.SOLVING) {
        TextInputDialog(
            title = stringResource(R.string.solve_title),
            subtitle = s.currentPlayer.name,
            initial = "",
            maxLength = 40,
            allowSpace = true,
            onConfirm = vm::submitSolve,
            onDismiss = vm::cancelSolve,
        )
    }

    if (s.phase == GamePhase.ROUND_COMPLETE) RoundCompleteDialog(s, vm)
}

@Composable
private fun ActionButtons(s: GameState, vm: GameViewModel, refocusKey: Any?) {
    val spin = remember { FocusRequester() }
    val solve = remember { FocusRequester() }
    val canSpin = vm.canSpin()
    RequestFocus(if (canSpin) spin else solve, s.phase, s.currentPlayerIndex, s.eventCounter, refocusKey)
    Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
        TvButton(stringResource(R.string.action_spin), vm::spin, Modifier.width(280.dp), enabled = canSpin, focusRequester = spin, height = 62.dp, fontSize = 24.sp)
        TvButton(stringResource(R.string.action_vowel, vm.rules.vowelCost), vm::buyVowel, Modifier.width(300.dp), enabled = vm.canBuyVowel(), height = 62.dp)
        TvButton(stringResource(R.string.action_solve), vm::startSolve, Modifier.width(180.dp), focusRequester = solve, height = 62.dp, fontSize = 24.sp)
    }
}

@Composable
private fun KeyboardPanel(s: GameState, vm: GameViewModel, refocusKey: Any?) {
    val segment = s.spinSegmentIndex?.let { vm.wheel.segments.getOrNull(it) }
    val header = when {
        s.phase == GamePhase.VOWEL_SELECTION -> stringResource(R.string.pick_vowel, vm.rules.vowelCost)
        segment?.type == SegmentType.JOKER -> stringResource(R.string.pick_joker, formatScore(vm.rules.jokerPoints))
        segment != null -> stringResource(R.string.pick_consonant_for, formatScore(segment.value)) +
            if (s.doubleActive) " · 2X" else ""
        else -> ""
    }
    val shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .background(AppColors.panelBrush, shape)
            .border(3.dp, AppColors.Gold, shape)
            .padding(top = 12.dp, bottom = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(header, style = goldTextStyle(26.sp))
        LetterKeyboard(
            isEnabled = { c -> vm.canSelectLetter(c) },
            onLetter = vm::chooseLetter,
            focusKey = s.phase to refocusKey,
        )
    }
}

@Composable
private fun WheelResultBanner(s: GameState, vm: GameViewModel, modifier: Modifier) {
    val segment = s.spinSegmentIndex?.let { vm.wheel.segments.getOrNull(it) } ?: return
    val (title, color) = when (segment.type) {
        SegmentType.POINTS -> stringResource(R.string.points_value, formatScore(segment.value)) to AppColors.Gold
        SegmentType.BANKRUPT -> stringResource(R.string.ev_bankrupt) to AppColors.Red
        SegmentType.LOSE_TURN -> WheelConfig.LABEL_LOSE_TURN to AppColors.BlueLight
        SegmentType.DOUBLE -> stringResource(R.string.ev_double) to AppColors.Gold
        SegmentType.JOKER -> WheelConfig.LABEL_JOKER to AppColors.Gold
    }
    val sub = when (segment.type) {
        SegmentType.POINTS, SegmentType.JOKER -> "HARF SEÇ"
        else -> null
    }
    BannerCard(title, sub, color, modifier)
}

@Composable
private fun RoundCompleteDialog(s: GameState, vm: GameViewModel) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(s.round) {
        delay(ROUND_DIALOG_DELAY_MS)
        visible = true
    }
    if (!visible) return
    GameDialog(onDismiss = {}, dismissOnBack = false, maxWidth = 820.dp) {
        Text(stringResource(R.string.round_complete), style = goldTextStyle(40.sp))
        Text(s.puzzle.answer, color = AppColors.White, fontSize = 28.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
        s.roundWinnerIndex?.let {
            Text(stringResource(R.string.round_winner, s.players[it].name), color = AppColors.GoldLight, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        PlayerScoreRow(s.players, s.roundWinnerIndex, Modifier.width(760.dp))
        var ready by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { delay(1200); ready = true }
        Box(Modifier.height(64.dp), contentAlignment = Alignment.Center) {
            if (ready) {
                val next = remember { FocusRequester() }
                TvButton(
                    stringResource(if (vm.isLastNormalRound()) R.string.go_final else R.string.next_round),
                    vm::nextRound,
                    Modifier.width(320.dp),
                    primary = true,
                    focusRequester = next,
                )
                RequestFocus(next, Unit)
            }
        }
    }
}
