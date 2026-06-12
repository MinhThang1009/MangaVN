package com.example.mybookslibrary.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.example.mybookslibrary.R
import com.example.mybookslibrary.ui.screens.components.SectionHeader
import com.example.mybookslibrary.ui.theme.Alphas
import com.example.mybookslibrary.ui.theme.Dimens
import com.example.mybookslibrary.ui.util.appString
import com.example.mybookslibrary.ui.util.isLandscape
import com.example.mybookslibrary.ui.viewmodel.StatisticsViewModel
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.RowChart
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.DotProperties
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.Line
import ir.ehsannarmani.compose_charts.models.Pie
import java.util.Calendar
import java.util.Locale

@Suppress("LongMethod")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    vm: StatisticsViewModel = hiltViewModel(),
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(appString(R.string.stats_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Lucide.ArrowLeft, contentDescription = appString(R.string.cd_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(
                horizontal = Dimens.ScreenPaddingCompact,
                vertical = Dimens.SpacingLg,
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg),
        ) {
            item {
                ChapterSummaryRow(
                    total = state.totalChapters,
                    completed = state.completedChapters,
                    inProgress = state.inProgressChapters,
                )
            }
            item { SectionHeader(title = appString(R.string.stats_weekly_activity)) }
            item { WeeklyColumnChart(activity = state.weeklyActivity) }
            item { SectionHeader(title = appString(R.string.stats_monthly_trend)) }
            item { MonthlyLineChart(trend = state.monthlyTrend) }
            item { SectionHeader(title = appString(R.string.stats_library_breakdown)) }
            item {
                LibraryPieChart(
                    reading = state.readingCount,
                    completed = state.completedCount,
                    favorite = state.favoriteCount,
                )
            }
            item { SectionHeader(title = appString(R.string.stats_status_comparison)) }
            item {
                StatusRowChart(
                    reading = state.readingCount,
                    completed = state.completedCount,
                    favorite = state.favoriteCount,
                )
            }
        }
    }
}

@Composable
private fun ChapterSummaryRow(total: Int, completed: Int, inProgress: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMd),
    ) {
        SummaryCard(total.toString(), appString(R.string.stats_total_chapters), Modifier.weight(1f))
        SummaryCard(completed.toString(), appString(R.string.stats_completed_chapters), Modifier.weight(1f))
        SummaryCard(inProgress.toString(), appString(R.string.stats_in_progress_chapters), Modifier.weight(1f))
    }
}

@Composable
private fun SummaryCard(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(Dimens.SpacingLg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(Dimens.SpacingXs))
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun WeeklyColumnChart(activity: List<Int>) {
    val primary = MaterialTheme.colorScheme.primary
    val primaryLight = primary.copy(alpha = Alphas.EmphasisFaint)
    val labels = remember { weekDayLabels() }

    val barsData = remember(activity) {
        activity.mapIndexed { index, value ->
            Bars(
                label = labels.getOrElse(index) { "" },
                values = listOf(
                    Bars.Data(
                        value = value.toDouble().coerceAtLeast(0.1),
                        color = Brush.verticalGradient(listOf(primary, primaryLight)),
                    ),
                ),
            )
        }
    }

    val chartHeight = if (isLandscape()) 140.dp else 180.dp
    ChartCard {
        ColumnChart(
            modifier = Modifier.fillMaxWidth().height(chartHeight).padding(Dimens.SpacingSm),
            data = barsData,
            barProperties = BarProperties(
                cornerRadius = Bars.Data.Radius.Rectangle(topRight = 6.dp, topLeft = 6.dp),
                spacing = 4.dp,
                thickness = 24.dp,
            ),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        )
    }
}

@Composable
private fun MonthlyLineChart(trend: List<Int>) {
    val primary = MaterialTheme.colorScheme.primary

    val lineData = remember(trend) {
        listOf(
            Line(
                label = "Chapters",
                values = trend.map { it.toDouble().coerceAtLeast(0.0) },
                color = SolidColor(primary),
                curvedEdges = true,
                dotProperties = DotProperties(
                    enabled = true,
                    color = SolidColor(primary),
                    strokeWidth = 2.dp,
                    radius = 4.dp,
                ),
                drawStyle = DrawStyle.Stroke(width = 3.dp),
            ),
        )
    }

    val lineChartHeight = if (isLandscape()) 140.dp else 180.dp
    ChartCard {
        LineChart(
            modifier = Modifier.fillMaxWidth().height(lineChartHeight).padding(Dimens.SpacingSm),
            data = lineData,
        )
    }
}

@Composable
private fun LibraryPieChart(reading: Int, completed: Int, favorite: Int) {
    val total = reading + completed + favorite
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    val pieData = remember(reading, completed, favorite) {
        listOf(
            Pie(
                label = "Reading",
                data = reading.toDouble().coerceAtLeast(0.01),
                color = primaryColor,
                selectedColor = primaryColor,
            ),
            Pie(
                label = "Completed",
                data = completed.toDouble().coerceAtLeast(0.01),
                color = secondaryColor,
                selectedColor = secondaryColor,
            ),
            Pie(
                label = "Favorite",
                data = favorite.toDouble().coerceAtLeast(0.01),
                color = tertiaryColor,
                selectedColor = tertiaryColor,
            ),
        )
    }

    ChartCard {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (total > 0) {
                PieChart(
                    modifier = Modifier.size(180.dp),
                    data = pieData,
                    scaleAnimEnterSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow,
                    ),
                )
                Spacer(Modifier.height(Dimens.SpacingLg))
            }
            BreakdownLegend(
                items = listOf(
                    LegendItem(primaryColor, appString(R.string.stats_reading), reading),
                    LegendItem(secondaryColor, appString(R.string.stats_completed), completed),
                    LegendItem(tertiaryColor, appString(R.string.stats_favorite), favorite),
                ),
            )
        }
    }
}

@Composable
private fun StatusRowChart(reading: Int, completed: Int, favorite: Int) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val readingLabel = appString(R.string.stats_reading)
    val completedLabel = appString(R.string.stats_completed)
    val favoriteLabel = appString(R.string.stats_favorite)

    val rowData = remember(
        reading, completed, favorite,
        readingLabel, completedLabel, favoriteLabel,
    ) {
        listOf(
            Bars(
                label = readingLabel,
                values = listOf(
                    Bars.Data(
                        value = reading.toDouble().coerceAtLeast(0.1),
                        color = SolidColor(primaryColor),
                    ),
                ),
            ),
            Bars(
                label = completedLabel,
                values = listOf(
                    Bars.Data(
                        value = completed.toDouble().coerceAtLeast(0.1),
                        color = SolidColor(secondaryColor),
                    ),
                ),
            ),
            Bars(
                label = favoriteLabel,
                values = listOf(
                    Bars.Data(
                        value = favorite.toDouble().coerceAtLeast(0.1),
                        color = SolidColor(tertiaryColor),
                    ),
                ),
            ),
        )
    }

    val rowChartHeight = if (isLandscape()) 120.dp else 160.dp
    ChartCard {
        RowChart(
            modifier = Modifier.fillMaxWidth().height(rowChartHeight).padding(Dimens.SpacingSm),
            data = rowData,
            barProperties = BarProperties(
                cornerRadius = Bars.Data.Radius.Rectangle(topRight = 6.dp, bottomRight = 6.dp),
                spacing = 4.dp,
                thickness = 28.dp,
            ),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        )
    }
}

@Composable
private fun ChartCard(content: @Composable () -> Unit) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(Dimens.ScreenPaddingCompact)) {
            content()
        }
    }
}

private data class LegendItem(val color: Color, val label: String, val count: Int)

@Composable
private fun BreakdownLegend(items: List<LegendItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSm)) {
        items.forEach { item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(12.dp).clip(CircleShape).background(item.color))
                Spacer(Modifier.width(Dimens.SpacingSm))
                Text(
                    item.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    item.count.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

private fun weekDayLabels(): List<String> {
    val cal = Calendar.getInstance()
    val today = cal.get(Calendar.DAY_OF_WEEK)
    return (0 until DAYS_IN_WEEK).map { offset ->
        cal.set(
            Calendar.DAY_OF_WEEK,
            ((today - DAYS_IN_WEEK + 1 + offset + Calendar.SATURDAY) % DAYS_IN_WEEK) + 1,
        )
        cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()) ?: ""
    }
}

private const val DAYS_IN_WEEK = 7
