package com.ailecarki.tv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ailecarki.tv.R
import com.ailecarki.tv.ui.theme.AppColors

@Composable
fun CategoryPill(category: String, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier
            .background(AppColors.Navy.copy(alpha = 0.85f), shape)
            .border(2.dp, AppColors.Gold, shape)
            .padding(horizontal = 36.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(R.string.category_label), color = AppColors.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(10.dp))
        Text(category, color = AppColors.Gold, fontSize = 24.sp, fontWeight = FontWeight.Black)
    }
}
