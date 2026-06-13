@file:OptIn(ExperimentalTextApi::class)

package com.example.mybookslibrary.ui.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateRectAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.mybookslibrary.R
import com.example.mybookslibrary.ui.screens.components.AppButton
import com.example.mybookslibrary.ui.screens.components.AppButtonStyle
import com.example.mybookslibrary.ui.theme.Alphas
import com.example.mybookslibrary.ui.theme.Dimens
import com.example.mybookslibrary.ui.theme.LocalReducedMotion
import com.example.mybookslibrary.ui.util.appString

private const val OVERLAY_ALPHA = 0.78f
private val SPOTLIGHT_PADDING = 10.dp
private const val SPOTLIGHT_CORNER = 20f
private val TOOLTIP_MAX_WIDTH = 300.dp

// Chiều cao ước lượng của tooltip — dùng để đặt tooltip phía trên target và clamp
// trong vùng an toàn (tránh tràn khỏi mép màn khi màn thấp, vd landscape).
private val ESTIMATED_TOOLTIP_HEIGHT = 180.dp

/**
 * Coach mark overlay khoét sáng element thật.
 * Dùng Path + PathFillType.EvenOdd (addRect toàn màn + addRoundRect lỗ sáng)
 * — cách đáng tin nhất, không cần BlendMode.Clear/CompositingStrategy.
 *
 * Ref: https://www.droiddevtips.com/jetpack-compose-spotlight-effect-a-step-by-step-guide.html
 */
@Suppress("LongMethod")
@Composable
fun CoachMarkOverlay(
    visible: Boolean,
    state: CoachMarkState,
    steps: List<CoachMarkStep>,
    onDismiss: () -> Unit,
) {
    if (!visible || steps.isEmpty()) return

    val currentStep = state.currentStep.coerceIn(0, steps.lastIndex)
    val step = steps[currentStep]
    val targetRect = state.getTargetRect(step.key)
    if (targetRect == null) return
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val reducedMotion = LocalReducedMotion.current
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val spotlightPadPx = with(density) { SPOTLIGHT_PADDING.toPx() }

    // Lỗ sáng trượt mượt khi đổi step (đồng bộ với slide của tooltip) thay vì nhảy tức thì.
    // Tôn trọng reducedMotion: tắt animation theo accessibility setting.
    val animatedRect by animateRectAsState(targetRect, label = "spotlightRect")
    val spotRect = if (reducedMotion) targetRect else animatedRect

    // Mô tả cho screen reader (TalkBack) đọc nội dung bước tour đang hiển thị.
    val tourDescription = "${appString(step.titleRes)}. ${appString(step.bodyRes)}"

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) { detectTapGestures { } }
                .semantics { contentDescription = tourDescription },
    ) {
        // Overlay tối + khoét lỗ sáng bằng Path EvenOdd. targetRect đã guard non-null ở trên.
        Canvas(modifier = Modifier.fillMaxSize()) {
            val overlayPath =
                Path().apply {
                    // Rect toàn màn hình
                    addRect(Rect(Offset.Zero, Size(size.width, size.height)))
                    // Khoét lỗ sáng tại target (dùng spotRect đã animate)
                    addRoundRect(
                        RoundRect(
                            left = spotRect.left - spotlightPadPx,
                            top = spotRect.top - spotlightPadPx,
                            right = spotRect.right + spotlightPadPx,
                            bottom = spotRect.bottom + spotlightPadPx,
                            cornerRadius = CornerRadius(SPOTLIGHT_CORNER, SPOTLIGHT_CORNER),
                        ),
                    )
                    fillType = PathFillType.EvenOdd
                }
            drawPath(overlayPath, Color.Black.copy(alpha = OVERLAY_ALPHA))
        }

        // Tooltip co bề ngang theo màn để không tràn trên màn hẹp / landscape.
        val edgePadPx = with(density) { Dimens.SpacingXl.toPx() }
        val tooltipWidthDp =
            TOOLTIP_MAX_WIDTH.coerceAtMost(
                (configuration.screenWidthDp.dp - Dimens.SpacingXl * 2).coerceAtLeast(0.dp),
            )
        val tooltipWidthPx = with(density) { tooltipWidthDp.toPx() }
        val estTooltipHeightPx = with(density) { ESTIMATED_TOOLTIP_HEIGHT.toPx() }

        // Đặt tooltip trên/dưới target tuỳ nửa màn; clamp Y trong vùng an toàn (fix tràn landscape).
        val tooltipBelow = spotRect.center.y < screenHeightPx / 2
        val rawTooltipY =
            if (tooltipBelow) {
                spotRect.bottom + spotlightPadPx + with(density) { Dimens.SpacingLg.toPx() }
            } else {
                spotRect.top - spotlightPadPx - estTooltipHeightPx
            }
        val maxTooltipY = (screenHeightPx - estTooltipHeightPx).coerceAtLeast(edgePadPx)
        val tooltipY = rawTooltipY.coerceIn(edgePadPx, maxTooltipY)

        // Căn tooltip theo trục X của target (fix lệch xa target trên rail tablet), clamp trong mép màn.
        val maxTooltipX = (screenWidthPx - tooltipWidthPx - edgePadPx).coerceAtLeast(edgePadPx)
        val tooltipX = (spotRect.center.x - tooltipWidthPx / 2).coerceIn(edgePadPx, maxTooltipX)

        Column(
            modifier =
                Modifier
                    .offset { IntOffset(tooltipX.toInt(), tooltipY.toInt()) }
                    .width(tooltipWidthDp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { it / 3 } + fadeIn()) togetherWith
                            (slideOutHorizontally { -it / 3 } + fadeOut())
                    } else {
                        (slideInHorizontally { -it / 3 } + fadeIn()) togetherWith
                            (slideOutHorizontally { it / 3 } + fadeOut())
                    }
                },
                label = "tourStep",
            ) { stepIndex ->
                val s = steps[stepIndex]
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        appString(s.titleRes),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(Dimens.SpacingSm))
                    Text(
                        appString(s.bodyRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = Alphas.EmphasisVeryHigh),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(Modifier.height(Dimens.SpacingLg))
            Text(
                appString(R.string.tour_step_counter, currentStep + 1, steps.size),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = Alphas.EmphasisMuted),
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
                        if (currentStep == steps.lastIndex) onDismiss() else state.currentStep++
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
