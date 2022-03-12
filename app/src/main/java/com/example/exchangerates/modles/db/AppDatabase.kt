package com.example.exchangerates.modles.db

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ExchangeRateHistory::class, Currency::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun exchangeRateHistoryDao() : ExchangeRateHistoryDao

    abstract fun currencyDao() : CurrencyDao

    companion object {
        private const val DB_NAME = "exchange_rate_histories"
        private lateinit var instance : AppDatabase

        @Synchronized
        fun getInstance(context: Context): AppDatabase {
            if (!this::instance.isInitialized) {
                instance = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DB_NAME)
                    .build()
            }

            return instance
        }
    }
}