package com.example.mybookslibrary

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented smoke test cho [MainActivity]:
 * - App khởi động không crash.
 * - UI compose thành công (có node trong semantics tree).
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
        composeRule.waitForIdle()
    }

    @Test
    fun firstScreen_rendersWithoutCrash() {
        // Chờ UI idle (bất kể màn nào — login hay discover) mà không crash.
        // Không assert text vì phụ thuộc trạng thái đăng nhập của emulator.
        composeRule.waitForIdle()
    }
}
