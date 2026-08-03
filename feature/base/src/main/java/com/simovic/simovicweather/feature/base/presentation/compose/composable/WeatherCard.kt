package com.simovic.simovicweather.feature.base.presentation.compose.composable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.simovic.simovicweather.feature.base.presentation.ui.AppTheme

@Composable
fun WeatherCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(AppTheme.dimensions.spaceXl),
    contentSpacing: Dp = AppTheme.dimensions.spaceM,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(AppTheme.dimensions.cardRadius),
        color = AppTheme.colors.card,
        contentColor = AppTheme.colors.textPrimary,
        border = BorderStroke(AppTheme.dimensions.cardBorderWidth, AppTheme.colors.cardBorder),
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(contentSpacing),
            content = content,
        )
    }
}

@PreviewAppThemes
@Composable
private fun WeatherCardPreview() {
    AppPreview {
        WeatherCard {
            Text(text = "7-day forecast", style = AppTheme.typography.titleMedium)
            Text(text = "Mostly cloudy", style = AppTheme.typography.bodyLarge)
        }
    }
}
