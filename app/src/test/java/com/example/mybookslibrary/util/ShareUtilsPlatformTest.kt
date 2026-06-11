package com.example.mybookslibrary.util

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * Test phần Android-platform của ShareUtils (Intent/Settings/DomainVerification) —
 * phần thuần logic (build URL/text, extract id) đã có ShareUtilsTest cover.
 *
 * Dùng Activity context như app thật (Compose gọi qua LocalContext.current):
 * startActivity từ non-Activity context sẽ throw vì thiếu FLAG_ACTIVITY_NEW_TASK.
 */
@RunWith(RobolectricTestRunner::class)
class ShareUtilsPlatformTest {
    private fun activity(): Activity = Robolectric.buildActivity(Activity::class.java).setup().get()

    @Test
    fun shareManga_startsChooserWrappingSendIntentWithShareText() {
        val activity = activity()

        shareManga(activity, mangaId = "abc-123", mangaTitle = "One Piece")

        val chooser = shadowOf(activity).nextStartedActivity
        assertEquals(Intent.ACTION_CHOOSER, chooser.action)

        val inner = chooser.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
        assertEquals(Intent.ACTION_SEND, inner?.action)
        assertEquals("text/plain", inner?.type)
        val text = inner?.getStringExtra(Intent.EXTRA_TEXT).orEmpty()
        assertTrue(text.contains("One Piece"))
        assertTrue(text.contains("https://mangadex.org/title/abc-123"))
    }

    @Test
    fun openAppLinkSettings_onApi31Plus_opensOpenByDefaultSettings() {
        val activity = activity()

        openAppLinkSettings(activity)

        val intent = shadowOf(activity).nextStartedActivity
        assertEquals(Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS, intent.action)
        assertEquals("package:${activity.packageName}", intent.data.toString())
    }

    @Config(sdk = [30])
    @Test
    fun openAppLinkSettings_belowApi31_opensAppDetailsSettings() {
        val activity = activity()

        openAppLinkSettings(activity)

        val intent = shadowOf(activity).nextStartedActivity
        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, intent.action)
        assertEquals("package:${activity.packageName}", intent.data.toString())
    }

    @Config(sdk = [30])
    @Test
    fun isOpenLinksGranted_belowApi31_returnsFalse() {
        assertFalse(isOpenLinksGranted(ApplicationProvider.getApplicationContext<Application>()))
    }

    @Test
    fun isOpenLinksGranted_onApi31Plus_withoutSelectedDomain_returnsFalse() {
        // Robolectric mặc định không có domain nào ở trạng thái SELECTED
        assertFalse(isOpenLinksGranted(ApplicationProvider.getApplicationContext<Application>()))
    }
}
