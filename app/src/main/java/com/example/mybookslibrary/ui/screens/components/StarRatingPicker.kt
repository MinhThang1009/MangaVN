package com.example.mybookslibrary.ui.screens.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Star
import com.example.mybookslibrary.R
import com.example.mybookslibrary.ui.theme.Alphas
import com.example.mybookslibrary.ui.theme.Dimens
import com.example.mybookslibrary.ui.theme.MyBooksLibraryTheme
import com.example.mybookslibrary.ui.util.appString

private const val STAR_COUNT = 5

/**
 * 5 sao tap-to-rate cho form viết review. [rating] 0 = chưa chọn (mọi sao mờ).
 * Màu sao = tertiary, khớp các nơi hiển thị rating khác (RatingBadge, review list).
 */
@Composable
fun StarRatingPicker(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        repeat(STAR_COUNT) { index ->
            val starValue = index + 1
            IconButton(onClick = { onRatingChange(starValue) }) {
                Icon(
                    Lucide.Star,
                    contentDescription = appString(R.string.review_cd_star, starValue),
                    tint =
                        if (starValue <= rating) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = Alphas.EmphasisFaint)
                        },
                    modifier = Modifier.size(Dimens.IconLg),
                )
            }
        }
    }
}

@Suppress("UnusedPrivateMember")
@Preview(name = "StarRatingPicker — 3 sao", showBackground = true)
@Composable
private fun StarRatingPickerPreview() {
    MyBooksLibraryTheme(darkTheme = true) {
        StarRatingPicker(rating = 3, onRatingChange = {})
    }
}

@Suppress("UnusedPrivateMember")
@Preview(name = "StarRatingPicker — chưa chọn", showBackground = true)
@Composable
private fun StarRatingPickerEmptyPreview() {
    MyBooksLibraryTheme(darkTheme = false) {
        StarRatingPicker(rating = 0, onRatingChange = {})
    }
}
