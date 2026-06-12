package com.example.mybookslibrary

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import androidx.work.testing.WorkManagerTestInitHelper
import dagger.hilt.android.testing.HiltTestApplication

/**
 * Custom test runner cho Hilt instrumented test.
 * Dùng HiltTestApplication thay vì MyBooksLibraryApp để @HiltAndroidTest hoạt động.
 * Khai báo trong build.gradle.kts: testInstrumentationRunner = "...HiltTestRunner"
 */
class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader, className: String, context: Context): Application =
        super.newApplication(cl, HiltTestApplication::class.java.name, context)

    override fun callApplicationOnCreate(app: Application) {
        super.callApplicationOnCreate(app)
        // HiltTestApplication không implement Configuration.Provider như MyBooksLibraryApp,
        // mà manifest đã remove WorkManagerInitializer → phải init test mode TRƯỚC khi
        // activity launch (MainActivity gọi ReadingReminderWorker.schedule lúc onCreate).
        WorkManagerTestInitHelper.initializeTestWorkManager(app)
    }
}
