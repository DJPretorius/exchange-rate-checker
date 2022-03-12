package com.example.exchangerates.modles.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Date

@Entity(tableName = "exchange_rate_histories")
data class ExchangeRateHistory (
    @PrimaryKey(autoGenerate = true)
    val id : Int = 0,
    @ColumnInfo(name = "from_currency")
    val fromCurrency : String,
    @ColumnInfo(name = "to_currency")
    val toCurrency : String,
    @ColumnInfo(name = "access_date")
    val accessDate : String,
    @ColumnInfo(name = "exchange_rate")
    val exchangeRate : Double
)
