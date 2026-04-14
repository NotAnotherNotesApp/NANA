package com.allubie.nana.widget

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class BudgetWidgetRefreshWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    private companion object {
        const val TAG = "BudgetWidgetRefresh"
    }

    override suspend fun doWork(): Result {
        return try {
            updateBudgetWidget(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Fallback budget widget refresh worker failed", e)
            Result.retry()
        }
    }
}
