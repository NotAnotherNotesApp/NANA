package com.allubie.nana.ui.screens.finances

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.allubie.nana.util.CurrencyFormatter
import kotlin.math.roundToInt
import androidx.compose.ui.res.stringResource
import com.allubie.nana.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancesOverviewScreen(
    selectedMonth: Int = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH),
    selectedYear: Int = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR),
    onNavigateBack: () -> Unit,
    viewModel: FinancesOverviewViewModel = viewModel(
        factory = FinancesOverviewViewModel.factory(selectedMonth, selectedYear)
    )
) {
    val overview by viewModel.overview.collectAsStateWithLifecycle()
    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()
    
    // Format currency with symbol from settings
    fun formatCurrency(amount: Double): String {
        return CurrencyFormatter.formatWithSymbol(kotlin.math.abs(amount), currencySymbol)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_spending_breakdown)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cash Flow Summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.section_cash_flow),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.label_income),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = formatCurrency(overview.totalIncome),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = stringResource(R.string.status_expenses),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = formatCurrency(overview.totalExpenses),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.status_net_savings),
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = formatCurrency(overview.netSavings),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (overview.netSavings >= 0) 
                                    MaterialTheme.colorScheme.primary 
                                    else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            
            // Spending by Category
            item {
                Text(
                    text = stringResource(R.string.section_spending_by_category),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Donut Chart
            if (overview.categoryBreakdown.isNotEmpty()) {
                item {
                    SpendingGaugeCard(
                        categories = overview.categoryBreakdown,
                        totalAmount = overview.totalExpenses,
                        totalIncome = overview.totalIncome,
                        currencySymbol = currencySymbol
                    )
                }
            }
            
            items(overview.categoryBreakdown, key = { it.name }) { category ->
                CategorySpendingItem(
                    category = category.name,
                    amount = formatCurrency(category.amount),
                    percentage = category.percentage,
                    color = Color(category.color)
                )
            }
            

            
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun CategorySpendingItem(
    category: String,
    amount: String,
    percentage: Float,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = category,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = amount,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${(percentage * 100).roundToInt().coerceIn(0, 100)}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SpendingGaugeCard(
    categories: List<CategorySpending>,
    totalAmount: Double,
    totalIncome: Double,
    currencySymbol: String
) {
    val chartColors = categories.map { Color(it.color) }
    
    // Animate the chart drawing for smooth entry
    val animationProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "chartAnimation"
    )
    
    // Spending ratio as percentage of income (for the sub-text)
    val spentPercentage = if (totalIncome > 0) {
        ((totalAmount / totalIncome) * 100).roundToInt().coerceIn(0, 999)
    } else if (totalAmount > 0) 100 else 0
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Multi-segment donut chart using Canvas
            val trackColor = MaterialTheme.colorScheme.surfaceVariant
            Box(
                modifier = Modifier.size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(180.dp)) {
                    val strokeWidthPx = 10.dp.toPx()
                    val diameter = size.minDimension - strokeWidthPx
                    val topLeft = Offset(
                        (size.width - diameter) / 2f,
                        (size.height - diameter) / 2f
                    )
                    val arcSize = Size(diameter, diameter)
                    
                    // Draw track (background ring)
                    drawArc(
                        color = trackColor,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                    )
                    
                    // Draw category segments
                    if (categories.isNotEmpty() && totalAmount > 0) {
                        var currentAngle = -90f // Start from top
                        categories.forEachIndexed { index, category ->
                            val sweepAngle = (category.percentage * 360f) * animationProgress
                            if (sweepAngle > 0.5f) { // Only draw visible segments
                                drawArc(
                                    color = chartColors[index],
                                    startAngle = currentAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                                )
                            }
                            currentAngle += sweepAngle
                        }
                    }
                }
                
                // Center text
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = CurrencyFormatter.formatWithSymbol(totalAmount, currencySymbol),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = stringResource(R.string.status_total_spent),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (totalIncome > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.template_of_total_income,
                        CurrencyFormatter.formatWithSymbol(totalIncome, currencySymbol)),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            
            if (categories.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Category legend with colored dots
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    maxItemsInEachRow = 3
                ) {
                    categories.forEachIndexed { index, category ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(chartColors[index])
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
