package com.allubie.nana.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.ColorFilter
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.GlanceTheme
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.allubie.nana.MainActivity
import com.allubie.nana.R

class QuickActionsWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.background)
                        .padding(8.dp)
                        .cornerRadius(R.dimen.widget_outer_radius),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // New Note Button
                    QuickActionRow(
                        context = context,
                        iconRes = R.drawable.ic_widget_add,
                        isPrimary = true,
                        title = "New Note",
                        navigateTo = "notes/editor/-1"
                    )

                    Spacer(modifier = GlanceModifier.height(4.dp))

                    // Add Expense Button
                    QuickActionRow(
                        context = context,
                        iconRes = R.drawable.ic_widget_expense,
                        isPrimary = false,
                        title = "Add Expense",
                        navigateTo = "finances/editor/-1"
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionRow(
    context: Context,
    iconRes: Int,
    isPrimary: Boolean,
    title: String,
    navigateTo: String
) {
    val iconBackground = if (isPrimary) GlanceTheme.colors.primaryContainer else GlanceTheme.colors.surfaceVariant
    val iconTint = if (isPrimary) GlanceTheme.colors.primary else GlanceTheme.colors.onSurfaceVariant

    Box(
        modifier = GlanceModifier
            .wrapContentWidth()
            .background(GlanceTheme.colors.surfaceVariant)
            .cornerRadius(R.dimen.widget_inner_radius)
            .clickable(
                actionStartActivity(
                    Intent(context, MainActivity::class.java).apply {
                        putExtra("navigate_to", navigateTo)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                )
            )
    ) {
        Row(
            modifier = GlanceModifier
                .wrapContentWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = GlanceModifier
                    .size(28.dp)
                    .background(iconBackground)
                    .cornerRadius(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(iconRes),
                    contentDescription = title,
                    modifier = GlanceModifier.size(14.dp),
                    colorFilter = ColorFilter.tint(iconTint)
                )
            }
            Spacer(modifier = GlanceModifier.width(8.dp))
            Text(
                text = title,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                ),
                maxLines = 1
            )
        }
    }
}

class QuickActionsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = QuickActionsWidget()
}
