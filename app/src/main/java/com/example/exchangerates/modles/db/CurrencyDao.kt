package com.example.exchangerates.modles.db

import androidx.room.*

@Dao
interface CurrencyDao {
    @Query("Select * from currencies")
    suspend fun getAllCurrencies() : List<Currency>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun storeCurrency(vararg currency: Currency)

    @Query("Delete from currencies")
    suspend fun deleteAllCurrencies()
}