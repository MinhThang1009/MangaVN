package com.example.mybookslibrary.data.repository

import com.example.mybookslibrary.data.local.UserEntity
import com.example.mybookslibrary.data.local.UserPreferencesDataStore
import com.example.mybookslibrary.data.local.dao.UserDao
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.MessageDigest

/**
 * Phủ luồng đăng nhập của [AuthRepository]: xác thực đúng/sai, user không tồn tại,
 * migrate hash SHA-256 cũ sang PBKDF2 khi đăng nhập, và xử lý hash hỏng an toàn.
 */
class AuthRepositoryLoginTest {
    private val userDao = mockk<UserDao>()
    private val preferences = mockk<UserPreferencesDataStore>(relaxed = true)
    private val repository = AuthRepository(userDao, preferences, Dispatchers.IO)

    // Hash SHA-256 không salt giống legacySha256() trong AuthRepository (định dạng hash cũ).
    private fun legacySha256(password: String): String =
        MessageDigest
            .getInstance("SHA-256")
            .digest(password.toByteArray())
            .joinToString("") { "%02x".format(it) }

    @Test
    fun login_correctPassword_succeedsAndSetsLoggedInUser() =
        runTest {
            // register tạo hash PBKDF2 thật; bắt lại để dùng cho login.
            val registered = slot<UserEntity>()
            coEvery { userDao.getByUsername("alice") } returns null
            coEvery { userDao.upsert(capture(registered)) } returns Unit
            repository.register("alice", "hunter2")

            coEvery { userDao.getByUsername("alice") } returns registered.captured.copy(id = 1)

            val result = repository.login("alice", "hunter2")

            assertTrue(result.isSuccess)
            coVerify { preferences.setLoggedInUserId("1") }
        }

    @Test
    fun login_wrongPassword_returnsFailure() =
        runTest {
            val registered = slot<UserEntity>()
            coEvery { userDao.getByUsername("alice") } returns null
            coEvery { userDao.upsert(capture(registered)) } returns Unit
            repository.register("alice", "hunter2")

            coEvery { userDao.getByUsername("alice") } returns registered.captured.copy(id = 1)

            val result = repository.login("alice", "wrong-password")

            assertTrue(result.isFailure)
            coVerify(exactly = 0) { preferences.setLoggedInUserId(any()) }
        }

    @Test
    fun login_unknownUser_returnsFailure() =
        runTest {
            coEvery { userDao.getByUsername("ghost") } returns null

            val result = repository.login("ghost", "whatever")

            assertTrue(result.isFailure)
        }

    @Test
    fun login_legacySha256Hash_rehashesToPbkdf2() =
        runTest {
            val legacyUser = UserEntity(id = 7, username = "bob", password = legacySha256("pw"))
            coEvery { userDao.getByUsername("bob") } returns legacyUser
            val rehashed = slot<UserEntity>()
            coEvery { userDao.upsert(capture(rehashed)) } returns Unit

            val result = repository.login("bob", "pw")

            assertTrue(result.isSuccess)
            // Đăng nhập đúng với hash cũ phải migrate sang định dạng PBKDF2.
            assertTrue(rehashed.captured.password.startsWith("pbkdf2:"))
        }

    @Test
    fun login_corruptedPbkdf2Hash_returnsFailureNotCrash() =
        runTest {
            val corruptUser = UserEntity(id = 9, username = "eve", password = "pbkdf2:not-valid")
            coEvery { userDao.getByUsername("eve") } returns corruptUser

            val result = repository.login("eve", "whatever")

            // Hash hỏng phải coi như sai mật khẩu, không để exception thô thoát ra.
            assertTrue(result.isFailure)
            assertFalse(result.isSuccess)
        }
}
