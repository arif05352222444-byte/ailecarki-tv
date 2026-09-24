package com.ailecarki.tv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ailecarki.tv.ui.theme.AppColors

@OptIn(ExperimentalTextApi::class)
fun goldTextStyle(size: TextUnit) = TextStyle(
    brush = Brush.verticalGradient(listOf(AppColors.GoldLight, AppColors.Gold, AppColors.GoldDark)),
    fontSize = size,
    fontWeight = FontWeight.Black,
    shadow = Shadow(Color(0xAA3A1E00), Offset(0f, 4f), 6f),
)

/** Altın ışıklı tabela başlık. Işık noktaları statik çizilir. */
@Composable
fun TitleMarquee(text: String, modifier: Modifier = Modifier, fontSize: TextUnit = 40.sp) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier
            .background(Brush.verticalGradient(listOf(Color(0xFF1B2E8F), Color(0xFF0B1450))), shape)
            .border(3.dp, AppColors.Gold, shape)
            .drawBehind {
                val n = 18
                val inset = 7.dp.toPx()
                for (i in 0 until n) {
                    val x = size.width * (i + 0.5f) / n
                    drawCircle(AppColors.GoldLight, 2.5.dp.toPx(), Offset(x, inset))
                    drawCircle(AppColors.GoldLight, 2.5.dp.toPx(), Offset(x, size.height - inset))
                }
            }
            .padding(horizontal = 40.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text("★ $text ★", style = goldTextStyle(fontSize))
    }
}
