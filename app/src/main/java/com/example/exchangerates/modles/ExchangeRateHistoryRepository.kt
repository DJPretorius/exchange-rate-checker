package com.example.exchangerates.modles

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.paging.*
import com.example.exchangerates.modles.db.AppDatabase
import com.example.exchangerates.modles.db.ExchangeRateHistory
import com.example.exchangerates.modles.db.ExchangeRateHistoryDao

@ExperimentalPagingApi
class ExchangeRateHistoryRepository(context: Context) {

    private val exchangeRateHistoryDao = AppDatabase.getInstance(context).exchangeRateHistoryDao()

    fun getHistoriesPagingData(): LiveData<PagingData<ExchangeRateHistory>> {
        val pagingSourceFactory = { exchangeRateHistoryDao.getAllHistories() }
        val pagingConfig = PagingConfig(DEFAULT_PAGE_SIZE)
        return Pager(
            config = pagingConfig,
            pagingSourceFactory = pagingSourceFactory
        ).liveData
    }

    companion object {
        const val DEFAULT_PAGE_SIZE = 5
    }
}