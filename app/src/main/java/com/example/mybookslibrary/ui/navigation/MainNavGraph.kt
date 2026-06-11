@file:Suppress("ktlint:standard:property-naming")

package com.example.mybookslibrary.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.Compass
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Settings
import com.example.mybookslibrary.R
import com.example.mybookslibrary.ui.screens.onboarding.CoachMarkOverlay
import com.example.mybookslibrary.ui.screens.onboarding.CoachMarkStep
import com.example.mybookslibrary.ui.screens.onboarding.rememberCoachMarkState
import kotlin.reflect.KClass

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
val LocalNavAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }
val LocalBottomNavPadding = compositionLocalOf<Dp> { 0.dp }
val LocalWindowWidthSizeClass = staticCompositionLocalOf { WindowWidthSizeClass.Compact }

sealed class BottomNavDestination(
    val destination: Any,
    val routeClass: KClass<out Any>,
    @param:StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    data object DiscoverTab :
        BottomNavDestination(Discover, Discover::class, R.string.nav_discover, Lucide.Compass)

    data object SearchTab :
        BottomNavDestination(Search, Search::class, R.string.nav_search, LucideSearchIcon)

    data object LibraryTab :
        BottomNavDestination(Library, Library::class, R.string.nav_library, Lucide.BookOpen)

    data object SettingTab :
        BottomNavDestination(Setting, Setting::class, R.string.nav_setting, Lucide.Settings)
}

internal val bottomDestinations =
    listOf(
        BottomNavDestination.DiscoverTab,
        BottomNavDestination.SearchTab,
        BottomNavDestination.LibraryTab,
        BottomNavDestination.SettingTab,
    )

@Suppress("CyclomaticComplexMethod", "ComplexCondition", "LongMethod")
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MainNavHost(
    loggedInUserId: String?,
    incomingMangaId: String? = null,
    onboardingWelcomeDone: Boolean = true,
    onWelcomeDone: () -> Unit = {},
    inAppTourDone: Boolean = true,
    onTourDone: () -> Unit = {},
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val coachMarkState = rememberCoachMarkState()
    val showTour = loggedInUserId != null && !inAppTourDone

    LaunchedEffect(loggedInUserId) {
        if (loggedInUserId == null) {
            if (currentDestination != null &&
                !currentDestination.hasRoute<Login>() &&
                !currentDestination.hasRoute<Register>() &&
                !currentDestination.hasRoute<Onboarding>()
            ) {
                navController.navigate(Login) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    // Navigate to manga detail when app is opened via share sheet (ACTION_SEND from browser).
    LaunchedEffect(incomingMangaId) {
        if (!incomingMangaId.isNullOrBlank() && loggedInUserId != null) {
            navController.navigate(MangaDetail(incomingMangaId)) {
                launchSingleTop = true
            }
        }
    }

    val showNav =
        currentDestination?.hierarchy?.none { dest ->
            dest.hasRoute<Login>() ||
                dest.hasRoute<Register>() ||
                dest.hasRoute<Onboarding>() ||
                dest.hasRoute<Reader>() ||
                dest.hasRoute<MangaDetail>() ||
                dest.hasRoute<MangaReview>()
        } ?: true

    val widthSizeClass = LocalWindowWidthSizeClass.current
    val useRail = showNav && widthSizeClass != WindowWidthSizeClass.Compact

    val navBarCallback: (BottomNavDestination) -> Unit = { destination ->
        navController.navigate(destination.destination) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    @Composable
    fun NavContent(modifier: Modifier = Modifier) {
        SharedTransitionLayout {
            CompositionLocalProvider(
                LocalSharedTransitionScope provides this@SharedTransitionLayout,
                LocalBottomNavPadding provides if (useRail) 0.dp else 80.dp,
            ) {
                NavHost(
                    navController = navController,
                    startDestination =
                    when {
                        loggedInUserId == null && !onboardingWelcomeDone -> Onboarding
                        loggedInUserId == null -> Login
                        else -> Discover
                    },
                    modifier = modifier,
                    enterTransition = { fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)) },
                    exitTransition = { fadeOut(animationSpec = tween(300, easing = FastOutSlowInEasing)) },
                    popEnterTransition = { fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)) },
                    popExitTransition = { fadeOut(animationSpec = tween(300, easing = FastOutSlowInEasing)) },
                ) {
                    onboardingGraph(navController, onWelcomeDone)
                    authGraph(navController)
                    mainTabsGraph(navController)
                    mangaDetailGraph(navController)
                    reviewGraph(navController)
                    readerGraph(navController)
                }
            }
        }
    }

    if (useRail) {
        Row(modifier = Modifier.fillMaxSize().testTag(MAIN_NAV_CONTENT_TAG)) {
            FloatingPillNavBar(
                currentDestination = currentDestination,
                onNavigate = navBarCallback,
                modifier =
                    Modifier
                        .testTag(FLOATING_PILL_NAV_TAG)
                        .onGloballyPositioned { coachMarkState.registerTarget("nav_bar", it) },
            )
            NavContent(modifier = Modifier.weight(1f))
        }
    } else {
        Scaffold(
            bottomBar = {
                if (showNav) {
                    FloatingPillNavBar(
                        currentDestination = currentDestination,
                        onNavigate = navBarCallback,
                        modifier =
                    Modifier
                        .testTag(FLOATING_PILL_NAV_TAG)
                        .onGloballyPositioned { coachMarkState.registerTarget("nav_bar", it) },
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
        ) { innerPadding ->
            Box(
                modifier =
                    Modifier
                        .consumeWindowInsets(innerPadding)
                        .testTag(MAIN_NAV_CONTENT_TAG)
                        .onGloballyPositioned { coachMarkState.registerTarget("content_area", it) },
            ) {
                NavContent()
            }
        }
    }

    CoachMarkOverlay(
        visible = showTour,
        state = coachMarkState,
        steps =
            listOf(
                CoachMarkStep("content_area", R.string.tour_step1_title, R.string.tour_step1_body),
                CoachMarkStep("nav_bar", R.string.tour_step2_title, R.string.tour_step2_body),
                CoachMarkStep("content_area", R.string.tour_step3_title, R.string.tour_step3_body),
                CoachMarkStep("nav_bar", R.string.tour_step4_title, R.string.tour_step4_body),
                CoachMarkStep("nav_bar", R.string.tour_step5_title, R.string.tour_step5_body),
                CoachMarkStep("nav_bar", R.string.tour_step6_title, R.string.tour_step6_body),
            ),
        onDismiss = onTourDone,
    )
}

internal const val MAIN_NAV_CONTENT_TAG = "main-nav-content"
internal const val FLOATING_PILL_NAV_TAG = "floating-pill-nav"
