package com.simovic.simovicweather.feature.base.presentation.compose.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.simovic.simovicweather.feature.base.presentation.ui.AppTheme

@Composable
fun WeatherBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(AppTheme.colors.backgroundStart, AppTheme.colors.backgroundEnd),
                    ),
                ),
        content = content,
    )
}

@PreviewAppThemes
@Composable
private fun WeatherBackgroundPreview() {
    AppPreview {
        WeatherBackground {
            Text(text = "Weather background")
        }
    }
}
