package com.example.mybookslibrary.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Spacing tokens — grid 4dp (refactor-ui-ux.md §3.3).
 * Mọi padding/gap trong UI lấy từ đây, không hardcode số lẻ.
 */
object Dimens {
    val SpacingXs = 4.dp
    val SpacingSm = 8.dp
    val SpacingMd = 12.dp
    val SpacingLg = 16.dp
    val SpacingXl = 24.dp
    val SpacingXxl = 32.dp

    // Screen padding theo width size class: compact = 16, medium trở lên = 24
    val ScreenPaddingCompact = 16.dp
    val ScreenPaddingMedium = 24.dp
}

/**
 * Elevation tokens — CHỈ dùng ở light mode (shadow mềm);
 * dark mode tách lớp bằng 4 bậc surface container, KHÔNG shadow (§3.3).
 */
object Elevations {
    val Resting = 2.dp
    val Raised = 6.dp
    val Dialog = 8.dp
}
