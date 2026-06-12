package com.example.mybookslibrary.ui.screens.reader

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.example.mybookslibrary.domain.model.ReadingMode
import com.example.mybookslibrary.ui.viewmodel.ReaderUiEffect
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ReaderPageNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun navigateToPage_verticalMode_scrollsLazyList() {
        val effects = MutableSharedFlow<ReaderUiEffect>(extraBufferCapacity = 1)
        var currentPage = -1

        composeRule.setContent {
            val listState = rememberLazyListState()
            val pagerState = rememberPagerState(pageCount = { 5 })

            LazyColumn(Modifier.fillMaxSize(), state = listState) {
                items((0..4).toList()) {
                    Box(Modifier.height(500.dp))
                }
            }
            ReaderEffectHandler(
                effects = effects,
                listState = listState,
                pagerState = pagerState,
                currentReadingMode = ReadingMode.VERTICAL,
                onEvent = {},
                snackbarHostState = remember { SnackbarHostState() },
            )
            LaunchedEffect(listState.firstVisibleItemIndex) {
                currentPage = listState.firstVisibleItemIndex
            }
        }

        composeRule.runOnIdle {
            effects.tryEmit(ReaderUiEffect.NavigateToPage(3))
        }
        composeRule.waitUntil(timeoutMillis = 5_000) { currentPage == 3 }
    }

    @Test
    fun navigateToPage_horizontalMode_scrollsPager() {
        val effects = MutableSharedFlow<ReaderUiEffect>(extraBufferCapacity = 1)
        var currentPage = -1

        composeRule.setContent {
            val listState = rememberLazyListState()
            val pagerState = rememberPagerState(pageCount = { 5 })

            HorizontalPager(state = pagerState) {}
            ReaderEffectHandler(
                effects = effects,
                listState = listState,
                pagerState = pagerState,
                currentReadingMode = ReadingMode.LTR,
                onEvent = {},
                snackbarHostState = remember { SnackbarHostState() },
            )
            LaunchedEffect(pagerState.settledPage) {
                currentPage = pagerState.settledPage
            }
        }

        composeRule.runOnIdle {
            effects.tryEmit(ReaderUiEffect.NavigateToPage(3))
        }
        composeRule.waitUntil(timeoutMillis = 5_000) { currentPage == 3 }
    }
}
