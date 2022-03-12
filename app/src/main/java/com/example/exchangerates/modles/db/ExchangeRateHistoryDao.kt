package com.example.exchangerates.modles.db

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ExchangeRateHistoryDao {

    @Query("SELECT * FROM exchange_rate_histories")
    fun getAllHistories() : PagingSource<Int, ExchangeRateHistory>

    @Insert
    suspend fun insertExchangeRate(exchangeRateHistory: ExchangeRateHistory)
}