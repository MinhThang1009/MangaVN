package com.example.mybookslibrary

import android.app.Application
import android.util.Log
import com.example.mybookslibrary.data.repository.LibraryRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MyBooksLibraryApp : Application() {

	@Inject
	lateinit var libraryRepository: LibraryRepository

	private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

	override fun onCreate() {
		super.onCreate()

		appScope.launch {
			runCatching {
				libraryRepository.debugClearAndReseed()
				Log.d(TAG, "Startup mock reseed completed")
			}.onFailure { throwable ->
				Log.e(TAG, "Startup mock reseed failed", throwable)
			}
		}
	}

	companion object {
		private const val TAG = "MyBooksLibraryApp"
	}
}

