package com.hyperrecursion.home_screen_vault2.scanner

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.hyperrecursion.home_screen_vault2.widget.WidgetStateRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class ScanningWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val widgetStateRepo: WidgetStateRepository,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {

        widgetStateRepo.updateByNewScan().let { result ->
            if (result.isSuccess) {
                Log.d(
                    "ScanningWorker",
                    "ScanningWorker.doWork(): Successfully updated widget state"
                )
                return Result.success()
            } else {
                Log.e(
                    "ScanningWorker",
                    "ScanningWorker.doWork(): Failed to update widget state: ${result.exceptionOrNull()}"
                )
                return Result.failure()
            }
        }
    }

    companion object {
        const val WORK_NAME = "ScanningWorker"
        fun updateWork(intervalSecs: Long, context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<ScanningWorker>(
                // Set the interval for subsequent scans.
                repeatInterval = intervalSecs,
                // Set the time unit for the interval.
                TimeUnit.SECONDS
            ).build()

            val operation = WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                // Set a unique tag for the work request.
                "scanning_worker",
                // Set the existing work policy.
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                // Set the work request.
                workRequest
            )
            Log.d("ScanningWorker", "ScanningWorker.updateWork(): $operation, $intervalSecs seconds")
        }
    }

}