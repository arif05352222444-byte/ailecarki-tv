package com.ailecarki.tv.ui.components

import androidx.compose.runtime.staticCompositionLocalOf
import com.ailecarki.tv.audio.SoundId

/** UI bileşenlerinin ses çalması için; AudioManager'a doğrudan bağımlı olmasınlar. */
val LocalSoundPlayer = staticCompositionLocalOf<(SoundId) -> Unit> { {} }
