package com.example.mybookslibrary

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mybookslibrary.data.local.UserPreferencesDataStore
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Instrumented navigation test dùng HiltAndroidTest + ActivityScenario.
 * DataStore set null trước khi launch → NavHost bắt đầu ở Login (initialValue = null).
 * Không cần conditional pattern vì initial state đã xác định.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class NavigationTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    // createEmptyComposeRule: không launch Activity tự động — ActivityScenario.launch() được gọi
    // trong từng test sau khi DataStore đã được set, tránh race condition với initialValue = null.
    @get:Rule(order = 1)
    val composeRule = createEmptyComposeRule()

    @Inject
    lateinit var preferencesDataStore: UserPreferencesDataStore

    @Before
    fun setUp() {
        hiltRule.inject()
        runBlocking { preferencesDataStore.setLoggedInUserId(null) }
    }

    @After
    fun tearDown() {
        runBlocking { preferencesDataStore.setLoggedInUserId(null) }
    }

    @Test
    fun app_notLoggedIn_showsLoginScreen() {
        ActivityScenario.launch(MainActivity::class.java).use {
            composeRule.waitForIdle()
            composeRule.onNodeWithText("Welcome Back!").assertIsDisplayed()
        }
    }

    @Test
    fun loginScreen_hasNavigationToRegister() {
        ActivityScenario.launch(MainActivity::class.java).use {
            composeRule.waitForIdle()
            composeRule.onNodeWithText("Don't have an account? Register").assertIsDisplayed()
            composeRule.onNodeWithText("Don't have an account? Register").performClick()
            composeRule.waitForIdle()
            composeRule.onNodeWithText("Create an Account").assertIsDisplayed()
        }
    }

    @Test
    fun registerScreen_hasNavigationBackToLogin() {
        ActivityScenario.launch(MainActivity::class.java).use {
            composeRule.waitForIdle()
            composeRule.onNodeWithText("Don't have an account? Register").performClick()
            composeRule.waitForIdle()
            composeRule.onNodeWithText("Already have an account? Login").performClick()
            composeRule.waitForIdle()
            composeRule.onNodeWithText("Welcome Back!").assertIsDisplayed()
        }
    }

    @Test
    fun signOut_withNullUserId_showsLoginScreen() {
        // Set null trước launch (đảm bảo deterministic) rồi verify Login hiển thị
        // và vẫn hiển thị sau khi set null lại (sign-out flow không crash).
        ActivityScenario.launch(MainActivity::class.java).use {
            composeRule.waitForIdle()
            composeRule.onNodeWithText("Welcome Back!").assertIsDisplayed()
            runBlocking { preferencesDataStore.setLoggedInUserId(null) }
            composeRule.waitForIdle()
            composeRule.onNodeWithText("Welcome Back!").assertIsDisplayed()
        }
    }
}
