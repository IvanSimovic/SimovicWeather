package com.simovic.simovicweather.feature.base.presentation.compose.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.simovic.simovicweather.feature.base.presentation.ui.AppTheme

@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(AppTheme.dimensions.spaceXxxl),
            color = AppTheme.colors.primary,
        )
    }
}

@PreviewAppThemes
@Composable
private fun LoadingIndicatorPreview() {
    AppPreview {
        LoadingIndicator()
    }
}
