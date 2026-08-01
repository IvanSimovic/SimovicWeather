package com.simovic.simovicweather.feature.base.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface NavigationScreens {
    @Serializable
    data object Weather : NavigationScreens

    @Serializable
    data object LocationSearch : NavigationScreens
}
