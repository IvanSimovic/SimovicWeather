package com.simovic.simovicweather.feature.weather.presentation.screen.weather

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.simovic.simovicweather.feature.base.presentation.compose.composable.AppSearchField
import com.simovic.simovicweather.feature.base.presentation.compose.composable.WeatherCard
import com.simovic.simovicweather.feature.base.presentation.ui.AppTheme
import com.simovic.simovicweather.feature.weather.R

@Composable
internal fun WeatherContent(
    weather: WeatherUiModel,
    onLocationClick: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth().safeDrawingPadding(),
        contentPadding =
            PaddingValues(
                horizontal = AppTheme.dimensions.screenPadding,
                vertical = AppTheme.dimensions.spaceXxxl,
            ),
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimensions.spaceL),
    ) {
        item {
            AppSearchField(
                value = weather.locationName.ifBlank { stringResource(R.string.current_location) },
                onValueChange = {},
                placeholder = stringResource(R.string.search_city),
                    onClear = null,
                    readOnly = true,
                    onClick = onLocationClick,
                    onClickLabel = stringResource(R.string.choose_location),
                )
        }
        item {
            CurrentWeatherContent(weather = weather)
        }
        item {
            WeatherDetails(current = weather.current)
        }
        item {
            DailyForecast(days = weather.days)
        }
    }
}

@Composable
private fun CurrentWeatherContent(weather: WeatherUiModel) {
    val current = weather.current
    WeatherCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.current_conditions),
            style = AppTheme.typography.bodySmall,
            color = AppTheme.colors.textSecondary,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTheme.dimensions.spaceM),
        ) {
            ConditionMarker(condition = current.condition)
            Text(
                text = stringResource(R.string.temperature_celsius, current.temperatureCelsius),
                style = AppTheme.typography.displayLarge,
            )
        }
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
        val minimumTemperature = weather.todayMinimumTemperatureCelsius
        val maximumTemperature = weather.todayMaximumTemperatureCelsius
        if (minimumTemperature != null && maximumTemperature != null) {
            Text(
                text =
                    stringResource(
                        R.string.high_low,
                        stringResource(R.string.temperature_celsius, maximumTemperature),
                        stringResource(R.string.temperature_celsius, minimumTemperature),
                    ),
                style = AppTheme.typography.bodyLarge,
            )
        }
        Text(
            text = stringResource(R.string.updated_at, weather.updatedAt),
            style = AppTheme.typography.bodySmall,
            color = AppTheme.colors.textSecondary,
        )
    }
}

@Composable
private fun WeatherDetails(current: CurrentWeatherUiModel) {
    WeatherCard(
        modifier = Modifier.fillMaxWidth(),
        contentSpacing = AppTheme.dimensions.spaceXs,
    ) {
        MetricRow(R.string.humidity, stringResource(R.string.humidity_percent, current.humidityPercent))
        MetricRow(
            R.string.wind,
            stringResource(R.string.wind_speed_kilometers_per_hour, current.windSpeedKilometersPerHour),
        )
        MetricRow(
            R.string.precipitation,
            stringResource(R.string.precipitation_millimeters, current.precipitationMillimeters),
        )
        MetricRow(
            R.string.pressure,
            stringResource(R.string.pressure_hectopascals, current.pressureHectopascals),
        )
    }
}

@Composable
private fun MetricRow(
    @StringRes labelRes: Int,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = AppTheme.dimensions.spaceXs),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(labelRes),
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.colors.textSecondary,
        )
        Text(text = value, style = AppTheme.typography.labelLarge)
    }
}

@Composable
private fun DailyForecast(days: List<DailyWeatherUiModel>) {
    WeatherCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.seven_day_forecast),
            style = AppTheme.typography.titleMedium,
        )
        Column {
            days.forEachIndexed { index, day ->
                DailyForecastRow(day = day)
                if (index != days.lastIndex) {
                    HorizontalDivider(color = AppTheme.colors.cardBorder)
                }
            }
        }
    }
}

@Composable
private fun DailyForecastRow(day: DailyWeatherUiModel) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = AppTheme.dimensions.spaceM),
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimensions.spaceXs),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (day.isToday) stringResource(R.string.today) else day.date,
                modifier = Modifier.weight(1f),
                style = AppTheme.typography.labelLarge,
            )
            ConditionMarker(condition = day.condition)
            Spacer(modifier = Modifier.width(AppTheme.dimensions.spaceS))
            Text(
                text = stringResource(R.string.temperature_celsius, day.maximumTemperatureCelsius),
                style = AppTheme.typography.labelLarge,
            )
            Spacer(modifier = Modifier.width(AppTheme.dimensions.spaceS))
            Text(
                text = stringResource(R.string.temperature_celsius, day.minimumTemperatureCelsius),
                style = AppTheme.typography.labelLarge,
                color = AppTheme.colors.textSecondary,
            )
        }
        Text(
            text = stringResource(day.condition.descriptionRes),
            style = AppTheme.typography.bodySmall,
            color = AppTheme.colors.textSecondary,
        )
        val precipitationProbability =
            stringResource(R.string.precipitation_probability_percent, day.precipitationProbabilityPercent)
        Text(
            text = stringResource(R.string.precipitation_chance, precipitationProbability),
            style = AppTheme.typography.bodySmall,
            color = AppTheme.colors.rain,
        )
    }
}

@Composable
private fun ConditionMarker(condition: WeatherConditionUiModel) {
    Text(
        text = stringResource(condition.icon.markerRes),
        style = AppTheme.typography.titleMedium,
    )
}

private val WeatherIcon.markerRes: Int
    @StringRes
    get() =
        when (this) {
            WeatherIcon.CLEAR -> R.string.weather_marker_clear
            WeatherIcon.PARTLY_CLOUDY, WeatherIcon.CLOUDY -> R.string.weather_marker_cloudy
            WeatherIcon.FOG -> R.string.weather_marker_fog
            WeatherIcon.RAIN -> R.string.weather_marker_rain
            WeatherIcon.FREEZING_RAIN, WeatherIcon.SNOW -> R.string.weather_marker_snow
            WeatherIcon.THUNDERSTORM -> R.string.weather_marker_thunderstorm
            WeatherIcon.UNKNOWN -> R.string.weather_marker_unknown
        }
