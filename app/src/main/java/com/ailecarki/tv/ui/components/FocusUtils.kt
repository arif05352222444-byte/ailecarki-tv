package com.ailecarki.tv.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.FocusRequester
import kotlinx.coroutines.delay

/** Odak kaybolmasın: ekran/faz değiştiğinde belirlenen öğeye odak ver. */
@Composable
fun RequestFocus(requester: FocusRequester, vararg keys: Any?) {
    LaunchedEffect(*keys) {
        delay(80)
        runCatching { requester.requestFocus() }
    }
}
