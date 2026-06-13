package com.example.mybookslibrary.data.repository

import com.example.mybookslibrary.data.local.UserPreferencesDataStore
import com.example.mybookslibrary.data.remote.FirestoreReviewDataSource
import com.example.mybookslibrary.data.remote.models.FirestoreReview
import com.google.firebase.auth.FirebaseUser
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.just
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewRepositoryTest {
    private val firestore = mockk<FirestoreReviewDataSource>()
    private val auth = mockk<AuthRepository>()
    private val prefs = mockk<UserPreferencesDataStore>()

    private fun repo() = ReviewRepository(firestore, auth, prefs)

    private fun loggedInAs(uid: String) {
        val user = mockk<FirebaseUser>(relaxed = true)
        every { user.uid } returns uid
        every { auth.getCurrentUser() } returns user
    }

    @Test
    fun isLoggedIn_guestTraVeFalse() {
        every { auth.getCurrentUser() } returns null

        assertFalse(repo().isLoggedIn())
    }

    @Test
    fun submitReview_guest_nemIllegalStateException() {
        every { auth.getCurrentUser() } returns null

        assertThrows(IllegalStateException::class.java) {
            kotlinx.coroutines.runBlocking {
                repo().submitReview("m1", rating = 5, title = "t", body = "b", fallbackAuthorName = "Ẩn danh")
            }
        }
    }

    @Test
    fun submitReview_moiTao_dungDisplayNameVaGioiHanKyTu() =
        runTest {
            loggedInAs("me")
            every { prefs.observeDisplayName() } returns flowOf("Thắng")
            val saved = slot<FirestoreReview>()
            coEvery { firestore.saveReview("m1", capture(saved)) } just Runs

            repo().submitReview(
                mangaId = "m1",
                rating = 9, // ngoài 1..5 -> coerce về 5
                title = " " + "a".repeat(200) + " ",
                body = "b".repeat(3000),
                fallbackAuthorName = "Ẩn danh",
            )

            assertEquals("me", saved.captured.authorUid)
            assertEquals(5, saved.captured.rating)
            assertEquals(ReviewRepository.TITLE_MAX_LENGTH, saved.captured.title.length)
            // trim phải xảy ra TRƯỚC take (không còn space đầu/cuối)
            assertFalse(saved.captured.title.startsWith(" "))
            assertEquals(ReviewRepository.BODY_MAX_LENGTH, saved.captured.body.length)
            assertEquals("Thắng", saved.captured.authorName)
            assertTrue(saved.captured.createdAt > 0)
            assertEquals(saved.captured.createdAt, saved.captured.updatedAt)
        }

    @Test
    fun submitReview_suaReviewCu_giuNguyenCreatedAt() =
        runTest {
            loggedInAs("me")
            every { prefs.observeDisplayName() } returns flowOf("")
            val saved = slot<FirestoreReview>()
            coEvery { firestore.saveReview("m1", capture(saved)) } just Runs

            repo().submitReview(
                "m1",
                rating = 4,
                title = "t",
                body = "b",
                fallbackAuthorName = "Ẩn danh",
                existingCreatedAt = 111L,
            )

            assertEquals(111L, saved.captured.createdAt)
            assertTrue(saved.captured.updatedAt > 111L)
            // displayName rỗng -> fallback đã localize từ caller
            assertEquals("Ẩn danh", saved.captured.authorName)
        }

    @Test
    fun deleteMyReview_guest_khongGoiFirestore() =
        runTest {
            every { auth.getCurrentUser() } returns null

            repo().deleteMyReview("m1")

            coVerify(exactly = 0) { firestore.deleteReview(any(), any()) }
        }

    @Test
    fun deleteMyReview_user_goiDungDoc() =
        runTest {
            loggedInAs("me")
            coEvery { firestore.deleteReview("m1", "me") } just Runs

            repo().deleteMyReview("m1")

            coVerify(exactly = 1) { firestore.deleteReview("m1", "me") }
        }
}
