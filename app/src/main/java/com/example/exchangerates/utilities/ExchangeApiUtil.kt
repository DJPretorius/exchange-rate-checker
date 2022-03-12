package com.example.exchangerates.utilities

import com.example.exchangerates.modles.ExchangeRate
import com.example.exchangerates.modles.db.Currency
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Exception

class ExchangeApiUtil(private val apiKey : String) {

    fun getExchangeRateQueryMap(currencyPair : String): MutableMap<String, String> {
        val map = mutableMapOf<String, String>()
        map["api_key"] = apiKey
        map["currency"] = currencyPair

        return map
    }

    fun getTimeSeriesQueryMap(currencyPair: String, startDate : String, endDate : String): MutableMap<String, String> {
        val map = mutableMapOf<String, String>()
        map["api_key"] = apiKey
        map["currency"] = currencyPair
        map["format"] = "close"
        map["interval"] = "daily"
        map["start_date"] = startDate
        map["end_date"] = endDate

        return map
    }

    /**
     * Parse the JSON String provided as argument and return a map that can be used to create a graph
     * @param responseBody a String in JSON format representing the time series data
     */
    fun getTimeSeriesData(responseBody: String): MutableMap<Long, Double>? {
        val timeSeriesMap = mutableMapOf<Long, Double>()
        try {
            val responseJson = JSONObject(responseBody)

            val prices = responseJson.getJSONObject("price")

            val keys = prices.keys()

            keys.forEach { key ->
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(key)
                val time = date!!.time
                val priceObj = prices.getJSONObject(key)
                val price : Double = priceObj.keys().next().let {
                    priceObj.getDouble(it)
                }

                timeSeriesMap[time] = price
            }

            return timeSeriesMap
        } catch (e : Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun getExchangeRate(responseBody: String): ExchangeRate? {
        var exchangeRate : ExchangeRate? = null
        try {
            val responseJson = JSONObject(responseBody)
            val priceObj = responseJson.getJSONObject("price")
            val currencies = priceObj.keys().next()
            val price = priceObj.getDouble(currencies)

            val timeStamp = responseJson.getLong("timestamp")

            exchangeRate = ExchangeRate(price, timeStamp, currencies)
        } catch (e : Exception) {
            e.printStackTrace()
        }

        return exchangeRate
    }

    /**
     * Parse response from all currencies and return a map of abbreviation as key and name as value.
     */
    fun getAvailableCurrencies(responseBody: String): MutableList<Currency> {
        val currencies = mutableListOf<Currency>()
        try {
            val responseJson = JSONObject(responseBody).getJSONObject("currencies")

            responseJson.keys().forEach {
                val name = responseJson.getString(it)
                val symbol = it.replace("USD", "")
                currencies.add(Currency(0, symbol, name))
            }
        } catch (e : Exception) {
            e.printStackTrace()
        }

        currencies.add(Currency(0, "USD", "United States Dollar"))

        return currencies
    }
}