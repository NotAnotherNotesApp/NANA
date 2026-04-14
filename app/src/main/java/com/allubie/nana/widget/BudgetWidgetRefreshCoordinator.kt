package com.allubie.nana.widget

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.TimeUnit

private object BudgetWidgetRefreshCoordinator {
    private const val TAG = "BudgetWidgetRefresh"
    private const val RETRY_DELAY_MS = 300L
    private const val DEBOUNCE_MS = 250L
    private const val FALLBACK_WORK_NAME = "budget_widget_refresh_fallback"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val refreshLock = Mutex()
    private val refreshRequests = MutableSharedFlow<Context>(extraBufferCapacity = 1)

    init {
        refreshRequests
            .debounce(DEBOUNCE_MS)
            .onEach { context ->
                refreshLock.withLock {
                    val refreshed = runCatching {
                        updateBudgetWidget(context)
                    }.onFailure { error ->
                        Log.e(TAG, "Budget widget refresh failed, retrying", error)
                    }.isSuccess

                    if (!refreshed) {
                        delay(RETRY_DELAY_MS)
                        val retried = runCatching { updateBudgetWidget(context) }
                            .onFailure { error ->
                                Log.e(TAG, "Budget widget refresh failed after retry", error)
                            }
                            .isSuccess
                        if (!retried) {
                            // A fallback one-time WorkManager refresh has already been enqueued on request.
                        }
                    }
                }
            }
            .launchIn(scope)
    }

    fun requestRefresh(context: Context) {
        val appContext = context.applicationContext
        scheduleFallbackWork(appContext)
        if (!refreshRequests.tryEmit(appContext)) {
            scope.launch { refreshRequests.emit(appContext) }
        }
    }

    private fun scheduleFallbackWork(context: Context) {
        val request = OneTimeWorkRequestBuilder<BudgetWidgetRefreshWorker>()
            .setInitialDelay(5, TimeUnit.SECONDS)
            .setConstraints(Constraints.NONE)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            FALLBACK_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}

fun requestBudgetWidgetRefresh(context: Context) {
    BudgetWidgetRefreshCoordinator.requestRefresh(context)
}
