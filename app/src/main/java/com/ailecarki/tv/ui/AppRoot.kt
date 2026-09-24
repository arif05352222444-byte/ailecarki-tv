package com.ailecarki.tv.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ailecarki.tv.AileCarkiApp
import com.ailecarki.tv.audio.SoundId
import com.ailecarki.tv.ui.components.LocalSoundPlayer
import com.ailecarki.tv.ui.components.StageBackground
import com.ailecarki.tv.ui.screens.GameScreen
import com.ailecarki.tv.ui.screens.HomeScreen
import com.ailecarki.tv.ui.screens.PlayerSetupScreen
import com.ailecarki.tv.ui.screens.SettingsScreen
import com.ailecarki.tv.ui.viewmodel.GameViewModel
import com.ailecarki.tv.ui.viewmodel.SettingsViewModel

enum class Screen { HOME, SETUP, GAME, SETTINGS }

@Composable
fun AppRoot(onExit: () -> Unit) {
    val context = LocalContext.current
    val container = remember { (context.applicationContext as AileCarkiApp).container }
    val gameVm: GameViewModel = viewModel()
    val settingsVm: SettingsViewModel = viewModel()
    var screen by rememberSaveable { mutableStateOf(Screen.HOME) }
    val game by gameVm.state.collectAsStateWithLifecycle()
    val hasSaved by gameVm.hasSavedGame.collectAsStateWithLifecycle()
    val sound: (SoundId) -> Unit = remember { { id -> container.audio.play(id) } }

    // Müzik ekrana göre değişir.
    val isFinal = game?.isFinal == true
    LaunchedEffect(screen, isFinal) {
        container.audio.playMusic(
            when {
                screen != Screen.GAME -> SoundId.MUSIC_MENU
                isFinal -> SoundId.MUSIC_FINAL
                else -> SoundId.MUSIC_GAME
            },
        )
    }
    // Süreç yeniden başlatıldıysa ve oyun state'i yoksa ana menüye dön.
    LaunchedEffect(screen, game) {
        if (screen == Screen.GAME && game == null) screen = Screen.HOME
    }

    CompositionLocalProvider(LocalSoundPlayer provides sound) {
        Box(Modifier.fillMaxSize()) {
            StageBackground()
            when (screen) {
                Screen.HOME -> HomeScreen(
                    hasSavedGame = hasSaved,
                    onNewGame = { screen = Screen.SETUP },
                    onContinue = { gameVm.resumeGame { ok -> if (ok) screen = Screen.GAME } },
                    onSettings = { screen = Screen.SETTINGS },
                    onExit = onExit,
                )
                Screen.SETUP -> PlayerSetupScreen(
                    vm = gameVm,
                    onBack = { screen = Screen.HOME },
                    onStarted = { screen = Screen.GAME },
                )
                Screen.SETTINGS -> SettingsScreen(settingsVm, onBack = { screen = Screen.HOME })
                Screen.GAME -> GameScreen(
                    vm = gameVm,
                    onExitToMenu = { screen = Screen.HOME },
                    onNewGame = { screen = Screen.SETUP },
                )
            }
        }
    }
}
