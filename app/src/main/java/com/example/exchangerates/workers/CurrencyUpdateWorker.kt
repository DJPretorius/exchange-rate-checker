package com.example.exchangerates.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.exchangerates.R
import com.example.exchangerates.modles.AppRepository
import com.example.exchangerates.modles.api.RequestState
import com.example.exchangerates.utilities.NotificationUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception

class CurrencyUpdateWorker(context: Context, workerParams: WorkerParameters)
    : CoroutineWorker(context, workerParams) {

    private val notificationUtil = NotificationUtil()

    override suspend fun doWork(): Result {
        notificationUtil.makeSyncNotification(applicationContext.getString(R.string.sync_currency_notification_message),
            applicationContext.getString(R.string.sync_currency_notification_title),
            applicationContext, NotificationUtil.CURRENCIES_NOTIFICATION_ID)

        return withContext<Result>(Dispatchers.IO) {
            val repository = AppRepository(applicationContext)
            return@withContext try {
                val currencies = repository.fetchCurrenciesRemote()
                if (currencies.status == RequestState.CurrentStatus.SUCCESS) {
                    Result.success()
                } else {
                    Result.retry()
                }
            } catch (e : Exception) {
                e.printStackTrace()
                Result.failure()
            }
        }
    }
}