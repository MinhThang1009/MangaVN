@file:OptIn(ExperimentalTextApi::class)

package com.example.mybookslibrary.ui.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.mybookslibrary.R
import com.example.mybookslibrary.ui.screens.components.AppButton
import com.example.mybookslibrary.ui.screens.components.AppButtonStyle
import com.example.mybookslibrary.ui.theme.Dimens
import com.example.mybookslibrary.ui.util.appString

private const val OVERLAY_ALPHA = 0.82f
private val SPOTLIGHT_PADDING = 8.dp
private val SPOTLIGHT_CORNER = 16.dp
private val TOOLTIP_MAX_WIDTH = 300.dp

/**
 * Overlay khoét sáng vào element thật + tooltip — dạng coach marks động.
 * Canvas + BlendMode.Clear khoét vùng highlight quanh target rect.
 */
@Suppress("LongMethod")
@Composable
fun CoachMarkOverlay(
    visible: Boolean,
    state: CoachMarkState,
    steps: List<CoachMarkStep>,
    onDismiss: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible && steps.isNotEmpty(),
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        val currentStep = state.currentStep.coerceIn(0, steps.lastIndex)
        val step = steps[currentStep]
        val targetRect = state.getTargetRect(step.key)
        val density = LocalDensity.current
        val configuration = LocalConfiguration.current
        val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
        val spotlightPaddingPx = with(density) { SPOTLIGHT_PADDING.toPx() }
        val cornerPx = with(density) { SPOTLIGHT_CORNER.toPx() }

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) { detectTapGestures { } },
        ) {
            Canvas(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen),
            ) {
                drawRect(Color.Black.copy(alpha = OVERLAY_ALPHA))
                if (targetRect != null) {
                    val spotlightRect =
                        Rect(
                            left = targetRect.left - spotlightPaddingPx,
                            top = targetRect.top - spotlightPaddingPx,
                            right = targetRect.right + spotlightPaddingPx,
                            bottom = targetRect.bottom + spotlightPaddingPx,
                        )
                    drawRoundRect(
                        color = Color.Black,
                        topLeft = Offset(spotlightRect.left, spotlightRect.top),
                        size = Size(spotlightRect.width, spotlightRect.height),
                        cornerRadius = CornerRadius(cornerPx, cornerPx),
                        blendMode = BlendMode.Clear,
                    )
                }
            }

            val tooltipBelow = targetRect == null || targetRect.center.y < screenHeightPx / 2
            val tooltipY =
                if (targetRect == null) {
                    with(density) { 200.dp.roundToPx() }
                } else if (tooltipBelow) {
                    (targetRect.bottom + spotlightPaddingPx + with(density) { Dimens.SpacingLg.toPx() }).toInt()
                } else {
                    (targetRect.top - spotlightPaddingPx - with(density) { 160.dp.toPx() }).toInt()
                }

            Column(
                modifier =
                    Modifier
                        .offset { IntOffset(0, tooltipY) }
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.SpacingXl),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    modifier =
                        Modifier
                            .widthIn(max = TOOLTIP_MAX_WIDTH)
                            .padding(Dimens.SpacingLg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        appString(step.titleRes),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(Dimens.SpacingSm))
                    Text(
                        appString(step.bodyRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center,
                    )
                }
                Spacer(Modifier.height(Dimens.SpacingLg))
                Text(
                    appString(R.string.tour_step_counter, currentStep + 1, steps.size),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f),
                )
                Spacer(Modifier.height(Dimens.SpacingMd))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    if (currentStep > 0) {
                        AppButton(
                            text = appString(R.string.tour_back),
                            onClick = { state.currentStep-- },
                            style = AppButtonStyle.Text,
                        )
                    }
                    AppButton(
                        text =
                            if (currentStep == steps.lastIndex) {
                                appString(R.string.tour_done)
                            } else {
                                appString(R.string.tour_next)
                            },
                        onClick = {
                            if (currentStep == steps.lastIndex) {
                                onDismiss()
                            } else {
                                state.currentStep++
                            }
                        },
                    )
                    AppButton(
                        text = appString(R.string.tour_skip),
                        onClick = onDismiss,
                        style = AppButtonStyle.Text,
                    )
                }
            }
        }
    }
}
