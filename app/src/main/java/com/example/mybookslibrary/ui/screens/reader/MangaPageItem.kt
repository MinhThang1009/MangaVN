package com.example.mybookslibrary.ui.screens.reader

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.mybookslibrary.ui.util.appString
import com.example.mybookslibrary.R

@Composable
fun MangaPageItem(
    imageUrl: String,
    index: Int,
    modifier: Modifier = Modifier
) {
    var aspectRatio by remember(imageUrl) { mutableStateOf<Float?>(null) }

    val imageModifier = if (aspectRatio != null) {
        modifier.fillMaxWidth().aspectRatio(aspectRatio!!)
    } else {
        modifier.fillMaxWidth().defaultMinSize(minHeight = 400.dp)
    }

    AsyncImage(
        model = imageUrl,
        contentDescription = appString(R.string.reader_page_description, index + 1),
        contentScale = ContentScale.FillWidth,
        onSuccess = { state ->
            val intrinsicSize = state.painter.intrinsicSize
            val width = intrinsicSize.width
            val height = intrinsicSize.height
            if (width > 0f && height > 0f) {
                aspectRatio = width / height
            }
        },
        modifier = imageModifier
    )
}


