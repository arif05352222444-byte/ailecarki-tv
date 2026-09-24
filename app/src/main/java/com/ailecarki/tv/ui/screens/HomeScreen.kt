package com.ailecarki.tv.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ailecarki.tv.R
import com.ailecarki.tv.domain.rules.WheelConfig
import com.ailecarki.tv.ui.components.RequestFocus
import com.ailecarki.tv.ui.components.TitleMarquee
import com.ailecarki.tv.ui.components.TvButton
import com.ailecarki.tv.ui.components.WheelView
import com.ailecarki.tv.ui.theme.AppColors
import com.ailecarki.tv.ui.theme.Dimens

@Composable
fun HomeScreen(
    hasSavedGame: Boolean,
    onNewGame: () -> Unit,
    onContinue: () -> Unit,
    onSettings: () -> Unit,
    onExit: () -> Unit,
) {
    val first = remember { FocusRequester() }
    RequestFocus(first, Unit)
    Row(
        Modifier.fillMaxSize().padding(horizontal = Dimens.SafeHorizontal, vertical = Dimens.SafeVertical),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        WheelView(WheelConfig.DEFAULT, rotation = { -7.5f }, size = 340.dp)
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            TitleMarquee(stringResource(R.string.title), fontSize = 50.sp)
            Text(stringResource(R.string.tagline), color = AppColors.TextMuted, fontSize = 20.sp, fontStyle = FontStyle.Italic)
            Spacer(Modifier.height(6.dp))
            val w = Modifier.width(360.dp)
            TvButton(stringResource(R.string.menu_new_game), onNewGame, w, focusRequester = first, height = 62.dp, fontSize = 24.sp)
            TvButton(stringResource(R.string.menu_continue), onContinue, w, enabled = hasSavedGame, height = 62.dp, fontSize = 24.sp)
            TvButton(stringResource(R.string.menu_settings), onSettings, w, height = 62.dp, fontSize = 24.sp)
            TvButton(stringResource(R.string.menu_exit), onExit, w, height = 62.dp, fontSize = 24.sp)
        }
    }
}
