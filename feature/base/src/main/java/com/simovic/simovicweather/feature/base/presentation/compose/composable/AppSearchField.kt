package com.simovic.simovicweather.feature.base.presentation.compose.composable

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import com.simovic.simovicweather.feature.base.R
import com.simovic.simovicweather.feature.base.presentation.ui.AppTheme
import kotlinx.coroutines.flow.filterIsInstance

@Composable
fun AppSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    onClear: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    onClick: (() -> Unit)? = null,
    onClickLabel: String? = null,
) {
    val focusManager = LocalFocusManager.current
    val effectiveOnClick = onClick.takeIf { enabled }
    val interactionSource = remember { MutableInteractionSource() }
    SearchFieldClickHandler(interactionSource = interactionSource, onClick = effectiveOnClick)

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.appSearchFieldModifier(effectiveOnClick, onClickLabel),
        enabled = enabled,
        readOnly = readOnly,
        interactionSource = interactionSource,
        textStyle = AppTheme.typography.bodyLarge,
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        placeholder = {
            Text(
                text = placeholder,
                style = AppTheme.typography.bodyLarge,
            )
        },
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = null,
            )
        },
        trailingIcon =
            if (value.isNotEmpty() && onClear != null) {
                {
                    IconButton(
                        onClick = onClear,
                        enabled = enabled,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = stringResource(R.string.clear_search),
                        )
                    }
                }
            } else {
                null
            },
        shape = RoundedCornerShape(AppTheme.dimensions.controlRadius),
        colors = appSearchFieldColors(),
    )
}

@Composable
private fun appSearchFieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedContainerColor = AppTheme.colors.cardSecondary,
        unfocusedContainerColor = AppTheme.colors.cardSecondary,
        disabledContainerColor = AppTheme.colors.disabledContainer,
        focusedBorderColor = AppTheme.colors.primary,
        unfocusedBorderColor = AppTheme.colors.cardBorder,
        disabledBorderColor = AppTheme.colors.cardBorder,
        focusedTextColor = AppTheme.colors.textPrimary,
        unfocusedTextColor = AppTheme.colors.textPrimary,
        disabledTextColor = AppTheme.colors.disabledContent,
        disabledPlaceholderColor = AppTheme.colors.disabledContent,
        disabledLeadingIconColor = AppTheme.colors.disabledContent,
        disabledTrailingIconColor = AppTheme.colors.disabledContent,
        cursorColor = AppTheme.colors.primary,
    )

@Composable
private fun SearchFieldClickHandler(
    interactionSource: MutableInteractionSource,
    onClick: (() -> Unit)?,
) {
    LaunchedEffect(interactionSource, onClick) {
        if (onClick != null) {
            interactionSource.interactions
                .filterIsInstance<PressInteraction.Release>()
                .collect { onClick() }
        }
    }
}

@Composable
private fun Modifier.appSearchFieldModifier(
    onClick: (() -> Unit)?,
    onClickLabel: String?,
): Modifier =
    fillMaxWidth()
        .heightIn(min = AppTheme.dimensions.controlHeight)
        .then(
            if (onClick != null) {
                Modifier.semantics {
                    role = Role.Button
                    onClick(label = onClickLabel) {
                        onClick()
                        true
                    }
                }
            } else {
                Modifier
            },
        )

@PreviewAppThemes
@Composable
private fun AppSearchFieldEmptyPreview() {
    AppPreview {
        AppSearchField(
            value = "",
            onValueChange = {},
            placeholder = "Search for a city",
            onClear = {},
        )
    }
}

@PreviewAppThemes
@Composable
private fun AppSearchFieldPopulatedPreview() {
    AppPreview {
        AppSearchField(
            value = "Sarajevo",
            onValueChange = {},
            placeholder = "Search for a city",
            onClear = {},
        )
    }
}
