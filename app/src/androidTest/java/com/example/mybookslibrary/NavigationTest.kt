package com.example.mybookslibrary

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import com.example.mybookslibrary.data.local.UserPreferencesDataStore

/**
 * Instrumented navigation test:
 * - App chưa login → màn Login hiển thị.
 * - Đăng ký account mới → navigate đúng màn.
 * - SignOut (loggedInUserId → null) → điều hướng về Login.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class NavigationTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var preferencesDataStore: UserPreferencesDataStore

    @Before
    fun setUp() {
        hiltRule.inject()
        // Đảm bảo không có user đang đăng nhập → màn đầu = Login
        runBlocking { preferencesDataStore.setLoggedInUserId(null) }
    }

    @Test
    fun notLoggedIn_showsLoginScreen() {
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Welcome Back!").assertIsDisplayed()
    }

    @Test
    fun loginScreen_hasUsernameAndPasswordFields() {
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Username").assertIsDisplayed()
        composeRule.onNodeWithText("Password").assertIsDisplayed()
    }

    @Test
    fun loginScreen_navigateToRegister() {
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Don't have an account? Register").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Create an Account").assertIsDisplayed()
    }

    @Test
    fun registerScreen_backToLogin() {
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Don't have an account? Register").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Already have an account? Login").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Welcome Back!").assertIsDisplayed()
    }

    @Test
    fun signOut_navigatesToLogin() {
        // Giả lập đã đăng nhập: set userId rồi recreate activity
        runBlocking { preferencesDataStore.setLoggedInUserId("test-user-123") }
        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()

        // Đăng xuất (xóa userId)
        runBlocking { preferencesDataStore.setLoggedInUserId(null) }
        composeRule.waitForIdle()

        // Phải về màn Login
        composeRule.onNodeWithText("Welcome Back!").assertIsDisplayed()
    }
}
