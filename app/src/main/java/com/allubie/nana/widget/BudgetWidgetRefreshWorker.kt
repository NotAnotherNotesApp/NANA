package com.allubie.nana.widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class BudgetWidgetRefreshWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        return try {
            updateBudgetWidget(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
