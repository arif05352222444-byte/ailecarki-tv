package com.ailecarki.tv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ailecarki.tv.R
import com.ailecarki.tv.domain.engine.TurkishAlphabet
import com.ailecarki.tv.ui.theme.AppColors

/**
 * Kumanda dostu metin girişi: ekrandaki Türkçe kartela + isteğe bağlı sistem klavyesi.
 * Sistem klavyesi olmayan / Türkçe karakter içermeyen TV Box'larda da çalışır.
 */
@Composable
fun TextInputDialog(
    title: String,
    initial: String,
    maxLength: Int,
    allowSpace: Boolean,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    subtitle: String? = null,
) {
    var value by remember { mutableStateOf(TextFieldValue(initial, TextRange(initial.length))) }
    fun setText(t: String) {
        val clean = TurkishAlphabet.upper(t).take(maxLength)
        value = TextFieldValue(clean, TextRange(clean.length))
    }
    val fieldFocus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var fieldFocused by remember { mutableStateOf(false) }

    GameDialog(onDismiss = onDismiss, maxWidth = 920.dp) {
        Text(title, style = goldTextStyle(30.sp))
        if (subtitle != null) Text(subtitle, color = AppColors.GoldLight, fontSize = 22.sp, fontWeight = FontWeight.Black)

        val shape = RoundedCornerShape(12.dp)
        Box(
            Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(AppColors.TileFace, shape)
                .border(if (fieldFocused) 4.dp else 2.dp, if (fieldFocused) AppColors.Gold else AppColors.BlueLight, shape)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            BasicTextField(
                value = value,
                onValueChange = { v ->
                    val up = TurkishAlphabet.upper(v.text).take(maxLength)
                    value = v.copy(text = up)
                },
                singleLine = true,
                textStyle = TextStyle(color = AppColors.Navy, fontSize = 30.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Start),
                cursorBrush = SolidColor(AppColors.Navy),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { keyboard?.hide(); onConfirm(value.text) }),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(fieldFocus)
                    .onFocusChanged { fieldFocused = it.isFocused }
                    .onPreviewKeyEvent { e ->
                        if (e.type == KeyEventType.KeyDown && e.key == Key.DirectionDown) {
                            keyboard?.hide(); focusManager.moveFocus(FocusDirection.Down); true
                        } else false
                    },
            )
        }

        LetterKeyboard(
            isEnabled = { true },
            onLetter = { c -> setText(value.text + c) },
            keySize = 48.dp,
            focusLetter = 'A',
        )

        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            if (allowSpace) {
                TvButton(stringResource(R.string.input_space), onClick = { if (value.text.isNotEmpty() && !value.text.endsWith(" ")) setText(value.text + " ") }, height = 52.dp, fontSize = 18.sp)
            }
            TvButton(stringResource(R.string.input_delete), onClick = { setText(value.text.dropLast(1)) }, height = 52.dp, fontSize = 18.sp)
            TvButton(stringResource(R.string.input_clear), onClick = { setText("") }, height = 52.dp, fontSize = 18.sp)
            TvButton(
                stringResource(R.string.input_system_keyboard),
                onClick = { runCatching { fieldFocus.requestFocus() }; keyboard?.show() },
                height = 52.dp,
                fontSize = 18.sp,
            )
            TvButton(
                stringResource(R.string.input_ok),
                onClick = { onConfirm(value.text) },
                enabled = value.text.isNotBlank(),
                primary = true,
                height = 52.dp,
                fontSize = 20.sp,
            )
        }
    }
}
