package com.ailecarki.tv.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.ailecarki.tv.domain.engine.TurkishAlphabet
import com.ailecarki.tv.ui.theme.AppColors
import kotlinx.coroutines.delay

/** Tek harf açılıyorsa kutular arası gecikme; birden çok harf aynı anda açılıyorsa daha kısa. */
const val REVEAL_STEP_SINGLE_MS = 380L
const val REVEAL_STEP_MULTI_MS = 90L
private const val MAX_LINE_CHARS = 16

/** Açılma animasyonunun toplam süresi (ViewModel'e ne zaman devam edileceğini söylemek için). */
fun revealDurationMs(newTileCount: Int, singleLetter: Boolean): Long =
    newTileCount * (if (singleLetter) REVEAL_STEP_SINGLE_MS else REVEAL_STEP_MULTI_MS) + 700L

/** Kelimeleri satırlara böler; bir kelime satıra sığmıyorsa tek başına satır olur. */
fun layoutLines(answer: String, maxChars: Int = MAX_LINE_CHARS): List<List<IndexedWord>> {
    val words = mutableListOf<IndexedWord>()
    var i = 0
    answer.split(' ').forEach { w ->
        if (w.isNotEmpty()) words += IndexedWord(w, i)
        i += w.length + 1
    }
    val lines = mutableListOf<MutableList<IndexedWord>>()
    var len = 0
    for (w in words) {
        val extra = if (lines.isEmpty() || lines.last().isEmpty()) w.text.length else len + 1 + w.text.length
        if (lines.isEmpty() || extra > maxChars) {
            lines += mutableListOf(w); len = w.text.length
        } else {
            lines.last() += w; len = extra
        }
    }
    return lines
}

data class IndexedWord(val text: String, val startIndex: Int)

@Composable
fun PuzzleBoard(answer: String, revealed: Set<Char>, modifier: Modifier = Modifier) {
    // Önceki açık harfleri hatırla → yeni açılanları sırayla animasyonla göster.
    var previous by remember(answer) { mutableStateOf(revealed) }
    val newly = revealed - previous
    val newPositions = answer.indices.filter { answer[it] in newly }
    val step = if (newly.size <= 1) REVEAL_STEP_SINGLE_MS else REVEAL_STEP_MULTI_MS
    val delays: Map<Int, Long> = newPositions.withIndex().associate { (rank, pos) -> pos to rank * step }
    SideEffect { previous = revealed }

    val lines = remember(answer) { layoutLines(answer) }
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val longest = lines.maxOf { line -> line.sumOf { it.text.length } + (line.size - 1) }
        val gap = 6.dp
        val byWidth = (maxWidth - gap * longest) / longest
        val byHeight = (maxHeight - gap * 2 * lines.size) / lines.size
        val tile: Dp = min(min(byWidth, byHeight), 60.dp).coerceAtLeast(18.dp)
        Column(verticalArrangement = Arrangement.spacedBy(gap * 1.5f), horizontalAlignment = Alignment.CenterHorizontally) {
            lines.forEach { line ->
                Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                    line.forEachIndexed { wi, word ->
                        if (wi > 0) Spacer(Modifier.width(tile * 0.5f))
                        word.text.forEachIndexed { ci, c ->
                            val pos = word.startIndex + ci
                            LetterTile(
                                letter = c,
                                shown = !TurkishAlphabet.isLetter(c) || c in revealed,
                                delayMs = delays[pos] ?: 0L,
                                size = tile,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LetterTile(letter: Char, shown: Boolean, delayMs: Long, size: Dp) {
    var displayed by remember { mutableStateOf(shown) }
    var highlight by remember { mutableStateOf(false) }
    val flip = remember { Animatable(1f) }
    LaunchedEffect(shown) {
        if (shown && !displayed) {
            delay(delayMs)
            flip.animateTo(0f, tween(140))
            displayed = true
            highlight = true
            flip.animateTo(1f, tween(160))
            delay(450)
            highlight = false
        } else if (!shown) {
            displayed = false
        }
    }
    val shape = RoundedCornerShape(6.dp)
    Box(
        Modifier
            .size(size)
            .graphicsLayer { scaleX = flip.value }
            .background(
                when {
                    highlight -> AppColors.Gold
                    displayed -> AppColors.TileFace
                    else -> AppColors.TileHidden
                },
                shape,
            )
            .border(2.dp, if (highlight) AppColors.GoldLight else AppColors.BlueLight.copy(alpha = 0.5f), shape),
        contentAlignment = Alignment.Center,
    ) {
        if (displayed) {
            Text(
                letter.toString(),
                color = AppColors.Navy,
                fontSize = (size.value * 0.55f).sp,
                fontWeight = FontWeight.Black,
            )
        }
    }
}
