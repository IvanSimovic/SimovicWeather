package com.simovic.simovicweather.feature.base.presentation.ui

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class WeatherDimensions(
    val spaceXs: Dp = 4.dp,
    val spaceS: Dp = 8.dp,
    val spaceM: Dp = 12.dp,
    val spaceL: Dp = 16.dp,
    val spaceXl: Dp = 20.dp,
    val spaceXxl: Dp = 24.dp,
    val spaceXxxl: Dp = 32.dp,
    val screenPadding: Dp = 20.dp,
    val cardRadius: Dp = 24.dp,
    val cardBorderWidth: Dp = 1.dp,
    val controlRadius: Dp = 16.dp,
    val controlHeight: Dp = 52.dp,
    val iconSize: Dp = 24.dp,
    val conditionArtworkSize: Dp = 96.dp,
)

internal val AppWeatherDimensions = WeatherDimensions()
