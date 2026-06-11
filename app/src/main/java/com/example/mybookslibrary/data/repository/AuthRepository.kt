package com.example.mybookslibrary.data.repository

import android.content.Context
import com.example.mybookslibrary.data.local.UserPreferencesDataStore
import com.example.mybookslibrary.di.IoDispatcher
import com.example.mybookslibrary.domain.model.AuthStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository
    @Inject
    constructor(
        private val firebaseAuth: FirebaseAuth,
        private val preferencesDataStore: UserPreferencesDataStore,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
        private val googleSignInClient: GoogleSignInClient,
    ) {
        fun observeAuthStatus(): Flow<AuthStatus> = preferencesDataStore.observeAuthStatus()

        fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

        suspend fun signOut() {
            firebaseAuth.signOut()
            preferencesDataStore.updateAuthStatus(AuthStatus.LOGGED_OUT)
            preferencesDataStore.updateFirebaseUid(null)
        }

        suspend fun continueAsGuest() {
            preferencesDataStore.updateAuthStatus(AuthStatus.GUEST)
            preferencesDataStore.updateFirebaseUid(null)
        }

        suspend fun registerWithEmail(
            email: String,
            password: String,
        ): Result<FirebaseUser> =
            withContext(ioDispatcher) {
                try {
                    val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                    val user = result.user ?: throw Exception("User is null after registration")
                    preferencesDataStore.updateAuthStatus(AuthStatus.LOGGED_IN)
                    preferencesDataStore.updateFirebaseUid(user.uid)
                    Result.success(user)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        suspend fun signInWithEmail(
            email: String,
            password: String,
        ): Result<FirebaseUser> =
            withContext(ioDispatcher) {
                try {
                    val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                    val user = result.user ?: throw Exception("User is null after sign in")
                    preferencesDataStore.updateAuthStatus(AuthStatus.LOGGED_IN)
                    preferencesDataStore.updateFirebaseUid(user.uid)
                    Result.success(user)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        suspend fun signInWithGoogle(context: Context): Result<FirebaseUser> =
            withContext(ioDispatcher) {
                try {
                    val account =
                        googleSignInClient
                            .getGoogleAccount(context)
                            .getOrThrow()

                    // account.googleId here is actually the ID Token from Credential Manager
                    val credential = GoogleAuthProvider.getCredential(account.googleId, null)
                    val result = firebaseAuth.signInWithCredential(credential).await()
                    val user = result.user ?: throw Exception("User is null after Google sign in")
                    
                    preferencesDataStore.updateAuthStatus(AuthStatus.LOGGED_IN)
                    preferencesDataStore.updateFirebaseUid(user.uid)
                    Result.success(user)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
            
        suspend fun deleteAccount(): Result<Unit> =
            withContext(ioDispatcher) {
                try {
                    // Xóa Firestore data sẽ được thực hiện ở UseCase/ViewModel (Task 4)
                    // Xóa Firebase Auth user
                    firebaseAuth.currentUser?.delete()?.await()
                    
                    preferencesDataStore.updateAuthStatus(AuthStatus.LOGGED_OUT)
                    preferencesDataStore.updateFirebaseUid(null)
                    Result.success(Unit)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
    }

