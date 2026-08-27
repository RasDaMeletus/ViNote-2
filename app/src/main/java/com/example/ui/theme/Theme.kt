package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ViNotePrimaryContainer,
    onPrimary = ViNoteOnPrimary,
    primaryContainer = ViNotePrimary,
    onPrimaryContainer = ViNoteOnPrimaryContainer,
    secondary = ViNoteSecondaryFixed,
    onSecondary = ViNoteOnSecondaryFixed,
    background = ViNoteDarkSurface,
    onBackground = ViNoteSurfaceContainerLowest,
    surface = ViNoteDarkSurface,
    onSurface = ViNoteSurfaceContainerLowest,
    surfaceVariant = ViNoteSurfaceContainerHigh,
    onSurfaceVariant = ViNoteOutlineVariant,
    error = ViNoteError,
    onError = ViNoteOnError,
    errorContainer = ViNoteErrorContainer,
    onErrorContainer = ViNoteOnErrorContainer
)

private val LightColorScheme = lightColorScheme(
    primary = ViNotePrimary,
    onPrimary = ViNoteOnPrimary,
    primaryContainer = ViNotePrimaryContainer,
    onPrimaryContainer = ViNoteOnPrimaryContainer,
    secondary = ViNoteSecondary,
    onSecondary = ViNoteOnPrimary,
    secondaryContainer = ViNoteSecondaryFixed,
    onSecondaryContainer = ViNoteOnSecondaryFixedVariant,
    tertiary = ViNoteTertiary,
    onTertiary = ViNoteOnPrimary,
    tertiaryContainer = ViNoteTertiaryContainer,
    onTertiaryContainer = ViNoteTertiaryFixed,
    background = ViNoteSurface,
    onBackground = ViNoteOnSurface,
    surface = ViNoteSurface,
    onSurface = ViNoteOnSurface,
    surfaceVariant = ViNoteSurfaceContainer,
    onSurfaceVariant = ViNoteOnSurfaceVariant,
    error = ViNoteError,
    onError = ViNoteOnError,
    errorContainer = ViNoteErrorContainer,
    onErrorContainer = ViNoteOnErrorContainer
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
