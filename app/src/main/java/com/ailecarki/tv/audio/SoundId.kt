package com.ailecarki.tv.audio

enum class AudioCategory(val folder: String) { VOICE("voice"), EFFECT("effect"), MUSIC("music"), UI("ui") }

/**
 * Oyunun kullandığı sesler. Kod bu kimliklere bağlıdır; dosya adlarına değil.
 * Dosya konumu: assets/audio/<kategori>/<fileBase>.(ogg|mp3|wav|m4a)
 */
enum class SoundId(val category: AudioCategory, val fileBase: String) {
    VOICE_WELCOME(AudioCategory.VOICE, "welcome"),
    VOICE_SPIN_PROMPT(AudioCategory.VOICE, "spin_prompt"),
    VOICE_CORRECT(AudioCategory.VOICE, "correct"),
    VOICE_WRONG(AudioCategory.VOICE, "wrong"),
    VOICE_BANKRUPT(AudioCategory.VOICE, "bankrupt"),
    VOICE_ROUND_COMPLETE(AudioCategory.VOICE, "round_complete"),
    VOICE_FINAL_INTRO(AudioCategory.VOICE, "final_intro"),
    VOICE_FINAL_WIN(AudioCategory.VOICE, "final_win"),
    VOICE_FINAL_LOSE(AudioCategory.VOICE, "final_lose"),

    FX_WHEEL_SPIN(AudioCategory.EFFECT, "wheel_spin"),
    FX_WHEEL_STOP(AudioCategory.EFFECT, "wheel_stop"),
    FX_LETTER_REVEAL(AudioCategory.EFFECT, "letter_reveal"),
    FX_LETTER_WRONG(AudioCategory.EFFECT, "letter_wrong"),
    FX_BANKRUPT(AudioCategory.EFFECT, "bankrupt"),
    FX_LOSE_TURN(AudioCategory.EFFECT, "lose_turn"),
    FX_DOUBLE(AudioCategory.EFFECT, "double"),
    FX_CORRECT(AudioCategory.EFFECT, "correct"),
    FX_WRONG(AudioCategory.EFFECT, "wrong"),
    FX_CELEBRATION(AudioCategory.EFFECT, "celebration"),
    FX_TIMER_TICK(AudioCategory.EFFECT, "timer_tick"),
    FX_TIMEOUT(AudioCategory.EFFECT, "timeout"),

    MUSIC_MENU(AudioCategory.MUSIC, "menu"),
    MUSIC_GAME(AudioCategory.MUSIC, "game"),
    MUSIC_FINAL(AudioCategory.MUSIC, "final"),

    UI_MOVE(AudioCategory.UI, "move"),
    UI_SELECT(AudioCategory.UI, "select"),
    UI_BACK(AudioCategory.UI, "back"),
    UI_DISABLED(AudioCategory.UI, "disabled"),
}
