package com.example.mybookslibrary.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.mybookslibrary.data.remote.models.FirestoreReview
import com.example.mybookslibrary.ui.viewmodel.ReviewAggregate
import com.example.mybookslibrary.ui.viewmodel.ReviewUiState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@Config(qualifiers = "w411dp-h4000dp-xxhdpi")
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MangaReviewScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun setContent(uiState: ReviewUiState, onWriteClick: () -> Unit = {}) {
        composeRule.setContent {
            MangaReviewScreenContent(
                uiState = uiState,
                snackbarHostState = SnackbarHostState(),
                onBackClick = {},
                onWriteClick = onWriteClick,
                onDeleteMyReview = {},
                onRetry = {},
            )
        }
    }

    @Test
    fun rendersWithBackButton_vaNutVietDanhGia() {
        setContent(ReviewUiState(isLoading = false))

        composeRule.onNodeWithContentDescription("Back").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Write a review").assertIsDisplayed()
    }

    @Test
    fun emptyState_hienKhiKhongCoReview() {
        setContent(ReviewUiState(isLoading = false))

        composeRule.onNodeWithText("No reviews yet").assertIsDisplayed()
    }

    @Test
    fun hienReviewCuaMinhVaNguoiKhac() {
        setContent(
            ReviewUiState(
                isLoading = false,
                myReview = FirestoreReview(
                    authorUid = "me",
                    rating = 5,
                    title = "Tuyệt",
                    body = "Hay",
                    authorName = "Tôi",
                    createdAt = 1L,
                ),
                otherReviews = listOf(
                    FirestoreReview(
                        authorUid = "u2",
                        rating = 4,
                        title = "Great",
                        body = "Nice art",
                        authorName = "User456",
                        createdAt = 2L,
                    ),
                ),
                aggregate = ReviewAggregate(average = 4.5, totalCount = 2),
                isLoggedIn = true,
            ),
        )

        composeRule.onNodeWithText("Your review").assertIsDisplayed()
        composeRule.onNodeWithText("Tuyệt").assertIsDisplayed()
        composeRule.onNodeWithText("Great").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Edit review").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Delete review").assertIsDisplayed()
    }

    @Test
    fun nutVietDanhGia_goiCallback() {
        var clicked = false
        setContent(ReviewUiState(isLoading = false), onWriteClick = { clicked = true })

        composeRule.onNodeWithContentDescription("Write a review").performClick()

        assertTrue(clicked)
    }

    @Test
    fun formSheet_chonSaoRoiGui_truyenDungRating() {
        var submitted: Triple<Int, String, String>? = null
        composeRule.setContent {
            ReviewFormSheetContent(
                initialReview = null,
                isSubmitting = false,
                onSubmit = { rating, title, body -> submitted = Triple(rating, title, body) },
            )
        }

        // Chọn 5 sao rồi gửi — rating bắt buộc, nút khóa khi chưa chọn
        composeRule.onNodeWithContentDescription("Rate 5 stars").performClick()
        composeRule.onNodeWithText("Submit").performClick()

        assertTrue(submitted?.first == 5)
    }
}
