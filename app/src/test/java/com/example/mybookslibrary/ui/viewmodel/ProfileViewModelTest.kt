package com.example.mybookslibrary.ui.viewmodel

import com.example.mybookslibrary.data.local.UserPreferencesDataStore
import com.example.mybookslibrary.data.local.dao.ChapterDao
import com.example.mybookslibrary.data.local.dao.LibraryDao
import com.example.mybookslibrary.data.repository.AuthRepository
import com.example.mybookslibrary.test.MainDispatcherRule
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val prefs = mockk<UserPreferencesDataStore>(relaxed = true)
    private val libraryDao = mockk<LibraryDao>(relaxed = true)
    private val chapterDao = mockk<ChapterDao>(relaxed = true)
    private val authRepository = mockk<AuthRepository>(relaxed = true)

    private fun build(user: FirebaseUser? = null): ProfileViewModel {
        every { authRepository.getCurrentUser() } returns user
        every { prefs.observeDisplayName() } returns flowOf("Thắng")
        every { prefs.observeAvatarUri() } returns flowOf("content://photo/1")
        every { libraryDao.observeCount() } returns flowOf(5)
        every { libraryDao.observeReadCount() } returns flowOf(3)
        every { chapterDao.observeCompletedChapterCount() } returns flowOf(10)
        return ProfileViewModel(prefs, libraryDao, chapterDao, authRepository)
    }

    @Test
    fun uiState_combineAllSources() =
        runTest(mainDispatcherRule.dispatcher.scheduler) {
            val user = mockk<FirebaseUser>(relaxed = true)
            every { user.email } returns "test@mail.com"

            val vm = build(user)
            vm.uiState.first { it.bookmarkCount > 0 }

            val state = vm.uiState.value
            assertEquals("test@mail.com", state.username)
            assertEquals("Thắng", state.displayName)
            assertEquals("content://photo/1", state.avatarUri)
            assertEquals(5, state.bookmarkCount)
            assertEquals(3, state.mangaReadCount)
            assertEquals(10, state.chaptersCompleted)
        }

    @Test
    fun uiState_khongCoUser_usernameRong() =
        runTest(mainDispatcherRule.dispatcher.scheduler) {
            val vm = build(user = null)
            vm.uiState.first { it.bookmarkCount > 0 }

            assertEquals("", vm.uiState.value.username)
        }
}
