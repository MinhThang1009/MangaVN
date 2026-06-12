package com.example.mybookslibrary.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Isolated Compose UI tests cho [DetailShareButton] — chạy trên JVM (Robolectric),
 * chuyển từ androidTest xuống vì không cần Hilt/Intent/emulator.
 * Pattern: truyền callback và verify nó được gọi khi click.
 */
@RunWith(AndroidJUnit4::class)
class MangaDetailShareButtonTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun shareButton_isDisplayed() {
        composeRule.setContent {
            MaterialTheme {
                DetailShareButton(onShareClick = {})
            }
        }
        composeRule
            .onNodeWithContentDescription("Share")
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
            .onNodeWithContentDescription("Share")
            .performClick()
        assertTrue("onShareClick phải được gọi khi nhấn share button", clicked)
    }
}
