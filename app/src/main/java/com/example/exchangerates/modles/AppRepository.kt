package com.example.exchangerates.modles

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.exchangerates.R
import com.example.exchangerates.modles.api.RequestState
import com.example.exchangerates.modles.api.RetrofitAPI
import com.example.exchangerates.modles.db.AppDatabase
import com.example.exchangerates.modles.db.Currency
import com.example.exchangerates.modles.db.ExchangeRateHistory
import com.example.exchangerates.utilities.ExchangeApiUtil
import java.lang.Exception
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

class AppRepository(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val exchangeRateUtil by lazy { ExchangeApiUtil(context.getString(R.string.api_key)) }

    suspend fun fetchCurrenciesLocal(): List<Currency> {
        val dao = db.currencyDao()
        return dao.getAllCurrencies()
    }

    /**
     * Get all available currency pairs that can be queried by the API
     */
    suspend fun fetchCurrenciesRemote(): RequestState<MutableList<Currency>> {
        val response = RetrofitAPI.create(context).getCurrencies(context.getString(R.string.api_key))
        return if (response.isSuccessful) {
            try {
                val currencies = exchangeRateUtil.getAvailableCurrencies(response.body()!!.string())
                val dao = db.currencyDao()
                dao.deleteAllCurrencies()
                dao.storeCurrency(*currencies.toTypedArray())
                RequestState.success(currencies)
            } catch (e : Exception) {
                e.printStackTrace()
                RequestState.error(null, "Request could not be processed")
            }
        } else {
            RequestState.error(null, response.errorBody()!!.string())
        }
    }

    /**
     * Store the currencies in the DB
     */
    suspend fun storeCurrency(vararg currency: Currency) {
        db.currencyDao().storeCurrency(*currency)
    }

    /**
     * store the exchange rate in the DB so that it can be displayed as a history
     */
    private suspend fun storeExchangeRate(exchangeRate : ExchangeRate?) {
        if (exchangeRate == null) return

        val fromCurrency = exchangeRate.currencies.subSequence(0,3).toString()
        val toCurrency = exchangeRate.currencies.subSequence(3,6).toString()
        val accessDate = Date(exchangeRate.timestamp * 1000).let {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            format.format(it)
        }

        val exchangeRateHistory = ExchangeRateHistory(0, fromCurrency, toCurrency, accessDate, exchangeRate.price)
        db.exchangeRateHistoryDao().insertExchangeRate(exchangeRateHistory)
    }

    /**
     * Get a series of exchange rates for the given currency pair.
     */
    suspend fun getTimeSeries(currencyPair: String): RequestState<MutableMap<Long, Double>> {
        var today = getWeekdayDate(Calendar.getInstance())
        var monthAgo = (today.clone() as Calendar)
        monthAgo.add(Calendar.DAY_OF_YEAR, -30)
        monthAgo = getWeekdayDate(monthAgo)

        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayString = format.format(today.time)
        val monthAgoString = format.format(monthAgo.time)
        val queryMap = exchangeRateUtil.getTimeSeriesQueryMap(currencyPair, monthAgoString, todayString)

        val response = RetrofitAPI.create(context).getTimeSeries(queryMap)

        return if (response.isSuccessful) {
            val timeSeries = exchangeRateUtil.getTimeSeriesData(response.body()!!.string())
            RequestState.success(timeSeries)
        } else {
            RequestState.error(null, null)
        }
    }

    /**
     * Get the current exchange rate for the given currency pair.
     */
    suspend fun getExchangeRate(currencyPair: String): RequestState<ExchangeRate> {
        val queryMap = exchangeRateUtil.getExchangeRateQueryMap(currencyPair)
        val response = RetrofitAPI.create(context).getExchangeRate(queryMap)

        return if (response.isSuccessful) {
            val exchangeRate = exchangeRateUtil.getExchangeRate(response.body()!!.string())
            storeExchangeRate(exchangeRate)

            RequestState.success(exchangeRate)
        } else {
            RequestState.error(null, null)
        }
    }

    private fun getWeekdayDate(date : Calendar): Calendar {
        when (date.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SATURDAY -> date.add(Calendar.DAY_OF_YEAR, -1)
            Calendar.SUNDAY -> date.add(Calendar.DAY_OF_YEAR, -2)
        }

        return date
    }
}