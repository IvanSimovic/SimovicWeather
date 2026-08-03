package com.simovic.simovicweather.feature.base.presentation.compose.composable

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.simovic.simovicweather.feature.base.presentation.ui.AppTheme

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    Button(
        onClick = onClick,
        modifier =
            modifier
                .heightIn(min = AppTheme.dimensions.controlHeight)
                .then(
                    if (isLoading) {
                        Modifier.semantics { contentDescription = text }
                    } else {
                        Modifier
                    },
                ),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(AppTheme.dimensions.controlRadius),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.primary,
                contentColor = AppTheme.colors.onPrimary,
                disabledContainerColor = AppTheme.colors.disabledContainer,
                disabledContentColor = AppTheme.colors.disabledContent,
            ),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(AppTheme.dimensions.spaceXxl),
                color = AppTheme.colors.onPrimary,
                strokeWidth = AppTheme.dimensions.spaceXs / 2,
            )
        } else {
            Text(text = text, style = AppTheme.typography.labelLarge)
        }
    }
}

@PreviewAppThemes
@Composable
private fun AppButtonPreview() {
    AppPreview {
        AppButton(
            text = "Use current location",
            onClick = {},
        )
    }
}

@PreviewAppThemes
@Composable
private fun AppButtonLoadingPreview() {
    AppPreview {
        AppButton(
            text = "Loading weather",
            onClick = {},
            isLoading = true,
        )
    }
}
