package com.example.exchangerates.modles

data class ExchangeRate (
    val price : Double,
    val timestamp : Long,
    val currencies: String
)