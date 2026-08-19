package com.allubie.nana.widget

import android.content.Context
import androidx.glance.appwidget.updateAll

suspend fun updateAllWidgets(context: Context) {
    QuickActionsWidget().updateAll(context)
    RecentNotesWidget().updateAll(context)
    ChecklistWidget().updateAll(context)
}

suspend fun updateBudgetWidget(context: Context) {
    // No-op while decoupled
}

suspend fun updateNotesWidgets(context: Context) {
    RecentNotesWidget().updateAll(context)
}

suspend fun updateChecklistWidgets(context: Context) {
    ChecklistWidget().updateAll(context)
}
