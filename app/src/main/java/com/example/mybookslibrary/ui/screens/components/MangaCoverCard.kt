@file:OptIn(ExperimentalTextApi::class)

package com.example.mybookslibrary.ui.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.mybookslibrary.ui.theme.CoverShape
import com.example.mybookslibrary.ui.theme.MyBooksLibraryTheme

/**
 * Ô bìa manga — ratio 2:3 DUY NHẤT toàn app, shape + placeholder thống nhất.
 * Crossfade 200ms qua Coil mặc định; placeholder surfaceContainer khi chưa load.
 */
@Composable
fun MangaCoverCard(
    coverUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    width: Dp = 120.dp,
) {
    Box(
        modifier =
            modifier
                .width(width)
                .aspectRatio(2f / 3f)
                .clip(CoverShape)
                .background(MaterialTheme.colorScheme.surfaceContainer),
    ) {
        if (coverUrl != null) {
            AsyncImage(
                model = coverUrl,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Suppress("UnusedPrivateMember")
@Preview(name = "Cover — dark", showBackground = true)
@Composable
private fun MangaCoverCardDarkPreview() {
    MyBooksLibraryTheme(darkTheme = true) {
        MangaCoverCard(coverUrl = null, contentDescription = "Placeholder")
    }
}

@Suppress("UnusedPrivateMember")
@Preview(name = "Cover — light", showBackground = true)
@Composable
private fun MangaCoverCardLightPreview() {
    MyBooksLibraryTheme(darkTheme = false) {
        MangaCoverCard(coverUrl = null, contentDescription = "Placeholder")
    }
}

@Suppress("UnusedPrivateMember")
@Preview(name = "Cover — 320dp", widthDp = 320, showBackground = true)
@Composable
private fun MangaCoverCard320Preview() {
    MyBooksLibraryTheme { MangaCoverCard(coverUrl = null, contentDescription = null) }
}

@Suppress("UnusedPrivateMember")
@Preview(name = "Cover — landscape", widthDp = 640, heightDp = 320, showBackground = true)
@Composable
private fun MangaCoverCardLandscapePreview() {
    MyBooksLibraryTheme { MangaCoverCard(coverUrl = null, contentDescription = null) }
}
