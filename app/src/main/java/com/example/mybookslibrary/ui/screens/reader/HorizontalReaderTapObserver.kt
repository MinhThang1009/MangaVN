package com.example.mybookslibrary.ui.screens.reader

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ViewConfiguration
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Observes edge taps without consuming pointer events, allowing Telephoto to keep handling
 * double-tap and pinch zoom gestures on the same image.
 */
internal fun Modifier.observeConfirmedEdgeTaps(
    viewConfiguration: ViewConfiguration,
    onConfirmedEdgeTap: (Offset) -> Unit,
    onManualDrag: () -> Unit
): Modifier = pointerInput(viewConfiguration, onConfirmedEdgeTap, onManualDrag) {
    coroutineScope {
        var pendingTap: PendingTap? = null
        var confirmationJob: Job? = null

        awaitEachGesture {
            val down = awaitFirstDown(pass = PointerEventPass.Initial, requireUnconsumed = false)
            val downPosition = down.position
            var isTap = true
            var hasReportedDrag = false
            var pointerCount = 1
            var upPosition: Offset? = null

            while (true) {
                val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                pointerCount = maxOf(pointerCount, event.changes.size)
                if (event.changes.any { change ->
                        (change.position - downPosition).getDistance() > viewConfiguration.touchSlop
                    }
                ) {
                    isTap = false
                    if (!hasReportedDrag) {
                        hasReportedDrag = true
                        confirmationJob?.cancel()
                        confirmationJob = null
                        pendingTap = null
                        Timber.d("Reader pager observer detected manual drag")
                        onManualDrag()
                    }
                }

                val trackedPointer = event.changes.firstOrNull { it.id == down.id }
                if (trackedPointer == null || !trackedPointer.pressed) {
                    upPosition = trackedPointer?.position ?: downPosition
                    break
                }
            }

            if (!isTap || pointerCount > 1) {
                Timber.d("Reader pager observer ignored gesture: isTap=%s pointerCount=%d", isTap, pointerCount)
                return@awaitEachGesture
            }
            val tapPosition = upPosition
            val previousTap = pendingTap
            val isDoubleTap = previousTap != null &&
                (previousTap.position - tapPosition).getDistance() <= viewConfiguration.touchSlop

            if (isDoubleTap) {
                Timber.d("Reader pager observer suppressed double tap: x=%.1f y=%.1f", tapPosition.x, tapPosition.y)
                confirmationJob?.cancel()
                confirmationJob = null
                pendingTap = null
            } else {
                confirmationJob?.cancel()
                previousTap?.let { onConfirmedEdgeTap(it.position) }
                pendingTap = PendingTap(tapPosition)
                confirmationJob = launch {
                    delay(viewConfiguration.doubleTapTimeoutMillis)
                    if (pendingTap?.position == tapPosition) {
                        pendingTap = null
                        Timber.d("Reader pager observer confirmed tap: x=%.1f y=%.1f", tapPosition.x, tapPosition.y)
                        onConfirmedEdgeTap(tapPosition)
                    }
                }
            }
        }
    }
}

private data class PendingTap(val position: Offset)
