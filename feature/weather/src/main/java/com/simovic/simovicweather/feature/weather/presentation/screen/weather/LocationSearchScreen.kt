package com.simovic.simovicweather.feature.weather.presentation.screen.weather

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simovic.simovicweather.feature.base.presentation.compose.composable.AppButton
import com.simovic.simovicweather.feature.base.presentation.compose.composable.AppSearchField
import com.simovic.simovicweather.feature.base.presentation.compose.composable.LoadingIndicator
import com.simovic.simovicweather.feature.base.presentation.compose.composable.WeatherBackground
import com.simovic.simovicweather.feature.base.presentation.compose.composable.WeatherCard
import com.simovic.simovicweather.feature.base.presentation.ui.AppTheme
import com.simovic.simovicweather.feature.weather.R
import com.simovic.simovicweather.feature.weather.presentation.navigation.LocationSearchResult
import org.koin.androidx.compose.koinViewModel

@Composable
fun LocationSearchScreen(
    shouldRequestFocus: Boolean,
    onClose: () -> Unit,
    onResult: (LocationSearchResult) -> Unit,
) {
    val viewModel = koinViewModel<LocationSearchViewModel>()
    val search by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                onResult(LocationSearchResult.CurrentLocation)
            } else {
                viewModel.onLocationPermissionDenied()
            }
        }

    LocationSearchContent(
        search = search,
        shouldRequestFocus = shouldRequestFocus,
        onQueryChange = viewModel::onQueryChanged,
        onSearchClear = { viewModel.onQueryChanged("") },
        onSearchClose = onClose,
        onRetry = viewModel::retry,
        onLocationSelect = { onResult(it.toLocationSearchResult()) },
        onUseCurrentLocation = {
            val isGranted =
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED
            if (isGranted) {
                onResult(LocationSearchResult.CurrentLocation)
            } else {
                permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
            }
        },
    )
}

@Composable
internal fun LocationSearchContent(
    search: LocationSearchUiState,
    shouldRequestFocus: Boolean,
    onQueryChange: (String) -> Unit,
    onSearchClear: () -> Unit,
    onSearchClose: () -> Unit,
    onRetry: () -> Unit,
    onLocationSelect: (LocationUiModel) -> Unit,
    onUseCurrentLocation: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    SearchFieldFocusEffect(shouldRequestFocus, focusRequester)

    WeatherBackground {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(
                        horizontal = AppTheme.dimensions.screenPadding,
                        vertical = AppTheme.dimensions.spaceXxxl,
                    ),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimensions.spaceL),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.choose_location),
                    style = AppTheme.typography.headlineMedium,
                )
                TextButton(onClick = onSearchClose) {
                    Text(text = stringResource(R.string.close))
                }
            }
            AppSearchField(
                value = search.query,
                onValueChange = onQueryChange,
                placeholder = stringResource(R.string.search_city),
                onClear = onSearchClear,
                modifier = Modifier.focusRequester(focusRequester),
            )
            AppButton(
                text = stringResource(R.string.use_current_location),
                onClick = onUseCurrentLocation,
                modifier = Modifier.fillMaxWidth(),
            )
            if (search.isLocationPermissionDenied) {
                PermissionDeniedMessage()
            }
            LocationSearchStatusContent(
                status = search.status,
                onRetry = onRetry,
                onLocationSelect = onLocationSelect,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PermissionDeniedMessage() {
    WeatherCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(AppTheme.dimensions.spaceL),
        contentSpacing = AppTheme.dimensions.spaceXs,
    ) {
        Text(
            text = stringResource(R.string.location_permission_search_fallback),
            style = AppTheme.typography.bodySmall,
            color = AppTheme.colors.textSecondary,
        )
    }
}

@Composable
private fun SearchFieldFocusEffect(
    shouldRequestFocus: Boolean,
    focusRequester: FocusRequester,
) {
    LaunchedEffect(shouldRequestFocus) {
        if (shouldRequestFocus) {
            focusRequester.requestFocus()
        }
    }
}

@Composable
private fun LocationSearchStatusContent(
    status: LocationSearchStatus,
    onRetry: () -> Unit,
    onLocationSelect: (LocationUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (status) {
        LocationSearchStatus.Idle -> Unit
        LocationSearchStatus.QueryTooShort -> SearchMessage(R.string.search_city_minimum_characters, modifier)
        LocationSearchStatus.Searching -> LoadingIndicator(modifier = modifier)
        is LocationSearchStatus.Results ->
            SearchResults(
                locations = status.locations,
                onLocationSelect = onLocationSelect,
                modifier = modifier,
            )
        LocationSearchStatus.NoResults -> SearchMessage(R.string.search_city_no_results, modifier)
        is LocationSearchStatus.Failed -> SearchFailure(status.reason, onRetry, modifier)
    }
}

@Composable
private fun SearchResults(
    locations: List<LocationUiModel>,
    onLocationSelect: (LocationUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    WeatherCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = AppTheme.dimensions.spaceL),
        contentSpacing = AppTheme.dimensions.spaceXs,
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            itemsIndexed(locations) { index, location ->
                LocationResultRow(location = location, onClick = { onLocationSelect(location) })
                if (index != locations.lastIndex) {
                    HorizontalDivider(color = AppTheme.colors.cardBorder)
                }
            }
        }
    }
}

@Composable
private fun SearchFailure(
    reason: WeatherErrorReason,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WeatherCard(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(reason.messageRes),
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.colors.textSecondary,
        )
        AppButton(
            text = stringResource(R.string.retry),
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SearchMessage(
    messageRes: Int,
    modifier: Modifier = Modifier,
) {
    WeatherCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(AppTheme.dimensions.spaceL),
    ) {
        Text(
            text = stringResource(messageRes),
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.colors.textSecondary,
        )
    }
}

@Composable
private fun LocationResultRow(
    location: LocationUiModel,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = AppTheme.dimensions.controlHeight)
                .clickable(role = Role.Button, onClick = onClick)
                .semantics(mergeDescendants = true) {}
                .padding(vertical = AppTheme.dimensions.spaceM),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = location.label,
            style = AppTheme.typography.bodyLarge,
        )
    }
}
