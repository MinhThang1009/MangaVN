package com.example.mybookslibrary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Menu
import com.composables.icons.lucide.Settings
import com.composables.icons.lucide.User
import com.example.mybookslibrary.R
import com.example.mybookslibrary.ui.navigation.LucideSearchIcon
import com.example.mybookslibrary.ui.screens.components.DiscoverSkeletonLoading
import com.example.mybookslibrary.ui.screens.components.ErrorState
import com.example.mybookslibrary.ui.screens.components.StyledDropdownMenu
import com.example.mybookslibrary.ui.theme.Dimens
import com.example.mybookslibrary.ui.util.appString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditorialTopBar(
    onSearchClick: () -> Unit = {},
    onLibraryClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
) {
    val menuExpanded = remember { mutableStateOf(false) }

    TopAppBar(
        title = { BrandTitle() },
        navigationIcon = {
            DiscoverNavigationMenu(
                expanded = menuExpanded.value,
                onExpandedChange = { menuExpanded.value = it },
                onLibraryClick = onLibraryClick,
                onProfileClick = onProfileClick,
            )
        },
        actions = {
            DiscoverTopBarActions(
                onSearchClick = onSearchClick,
                onProfileClick = onProfileClick,
            )
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                scrolledContainerColor = MaterialTheme.colorScheme.background,
            ),
    )
}

@Composable
private fun BrandTitle() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Lucide.BookOpen,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.width(Dimens.SpacingSm))
        Text(
            appString(R.string.brand_name),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun DiscoverNavigationMenu(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onLibraryClick: () -> Unit,
    onProfileClick: () -> Unit,
) {
    Box {
        IconButton(onClick = { onExpandedChange(true) }) {
            Icon(Lucide.Menu, appString(R.string.cd_menu), tint = MaterialTheme.colorScheme.primary)
        }
        StyledDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
        ) {
            DropdownMenuItem(
                text = { Text(appString(R.string.nav_library), style = MaterialTheme.typography.bodyLarge) },
                onClick = {
                    onExpandedChange(false)
                    onLibraryClick()
                },
                leadingIcon = { Icon(Lucide.BookOpen, null, tint = MaterialTheme.colorScheme.primary) },
            )
            DropdownMenuItem(
                text = { Text(appString(R.string.settings_title), style = MaterialTheme.typography.bodyLarge) },
                onClick = {
                    onExpandedChange(false)
                    onProfileClick()
                },
                leadingIcon = { Icon(Lucide.Settings, null, tint = MaterialTheme.colorScheme.primary) },
            )
        }
    }
}

@Composable
private fun DiscoverTopBarActions(onSearchClick: () -> Unit, onProfileClick: () -> Unit) {
    IconButton(onClick = onSearchClick) {
        Icon(LucideSearchIcon, appString(R.string.cd_search), tint = MaterialTheme.colorScheme.primary)
    }
    IconButton(onClick = onProfileClick) {
        Box(
            modifier =
                Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Lucide.User,
                appString(R.string.cd_profile),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
internal fun DiscoverLoadingState(modifier: Modifier = Modifier) {
    DiscoverSkeletonLoading(modifier)
}

@Composable
internal fun DiscoverErrorState(modifier: Modifier = Modifier, onRetry: () -> Unit) {
    ErrorState(
        message = appString(R.string.discover_error_title),
        modifier = modifier,
        onRetry = onRetry,
    )
}
