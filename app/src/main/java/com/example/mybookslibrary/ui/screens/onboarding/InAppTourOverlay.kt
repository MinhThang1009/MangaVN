@file:OptIn(ExperimentalTextApi::class)

package com.example.mybookslibrary.ui.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.Bookmark
import com.composables.icons.lucide.Compass
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MousePointerClick
import com.composables.icons.lucide.Settings
import com.example.mybookslibrary.R
import com.example.mybookslibrary.ui.navigation.LucideSearchIcon
import com.example.mybookslibrary.ui.screens.components.AppButton
import com.example.mybookslibrary.ui.screens.components.AppButtonStyle
import com.example.mybookslibrary.ui.theme.Dimens
import com.example.mybookslibrary.ui.util.appString

private data class TourStep(
    val titleRes: Int,
    val bodyRes: Int,
    val icon: ImageVector,
)

private val tourSteps =
    listOf(
        TourStep(R.string.tour_step1_title, R.string.tour_step1_body, Lucide.Compass),
        TourStep(R.string.tour_step2_title, R.string.tour_step2_body, LucideSearchIcon),
        TourStep(R.string.tour_step3_title, R.string.tour_step3_body, Lucide.MousePointerClick),
        TourStep(R.string.tour_step4_title, R.string.tour_step4_body, Lucide.Bookmark),
        TourStep(R.string.tour_step5_title, R.string.tour_step5_body, Lucide.BookOpen),
        TourStep(R.string.tour_step6_title, R.string.tour_step6_body, Lucide.Settings),
    )

/**
 * Guided tour overlay 6 bước — hiện sau login lần đầu trên Discover.
 * Mỗi bước: icon minh hoạ + title + body + step counter + Back/Next/Skip.
 */
@Suppress("LongMethod")
@Composable
fun InAppTourOverlay(
    visible: Boolean,
    onDismiss: () -> Unit,
) {
    if (!visible) return

    var currentStep by remember { mutableIntStateOf(0) }
    val isFirst = currentStep == 0
    val isLast = currentStep == tourSteps.lastIndex

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .statusBarsPadding()
                .navigationBarsPadding(),
    ) {
        // Skip button top-right
        Box(
            modifier = Modifier.fillMaxWidth().padding(Dimens.SpacingLg),
            contentAlignment = Alignment.TopEnd,
        ) {
            AppButton(
                text = appString(R.string.tour_skip),
                onClick = onDismiss,
                style = AppButtonStyle.Text,
            )
        }

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
            modifier = Modifier.align(Alignment.Center),
            label = "tourStep",
        ) { stepIndex ->
            val s = tourSteps[stepIndex]
            Column(
                modifier = Modifier.padding(horizontal = Dimens.SpacingXxl),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    s.icon,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(Dimens.SpacingXl))
                Text(
                    appString(s.titleRes),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(Dimens.SpacingMd))
                Text(
                    appString(s.bodyRes),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                )
            }
        }

        // Bottom controls
        Column(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = Dimens.SpacingXl, vertical = Dimens.SpacingXl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                appString(R.string.tour_step_counter, currentStep + 1, tourSteps.size),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.6f),
            )
            Spacer(Modifier.height(Dimens.SpacingLg))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                if (!isFirst) {
                    AppButton(
                        text = appString(R.string.tour_back),
                        onClick = { currentStep-- },
                        style = AppButtonStyle.Text,
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }
                AppButton(
                    text =
                        if (isLast) {
                            appString(R.string.tour_done)
                        } else {
                            appString(R.string.tour_next)
                        },
                    onClick = {
                        if (isLast) onDismiss() else currentStep++
                    },
                )
            }
        }
    }
}
