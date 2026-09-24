package com.ailecarki.tv.domain.model

/** UI'da tek seferlik banner / animasyon / ses tetiklemek için olaylar. */
sealed interface GameEvent {
    data class LetterFound(val letter: Char, val count: Int, val points: Int) : GameEvent
    data class LetterMissing(val letter: Char) : GameEvent
    data class VowelBought(val letter: Char, val count: Int, val cost: Int) : GameEvent
    data object Bankrupt : GameEvent
    data object LoseTurn : GameEvent
    data object DoubleActivated : GameEvent
    data class CorrectAnswer(val playerIndex: Int) : GameEvent
    data class WrongAnswer(val playerIndex: Int) : GameEvent
    data class FinalResult(val won: Boolean) : GameEvent
}
