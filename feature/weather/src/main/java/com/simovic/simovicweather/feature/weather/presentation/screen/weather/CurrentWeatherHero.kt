package com.simovic.simovicweather.feature.weather.presentation.screen.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import com.simovic.simovicweather.feature.base.presentation.ui.AppTheme
import com.simovic.simovicweather.feature.weather.R

@Composable
internal fun CurrentWeatherHero(
    weather: WeatherUiModel,
    artwork: WeatherHeroArtwork,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = AppTheme.dimensions.weatherHeroMinHeight),
    ) {
        WeatherHeroBackground(
            artwork = artwork,
            modifier = Modifier.matchParentSize(),
        )
        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(AppTheme.colors.heroScrimStart, AppTheme.colors.heroScrimEnd),
                        ),
                    ),
        )
        CurrentWeatherHeroContent(weather = weather, modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
private fun CurrentWeatherHeroContent(
    weather: WeatherUiModel,
    modifier: Modifier = Modifier,
) {
    val current = weather.current
    CompositionLocalProvider(LocalContentColor provides AppTheme.colors.heroContent) {
        Column(
            modifier =
                modifier.padding(
                    horizontal = AppTheme.dimensions.screenPadding,
                    vertical = AppTheme.dimensions.spaceXxl,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimensions.spaceS),
        ) {
            Text(
                text = stringResource(R.string.temperature_celsius, current.temperatureCelsius),
                style = AppTheme.typography.displayLarge,
            )
            Text(
                text = stringResource(current.condition.descriptionRes),
                style = AppTheme.typography.titleMedium,
            )
            val apparentTemperature =
                stringResource(R.string.temperature_celsius, current.apparentTemperatureCelsius)
            Text(
                text = stringResource(R.string.feels_like, apparentTemperature),
                style = AppTheme.typography.bodyLarge,
            )
            TodayTemperatureRange(weather)
            Text(
                text = stringResource(R.string.updated_at, weather.updatedAt),
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.heroContentSecondary,
            )
        }
    }
}

@Composable
private fun TodayTemperatureRange(weather: WeatherUiModel) {
    val minimum = weather.todayMinimumTemperatureCelsius
    val maximum = weather.todayMaximumTemperatureCelsius
    if (minimum != null && maximum != null) {
        Text(
            text =
                stringResource(
                    R.string.high_low,
                    stringResource(R.string.temperature_celsius, maximum),
                    stringResource(R.string.temperature_celsius, minimum),
                ),
            style = AppTheme.typography.bodyLarge,
        )
    }
}
