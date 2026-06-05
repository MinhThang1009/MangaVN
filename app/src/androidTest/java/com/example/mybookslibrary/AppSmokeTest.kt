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
    fun firstScreen_rendersWithoutCrash() {
        // Verify UI render xong (bất kể màn nào — login hay discover) mà không crash.
        // Text cụ thể thay đổi theo trạng thái đăng nhập → không assert text.
        composeRule.waitForIdle()
        // Có ít nhất 1 node trong semantics tree = UI đã compose thành công.
        assert(composeRule.onAllNodes(androidx.compose.ui.test.isRoot()).fetchSemanticsNodes().isNotEmpty())
    }
}
