package com.simovic.simovicweather.feature.weather.presentation.screen.weather

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
        item { CurrentWeatherHero(weather = weather) }
        item { WeatherDetails(current = weather.current) }
        item { DailyForecast(days = weather.days) }
    }
}

@Composable
private fun CurrentWeatherHero(weather: WeatherUiModel) {
    val current = weather.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimensions.spaceS),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.temperature_celsius, current.temperatureCelsius),
                style = AppTheme.typography.displayLarge,
            )
            ConditionMarker(condition = current.condition, isHero = true)
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
        TodayTemperatureRange(weather)
        Text(
            text = stringResource(R.string.updated_at, weather.updatedAt),
            style = AppTheme.typography.bodySmall,
            color = AppTheme.colors.textSecondary,
        )
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

@Composable
private fun WeatherDetails(current: CurrentWeatherUiModel) {
    WeatherCard(
        modifier = Modifier.fillMaxWidth(),
        contentSpacing = AppTheme.dimensions.spaceL,
    ) {
        MetricPair(
            first =
                WeatherMetric(
                    R.string.humidity,
                    stringResource(R.string.humidity_percent, current.humidityPercent),
                    R.drawable.ic_humidity,
                ),
            second =
                WeatherMetric(
                    R.string.wind,
                    stringResource(
                        R.string.wind_speed_kilometers_per_hour,
                        current.windSpeedKilometersPerHour,
                    ),
                    R.drawable.ic_wind,
                ),
        )
        MetricPair(
            first =
                WeatherMetric(
                    R.string.precipitation,
                    stringResource(R.string.precipitation_millimeters, current.precipitationMillimeters),
                    R.drawable.ic_precipitation,
                ),
            second =
                WeatherMetric(
                    R.string.pressure,
                    stringResource(R.string.pressure_hectopascals, current.pressureHectopascals),
                    R.drawable.ic_pressure,
                ),
        )
    }
}

@Composable
private fun MetricPair(
    first: WeatherMetric,
    second: WeatherMetric,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimensions.spaceL),
    ) {
        MetricItem(metric = first, modifier = Modifier.weight(1f))
        MetricItem(metric = second, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun MetricItem(
    metric: WeatherMetric,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimensions.spaceXs),
    ) {
        Icon(
            painter = painterResource(metric.iconRes),
            contentDescription = null,
            modifier = Modifier.size(AppTheme.dimensions.iconSize),
            tint = AppTheme.colors.primary,
        )
        Text(
            text = stringResource(metric.labelRes),
            style = AppTheme.typography.bodySmall,
            color = AppTheme.colors.textSecondary,
        )
        Text(text = metric.value, style = AppTheme.typography.titleMedium)
    }
}

@Composable
private fun DailyForecast(days: List<DailyWeatherUiModel>) {
    WeatherCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTheme.dimensions.spaceS),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_calendar),
                contentDescription = null,
                modifier = Modifier.size(AppTheme.dimensions.iconSize),
                tint = AppTheme.colors.primary,
            )
            Text(
                text = stringResource(R.string.seven_day_forecast),
                style = AppTheme.typography.titleMedium,
            )
        }
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
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = AppTheme.dimensions.spaceM),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (day.isToday) stringResource(R.string.today) else day.date,
            modifier = Modifier.weight(1f),
            style = AppTheme.typography.labelLarge,
            maxLines = 1,
        )
        ConditionMarker(
            condition = day.condition,
            contentDescription = stringResource(day.condition.descriptionRes),
        )
        Spacer(modifier = Modifier.width(AppTheme.dimensions.spaceS))
        PrecipitationProbability(
            value =
                stringResource(
                    R.string.precipitation_probability_percent,
                    day.precipitationProbabilityPercent,
                ),
        )
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
}

@Composable
private fun PrecipitationProbability(value: String) {
    val description = stringResource(R.string.precipitation_chance, value)
    Text(
        text = value,
        modifier = Modifier.semantics { contentDescription = description },
        style = AppTheme.typography.bodySmall,
        color = AppTheme.colors.rain,
    )
}

@Composable
private fun ConditionMarker(
    condition: WeatherConditionUiModel,
    isHero: Boolean = false,
    contentDescription: String? = null,
) {
    Box(
        modifier =
            if (isHero) {
                Modifier.size(AppTheme.dimensions.conditionArtworkSize)
            } else {
                Modifier.size(AppTheme.dimensions.iconSize)
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(condition.icon.markerRes),
            modifier =
                Modifier.clearAndSetSemantics {
                    if (contentDescription != null) {
                        this.contentDescription = contentDescription
                    }
                },
            style = if (isHero) AppTheme.typography.displayLarge else AppTheme.typography.titleMedium,
            color = condition.icon.markerColor,
        )
    }
}

private data class WeatherMetric(
    val labelRes: Int,
    val value: String,
    val iconRes: Int,
)

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

private val WeatherIcon.markerColor: Color
    @Composable
    get() =
        when (this) {
            WeatherIcon.CLEAR -> AppTheme.colors.sun
            WeatherIcon.RAIN, WeatherIcon.FREEZING_RAIN, WeatherIcon.SNOW -> AppTheme.colors.rain
            else -> AppTheme.colors.textPrimary
        }
