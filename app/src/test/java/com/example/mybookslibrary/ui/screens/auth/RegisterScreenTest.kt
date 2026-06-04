package com.example.mybookslibrary.ui.screens.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.mybookslibrary.data.repository.AuthRepository
import com.example.mybookslibrary.ui.viewmodel.AuthViewModel
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

/**
 * UI test cho [RegisterScreen] qua Robolectric + Compose (JVM, không cần Hilt):
 * truyền [AuthViewModel] thật với [AuthRepository] giả vào param viewModel.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class RegisterScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private fun viewModel() = AuthViewModel(mockk<AuthRepository>(relaxed = true))

    @Test
    fun rendersTitleAndFields() {
        composeRule.setContent {
            RegisterScreen(onRegisterSuccess = {}, onNavigateToLogin = {}, viewModel = viewModel())
        }

        composeRule.onNodeWithText("Create an Account").assertIsDisplayed()
        composeRule.onNodeWithText("Username").assertIsDisplayed()
        composeRule.onNodeWithText("Password").assertIsDisplayed()
        composeRule.onNodeWithText("Confirm Password").assertIsDisplayed()
    }

    @Test
    fun registerButton_disabledWhenEmpty() {
        composeRule.setContent {
            RegisterScreen(onRegisterSuccess = {}, onNavigateToLogin = {}, viewModel = viewModel())
        }

        // Button disabled khi không nhập gì
        composeRule.onNode(hasText("Register") and hasClickAction()).assertIsNotEnabled()
    }

    @Test
    fun passwordMismatch_showsError() {
        composeRule.setContent {
            RegisterScreen(onRegisterSuccess = {}, onNavigateToLogin = {}, viewModel = viewModel())
        }

        composeRule.onNodeWithText("Username").performTextInput("user1")
        composeRule.onNodeWithText("Password").performTextInput("pass123")
        composeRule.onNodeWithText("Confirm Password").performTextInput("different")

        composeRule.onNodeWithText("Passwords do not match").assertIsDisplayed()
    }

    @Test
    fun passwordMatch_buttonEnabled() {
        composeRule.setContent {
            RegisterScreen(onRegisterSuccess = {}, onNavigateToLogin = {}, viewModel = viewModel())
        }

        composeRule.onNodeWithText("Username").performTextInput("user1")
        composeRule.onNodeWithText("Password").performTextInput("pass123")
        composeRule.onNodeWithText("Confirm Password").performTextInput("pass123")

        composeRule.onNode(hasText("Register") and hasClickAction()).assertIsEnabled()
    }

    @Test
    fun togglePasswordVisibility() {
        composeRule.setContent {
            RegisterScreen(onRegisterSuccess = {}, onNavigateToLogin = {}, viewModel = viewModel())
        }

        composeRule.onNodeWithContentDescription("Show password").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Show password").performClick()
        composeRule.onNodeWithContentDescription("Hide password").assertIsDisplayed()
    }

    @Test
    fun loginLink_exists() {
        composeRule.setContent {
            RegisterScreen(onRegisterSuccess = {}, onNavigateToLogin = {}, viewModel = viewModel())
        }

        // TextButton ở cuối Column — viewport nhỏ có thể chưa scroll tới → assertExists (đã compose)
        composeRule.onNodeWithText("Already have an account? Login").assertExists()
    }
}
