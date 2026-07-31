package com.simovic.simovicweather.feature.base.presentation.compose.composable

import android.content.res.Configuration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.simovic.simovicweather.feature.base.presentation.ui.AppTheme

@Composable
fun AppPreview(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    AppTheme {
        Surface(
            modifier = modifier,
            color = MaterialTheme.colorScheme.background,
            content = content,
        )
    }
}

@Preview(
    name = "Light",
    group = "Themes",
    showBackground = true,
)
@Preview(
    name = "Dark",
    group = "Themes",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
annotation class PreviewAppThemes
