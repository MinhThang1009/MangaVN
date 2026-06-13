package com.example.mybookslibrary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MessageSquare
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.SquarePen
import com.composables.icons.lucide.Star
import com.composables.icons.lucide.Trash2
import com.example.mybookslibrary.R
import com.example.mybookslibrary.data.remote.models.FirestoreReview
import com.example.mybookslibrary.data.repository.ReviewRepository
import com.example.mybookslibrary.ui.screens.components.AppButton
import com.example.mybookslibrary.ui.screens.components.EmptyState
import com.example.mybookslibrary.ui.screens.components.ErrorState
import com.example.mybookslibrary.ui.screens.components.LoadingIndicator
import com.example.mybookslibrary.ui.screens.components.StarRatingPicker
import com.example.mybookslibrary.ui.theme.Alphas
import com.example.mybookslibrary.ui.theme.Dimens
import com.example.mybookslibrary.ui.theme.MyBooksLibraryTheme
import com.example.mybookslibrary.ui.util.appString
import com.example.mybookslibrary.ui.viewmodel.MangaReviewViewModel
import com.example.mybookslibrary.ui.viewmodel.ReviewAggregate
import com.example.mybookslibrary.ui.viewmodel.ReviewEvent
import com.example.mybookslibrary.ui.viewmodel.ReviewUiState
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MangaReviewScreen(
    onBackClick: () -> Unit,
    vm: MangaReviewViewModel = hiltViewModel(),
) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var sheetOpen by rememberSaveable { mutableStateOf(false) }

    val loginRequiredMsg = appString(R.string.review_login_required)
    val submittedMsg = appString(R.string.review_submitted)
    val submitFailedMsg = appString(R.string.review_submit_failed)
    val deletedMsg = appString(R.string.review_deleted)
    val deleteFailedMsg = appString(R.string.review_delete_failed)
    val anonymousName = appString(R.string.review_anonymous)

    LaunchedEffect(vm) {
        vm.events.collect { event ->
            when (event) {
                ReviewEvent.LOGIN_REQUIRED -> snackbarHostState.showSnackbar(loginRequiredMsg)
                ReviewEvent.SUBMITTED -> {
                    sheetOpen = false
                    snackbarHostState.showSnackbar(submittedMsg)
                }
                // Thất bại: GIỮ sheet + nội dung form, chỉ báo lỗi generic
                ReviewEvent.SUBMIT_FAILED -> snackbarHostState.showSnackbar(submitFailedMsg)
                ReviewEvent.DELETED -> snackbarHostState.showSnackbar(deletedMsg)
                ReviewEvent.DELETE_FAILED -> snackbarHostState.showSnackbar(deleteFailedMsg)
            }
        }
    }

    MangaReviewScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBackClick = onBackClick,
        // Chặn mở form khi đang load: chưa biết user có review cũ hay không,
        // mở sớm sẽ thiếu prefill -> nguy cơ ghi đè review cũ bằng form trống.
        onWriteClick = { if (!uiState.isLoading && vm.requestWriteReview()) sheetOpen = true },
        onDeleteMyReview = vm::deleteMyReview,
        onRetry = vm::loadReviews,
        modifier = Modifier,
    )

    if (sheetOpen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { sheetOpen = false },
            sheetState = sheetState,
        ) {
            ReviewFormSheetContent(
                initialReview = uiState.myReview,
                isSubmitting = uiState.isSubmitting,
                onSubmit = { rating, title, body ->
                    vm.submitReview(rating, title, body, fallbackAuthorName = anonymousName)
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MangaReviewScreenContent(
    uiState: ReviewUiState,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onWriteClick: () -> Unit,
    onDeleteMyReview: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Confirm trước khi xóa review — hành động destructive
    var confirmDelete by remember { mutableStateOf(false) }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(appString(R.string.review_delete)) },
            text = { Text(appString(R.string.review_delete_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteMyReview()
                        confirmDelete = false
                    },
                ) {
                    Text(appString(R.string.review_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text(appString(R.string.action_cancel))
                }
            },
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        appString(R.string.detail_customer_reviews),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Lucide.ArrowLeft, contentDescription = appString(R.string.cd_back))
                    }
                },
                actions = {
                    IconButton(onClick = onWriteClick) {
                        Icon(Lucide.SquarePen, contentDescription = appString(R.string.cd_write_review))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        when {
            uiState.isLoading ->
                LoadingIndicator(modifier = Modifier.fillMaxSize().padding(paddingValues))
            uiState.isError ->
                ErrorState(
                    message = appString(R.string.review_load_error),
                    onRetry = onRetry,
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                )
            uiState.myReview == null && uiState.otherReviews.isEmpty() ->
                EmptyState(
                    title = appString(R.string.review_empty_title),
                    subtitle = appString(R.string.review_empty_subtitle),
                    icon = Lucide.MessageSquare,
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                )
            else ->
                ReviewList(
                    uiState = uiState,
                    onEditClick = onWriteClick,
                    onDeleteClick = { confirmDelete = true },
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                )
        }
    }
}

@Composable
private fun ReviewList(
    uiState: ReviewUiState,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = Dimens.ScreenPaddingCompact),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg),
    ) {
        item(key = "aggregate") { AggregateHeader(aggregate = uiState.aggregate) }

        uiState.myReview?.let { mine ->
            item(key = "my_review_header") {
                Text(
                    appString(R.string.review_my_review),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = Dimens.SpacingMd),
                )
            }
            item(key = "my_review") {
                ReviewCard(
                    review = mine,
                    actions = {
                        IconButton(onClick = onEditClick) {
                            Icon(
                                Lucide.Pencil,
                                contentDescription = appString(R.string.review_edit),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(Dimens.IconDefault),
                            )
                        }
                        IconButton(onClick = onDeleteClick) {
                            Icon(
                                Lucide.Trash2,
                                contentDescription = appString(R.string.review_delete),
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(Dimens.IconDefault),
                            )
                        }
                    },
                )
            }
        }

        if (uiState.otherReviews.isNotEmpty()) {
            item(key = "section_header") {
                Text(
                    appString(R.string.review_section_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = Dimens.SpacingMd),
                )
            }
            items(uiState.otherReviews, key = { it.authorUid }) { review ->
                ReviewCard(review = review)
            }
        }

        item(key = "bottom_spacer") { Spacer(Modifier.height(Dimens.SpacingXl)) }
    }
}

@Composable
private fun AggregateHeader(aggregate: ReviewAggregate) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = Dimens.SpacingLg),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                aggregate.average.toString(),
                style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                appString(R.string.review_out_of_5),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(
            modifier = Modifier.weight(1f).padding(start = Dimens.SpacingXl),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingXs),
        ) {
            aggregate.distribution.forEachIndexed { index, fraction ->
                RatingBarRow(stars = ReviewAggregate.STAR_LEVELS - index, progress = fraction)
            }
            Text(
                appString(R.string.review_ratings_count, aggregate.totalCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.End).padding(top = Dimens.SpacingXs),
            )
        }
    }
}

@Composable
private fun ReviewCard(
    review: FirestoreReview,
    modifier: Modifier = Modifier,
    actions: (@Composable () -> Unit)? = null,
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(Dimens.SpacingLg).fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    if (review.title.isNotBlank()) {
                        Text(
                            review.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
                actions?.invoke()
            }
            if (review.body.isNotBlank()) {
                Spacer(Modifier.height(Dimens.SpacingSm))
                Text(
                    review.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(Modifier.height(Dimens.SpacingMd))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row {
                    repeat(ReviewAggregate.STAR_LEVELS) { index ->
                        Icon(
                            Lucide.Star,
                            contentDescription = null,
                            tint =
                                if (index < review.rating) {
                                    MaterialTheme.colorScheme.tertiary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = Alphas.EmphasisFaint)
                                },
                            modifier = Modifier.size(Dimens.IconXs),
                        )
                    }
                }
                Spacer(Modifier.width(Dimens.SpacingSm))
                // Format ngày theo locale máy, không hardcode pattern
                val dateText = remember(review.createdAt) {
                    DateFormat.getDateInstance().format(Date(review.createdAt))
                }
                Text(
                    "$dateText, ${review.authorName.ifBlank { appString(R.string.review_anonymous) }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * Nội dung form viết/sửa review — tách riêng để @Preview chụp được
 * (ModalBottomSheet render window riêng, goldens không thấy).
 */
@Composable
internal fun ReviewFormSheetContent(
    initialReview: FirestoreReview?,
    isSubmitting: Boolean,
    onSubmit: (rating: Int, title: String, body: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // rememberSaveable chỉ init 1 lần — key theo authorUid để form re-prefill nếu
    // initialReview xuất hiện sau khi composition đã bắt đầu (load xong muộn).
    var rating by rememberSaveable(initialReview?.authorUid) {
        mutableIntStateOf(initialReview?.rating ?: 0)
    }
    var title by rememberSaveable(initialReview?.authorUid) {
        mutableStateOf(initialReview?.title.orEmpty())
    }
    var body by rememberSaveable(initialReview?.authorUid) {
        mutableStateOf(initialReview?.body.orEmpty())
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.ScreenPaddingCompact)
            .padding(bottom = Dimens.SpacingXl),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg),
    ) {
        Text(
            appString(if (initialReview == null) R.string.review_write else R.string.review_edit),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        StarRatingPicker(
            rating = rating,
            onRatingChange = { rating = it },
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        OutlinedTextField(
            value = title,
            onValueChange = { title = it.take(ReviewRepository.TITLE_MAX_LENGTH) },
            label = { Text(appString(R.string.review_title_label)) },
            supportingText = {
                Text(appString(R.string.review_char_counter, title.length, ReviewRepository.TITLE_MAX_LENGTH))
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = body,
            onValueChange = { body = it.take(ReviewRepository.BODY_MAX_LENGTH) },
            label = { Text(appString(R.string.review_body_label)) },
            supportingText = {
                Text(appString(R.string.review_char_counter, body.length, ReviewRepository.BODY_MAX_LENGTH))
            },
            minLines = 3,
            modifier = Modifier.fillMaxWidth(),
        )
        if (isSubmitting) {
            LoadingIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            AppButton(
                text = appString(R.string.review_submit),
                onClick = { onSubmit(rating, title, body) },
                // Rating bắt buộc 1..5 — chưa chọn sao thì không gửi được
                enabled = rating in ReviewRepository.RATING_MIN..ReviewRepository.RATING_MAX,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun RatingBarRow(stars: Int, progress: Float) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.width(RatingBarDimens.StarsWidth)) {
            repeat(stars) {
                Icon(
                    Lucide.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(RatingBarDimens.StarSize),
                )
            }
        }
        Spacer(modifier = Modifier.width(Dimens.SpacingSm))
        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .height(Dimens.SpacingXs)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outlineVariant),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth(progress)
                        .height(Dimens.SpacingXs)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiary),
            )
        }
    }
}

// Kích thước riêng của distribution bar (đặc thù màn này, không phải token dùng chung)
private object RatingBarDimens {
    val StarsWidth = 60.dp
    val StarSize = 10.dp
}

@Suppress("UnusedPrivateMember")
@Preview(name = "MangaReview — có data", showBackground = true)
@Composable
private fun MangaReviewScreenPreview() {
    MyBooksLibraryTheme(darkTheme = true) {
        MangaReviewScreenContent(
            uiState = ReviewUiState(
                isLoading = false,
                myReview = FirestoreReview(
                    authorUid = "me",
                    rating = 5,
                    title = "Tuyệt vời",
                    body = "Cốt truyện cuốn, art đẹp.",
                    authorName = "Thắng",
                    createdAt = 1_750_000_000_000,
                ),
                otherReviews = listOf(
                    FirestoreReview(
                        authorUid = "u2",
                        rating = 4,
                        title = "Great read",
                        body = "Engaging story and fantastic art.",
                        authorName = "User456",
                        createdAt = 1_749_000_000_000,
                    ),
                ),
                aggregate = ReviewAggregate(
                    average = 4.5,
                    totalCount = 2,
                    distribution = listOf(0.5f, 0.5f, 0f, 0f, 0f),
                ),
                isLoggedIn = true,
            ),
            snackbarHostState = SnackbarHostState(),
            onBackClick = {},
            onWriteClick = {},
            onDeleteMyReview = {},
            onRetry = {},
        )
    }
}

@Suppress("UnusedPrivateMember")
@Preview(name = "MangaReview — form sheet", showBackground = true)
@Composable
private fun ReviewFormSheetPreview() {
    MyBooksLibraryTheme(darkTheme = true) {
        ReviewFormSheetContent(
            initialReview = null,
            isSubmitting = false,
            onSubmit = { _, _, _ -> },
        )
    }
}

@Suppress("UnusedPrivateMember")
@Preview(name = "MangaReview — rỗng", showBackground = true)
@Composable
private fun MangaReviewScreenEmptyPreview() {
    MyBooksLibraryTheme(darkTheme = false) {
        MangaReviewScreenContent(
            uiState = ReviewUiState(isLoading = false),
            snackbarHostState = SnackbarHostState(),
            onBackClick = {},
            onWriteClick = {},
            onDeleteMyReview = {},
            onRetry = {},
        )
    }
}
