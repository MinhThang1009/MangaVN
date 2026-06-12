package com.example.mybookslibrary.ui.screens.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.example.mybookslibrary.R
import com.example.mybookslibrary.ui.screens.components.LoadingIndicator
import com.example.mybookslibrary.ui.screens.components.LoadingSize
import com.example.mybookslibrary.ui.theme.Alphas
import com.example.mybookslibrary.ui.util.appString

@Composable
internal fun ReaderPageLoadingOverlay(
    pageNumber: Int,
    modifier: Modifier = Modifier,
) {
    val loadingDescription = appString(R.string.reader_loading_page, pageNumber)
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = Alphas.EmphasisMedium))
                .semantics { contentDescription = loadingDescription },
        contentAlignment = Alignment.Center,
    ) {
        LoadingIndicator(
            size = LoadingSize.Medium,
            color = Color.White,
        )
    }
}
