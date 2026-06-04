package com.example.mybookslibrary

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented smoke test cho [MainActivity]:
 * - App khởi động không crash.
 * - Màn hình đầu tiên (Login hoặc Discover) hiển thị đúng.
 *
 * Chạy trên emulator thật qua `./gradlew connectedDebugAndroidTest`.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AppSmokeTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun app_launchesWithoutCrash() {
        // Chỉ cần app start và idle — không crash là pass.
        composeRule.waitForIdle()
    }

    @Test
    fun firstScreen_isDisplayed() {
        composeRule.waitForIdle()
        // App mở ra ở LoginScreen (chưa đăng nhập) hoặc DiscoverScreen (đã login).
        // Một trong hai text này phải xuất hiện.
        val loginVisible = runCatching {
            composeRule.onNodeWithText("Welcome Back!").assertIsDisplayed()
        }.isSuccess

        val discoverVisible = runCatching {
            composeRule.onNodeWithText("Discover").assertIsDisplayed()
        }.isSuccess

        assert(loginVisible || discoverVisible) {
            "Màn hình đầu tiên không phải Login cũng không phải Discover"
        }
    }
}
