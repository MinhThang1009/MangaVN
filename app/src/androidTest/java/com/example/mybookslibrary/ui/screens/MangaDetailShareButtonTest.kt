package com.example.mybookslibrary.ui.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Isolated Compose UI tests cho [DetailShareButton].
 * Không dùng Hilt/ViewModel — chỉ test behavior của UI component thuần.
 * Pattern: truyền callback và verify nó được gọi khi click.
 */
@RunWith(AndroidJUnit4::class)
class MangaDetailShareButtonTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun shareButton_isDisplayed() {
        composeRule.setContent {
            MaterialTheme {
                DetailShareButton(onShareClick = {})
            }
        }
        composeRule
            .onNodeWithContentDescription("Share manga")
            .assertIsDisplayed()
    }

    @Test
    fun shareButton_click_triggersCallback() {
        var clicked = false
        composeRule.setContent {
            MaterialTheme {
                DetailShareButton(onShareClick = { clicked = true })
            }
        }
        composeRule
            .onNodeWithContentDescription("Share manga")
            .performClick()
        assertTrue("onShareClick phải được gọi khi nhấn share button", clicked)
    }
}
