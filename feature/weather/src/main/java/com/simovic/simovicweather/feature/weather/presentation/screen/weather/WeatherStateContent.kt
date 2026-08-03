package com.simovic.simovicweather.feature.weather.presentation.screen.weather

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.simovic.simovicweather.feature.base.presentation.compose.composable.AppButton
import com.simovic.simovicweather.feature.base.presentation.compose.composable.WeatherCard
import com.simovic.simovicweather.feature.base.presentation.ui.AppTheme
import com.simovic.simovicweather.feature.weather.R

@Composable
internal fun WeatherIntroductionContent(onUseCurrentLocation: () -> Unit) {
    CenteredStateCard(
        titleRes = R.string.weather_near_you,
        messageRes = R.string.weather_near_you_explanation,
        actionRes = R.string.use_current_location,
        onAction = onUseCurrentLocation,
    )
}

@Composable
internal fun LocationPermissionContent(onUseCurrentLocation: () -> Unit) {
    CenteredStateCard(
        titleRes = R.string.location_access_needed,
        messageRes = R.string.location_permission_explanation,
        actionRes = R.string.allow_location_access,
        onAction = onUseCurrentLocation,
    )
}

@Composable
internal fun WeatherErrorContent(
    reason: WeatherErrorReason,
    canRetry: Boolean,
    onRetry: () -> Unit,
) {
    CenteredStateCard(
        titleRes = R.string.weather_error_title,
        messageRes = reason.messageRes,
        actionRes = if (canRetry) R.string.retry else null,
        onAction = onRetry,
    )
}

@Composable
private fun CenteredStateCard(
    @StringRes titleRes: Int,
    @StringRes messageRes: Int,
    @StringRes actionRes: Int?,
    onAction: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(
                    horizontal = AppTheme.dimensions.screenPadding,
                    vertical = AppTheme.dimensions.spaceXxxl,
                ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        WeatherCard(modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(titleRes), style = AppTheme.typography.headlineMedium)
            Text(
                text = stringResource(messageRes),
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.textSecondary,
            )
            if (actionRes != null) {
                AppButton(
                    text = stringResource(actionRes),
                    onClick = onAction,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

private val WeatherErrorReason.messageRes: Int
    @StringRes
    get() =
        when (this) {
            WeatherErrorReason.NETWORK -> R.string.weather_error_network
            WeatherErrorReason.RATE_LIMITED -> R.string.weather_error_rate_limited
            WeatherErrorReason.LOCATION_UNAVAILABLE -> R.string.weather_error_location_unavailable
            WeatherErrorReason.MALFORMED_DATA -> R.string.weather_error_malformed_data
            WeatherErrorReason.SERVER -> R.string.weather_error_server
            WeatherErrorReason.UNKNOWN -> R.string.weather_error_unknown
        }
