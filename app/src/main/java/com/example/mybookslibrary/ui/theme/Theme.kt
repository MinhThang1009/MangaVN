package com.example.mybookslibrary.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val KansoColorScheme = lightColorScheme(
    primary = KansoInk,
    onPrimary = KansoCard,
    primaryContainer = KansoInk,
    onPrimaryContainer = KansoCard,
    secondary = KansoGraphite,
    onSecondary = KansoCard,
    secondaryContainer = KansoPaper,
    onSecondaryContainer = KansoSoftInk,
    tertiary = KansoTerracotta,
    onTertiary = KansoCard,
    background = KansoPaper,
    onBackground = KansoSoftInk,
    surface = KansoCard,
    onSurface = KansoSoftInk,
    surfaceVariant = KansoPaper,
    onSurfaceVariant = KansoGraphite,
    error = KansoTerracotta,
    onError = KansoCard,
    outline = KansoGraphite,
    outlineVariant = KansoCard
)

@Composable
fun MyBooksLibraryTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = KansoColorScheme,
        typography = KansoTypography,
        content = content
    )
}
