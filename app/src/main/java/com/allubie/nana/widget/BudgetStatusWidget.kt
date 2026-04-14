package com.allubie.nana.widget

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.Log
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.allubie.nana.MainActivity
import com.allubie.nana.R
import com.allubie.nana.data.NanaDatabase
import com.allubie.nana.data.PreferencesManager
import com.allubie.nana.data.model.TransactionType
import com.allubie.nana.widget.NanaWidgetColorProviders
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.Calendar

class BudgetStatusWidget : GlanceAppWidget() {
    private companion object {
        const val TAG = "BudgetStatusWidget"
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = NanaDatabase.getDatabase(context)
        val prefs = PreferencesManager(context)

        val snapshot = withContext(Dispatchers.IO) {
            val currencySymbol = prefs.currencySymbol.first()

            val calendar = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfMonth = calendar.timeInMillis
            calendar.add(Calendar.MONTH, 1)
            val endOfMonth = calendar.timeInMillis

            val monthSpending = try {
                db.transactionDao().getTotalByTypeInRange(
                    TransactionType.EXPENSE, startOfMonth, endOfMonth
                ) ?: 0.0
            } catch (e: Exception) {
                Log.e(TAG, "Failed to read monthly spending for widget", e)
                0.0
            }

            val totalBudgetLimit = prefs.totalBudget.first()
            val allBudgets = try {
                db.budgetDao().getAllBudgets().first()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to read budgets for widget", e)
                emptyList()
            }
            val totalAllocated = allBudgets.sumOf { it.amount }
            val budgetAmount = if (totalBudgetLimit > 0) totalBudgetLimit else totalAllocated
            val remainingAmountRaw = budgetAmount - monthSpending
            val remainingAmount = remainingAmountRaw.coerceAtLeast(0.0)
            val remainingPercentage = if (budgetAmount > 0) {
                (remainingAmount / budgetAmount * 100).coerceIn(0.0, 100.0)
            } else {
                0.0
            }

            BudgetSnapshot(
                currencySymbol = currencySymbol,
                monthSpending = monthSpending,
                budgetAmount = budgetAmount,
                remainingAmount = remainingAmount,
                hasBudget = budgetAmount > 0,
                remainingPercentage = remainingPercentage
            )
        }

        val isDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
        val progressColor = when {
            snapshot.remainingPercentage <= 10 -> 0xFFBA1A1A.toInt()
            else -> if (isDark) 0xFFD0BCFF.toInt() else 0xFF6750A4.toInt()
        }
        val trackColor = if (isDark) 0xFF4A4A4D.toInt() else 0xFFE1DFE4.toInt()
        val progressBitmap = withContext(Dispatchers.Default) {
            createCircularProgressBitmap(context, snapshot.remainingPercentage, progressColor, trackColor, 56)
        }

        val spendingFormatted = formatAmount(snapshot.monthSpending, snapshot.currencySymbol)
        val budgetFormatted = formatAmount(snapshot.budgetAmount, snapshot.currencySymbol)
        val remainingFormatted = formatAmount(snapshot.remainingAmount, snapshot.currencySymbol)
        val percentageInt = snapshot.remainingPercentage.toInt()

        provideContent {
            GlanceTheme(colors = NanaWidgetColorProviders) {
                Row(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.surface)
                        .padding(8.dp)
                        .cornerRadius(R.dimen.widget_outer_radius)
                        .clickable(
                            actionStartActivity(
                                Intent(context, MainActivity::class.java).apply {
                                    putExtra("navigate_to", "finances")
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                }
                            )
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        provider = ImageProvider(progressBitmap),
                        contentDescription = "$percentageInt% of budget remaining",
                        modifier = GlanceModifier.size(56.dp)
                    )

                    Spacer(modifier = GlanceModifier.width(8.dp))

                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text(
                            text = "Budget",
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        )

                        if (!snapshot.hasBudget) {
                            Text(
                                text = "No budget set",
                                style = TextStyle(
                                    color = GlanceTheme.colors.onSurface,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Spent $spendingFormatted",
                                style = TextStyle(
                                    color = GlanceTheme.colors.onSurfaceVariant,
                                    fontSize = 10.sp
                                )
                            )
                        } else {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = remainingFormatted,
                                    style = TextStyle(
                                        color = GlanceTheme.colors.primary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                )
                                Text(
                                    text = "/$budgetFormatted",
                                    style = TextStyle(
                                        color = GlanceTheme.colors.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                )
                            }

                            Text(
                                text = "${percentageInt}% left",
                                style = TextStyle(
                                    color = GlanceTheme.colors.onSurfaceVariant,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private data class BudgetSnapshot(
        val currencySymbol: String,
        val monthSpending: Double,
        val budgetAmount: Double,
        val remainingAmount: Double,
        val hasBudget: Boolean,
        val remainingPercentage: Double
    )

    private fun createCircularProgressBitmap(
        context: Context,
        percentage: Double,
        progressColor: Int,
        trackColor: Int,
        sizeDp: Int
    ): Bitmap {
        val density = context.resources.displayMetrics.density
        val sizePx = (sizeDp * density).toInt()
        val strokeWidth = sizePx * 0.10f

        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
        }

        val inset = strokeWidth / 2 + 2
        val rect = RectF(inset, inset, sizePx - inset, sizePx - inset)

        // Track arc: 270° with gap at bottom
        paint.color = trackColor
        canvas.drawArc(rect, 135f, 270f, false, paint)

        // Progress arc
        if (percentage > 0) {
            paint.color = progressColor
            val sweepAngle = (percentage / 100.0 * 270.0).toFloat()
            canvas.drawArc(rect, 135f, sweepAngle, false, paint)
        }

        return bitmap
    }

    private fun formatAmount(amount: Double, symbol: String): String {
        return if (amount == amount.toLong().toDouble()) {
            "$symbol${amount.toLong()}"
        } else {
            "$symbol${"%.2f".format(amount)}"
        }
    }
}

class BudgetStatusWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = BudgetStatusWidget()
}
