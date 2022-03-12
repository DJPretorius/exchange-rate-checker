package com.example.exchangerates.viewmodels

import android.app.Application
import androidx.lifecycle.*
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.work.*
import com.example.exchangerates.modles.AppRepository
import com.example.exchangerates.modles.ExchangeRate
import com.example.exchangerates.modles.ExchangeRateHistoryRepository
import com.example.exchangerates.modles.api.RequestState
import com.example.exchangerates.modles.db.Currency
import com.example.exchangerates.modles.db.ExchangeRateHistory
import com.example.exchangerates.workers.CurrencyUpdateWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

@ExperimentalPagingApi
class HomeViewModel(application: Application) : AndroidViewModel(application) {
    // View binding fields
    val fromLabel = MutableLiveData<String>()
    val toLabel = MutableLiveData<String>()
    val fromCurrencyList = MutableLiveData<List<Currency>?>()
    val toCurrencyList = MutableLiveData<List<Currency>?>()
    var selectedFromCurrency : Currency? = null
    var selectedToCurrency : Currency? = null
    val currentExchangeRate = MutableLiveData<RequestState<ExchangeRate>>()
    val timeSeriesData = MutableLiveData<RequestState<Map<Long, Double>>>()
    val isLoading = MutableLiveData(false)

    // Model related fields
    private val appRepository = AppRepository(application)
    private val exchangeRateHistoryRepository = ExchangeRateHistoryRepository(application)
    private val workManager = WorkManager.getInstance(application)

    val allCurrencies = liveData<RequestState<List<Currency>>> {
        isLoading.postValue(true)

        val currencies = appRepository.fetchCurrenciesLocal()
        emit(RequestState.success(currencies))
        isLoading.postValue(false)
        if (currencies.isEmpty()) {
            isLoading.postValue(true)
            withContext(Dispatchers.IO) {
                val requestState = appRepository.fetchCurrenciesRemote()
                isLoading.postValue(false)
                if (requestState.status == RequestState.CurrentStatus.SUCCESS) {
                    requestState.data?.let {
                        val fetchedCurrencies = appRepository.fetchCurrenciesLocal()
                        emit(RequestState.success(fetchedCurrencies))
                    }
                } else {
                    emit(requestState)
                }
            }
        }
    }

    fun setSelectedFromCurrency(position : Int) {
        fromCurrencyList.value?.let { fromCurrencyList ->
            selectedFromCurrency = fromCurrencyList[position]
            fromLabel.value = selectedFromCurrency?.symbol

        }
    }

    fun setSelectedToCurrency(position : Int) {
        fromCurrencyList.value?.let { toCurrencyList ->
            selectedToCurrency = toCurrencyList[position]
            toLabel.value = selectedToCurrency?.symbol

        }
    }

    fun fetchCurrentExchangeRate() {
        if (selectedToCurrency == null || selectedFromCurrency == null) {
            currentExchangeRate.value = RequestState.error(null, "Please select currencies to compare.")
            return
        } else if (selectedToCurrency?.id == selectedFromCurrency?.id) {
            currentExchangeRate.value = RequestState.error(null, "Please select different currencies to compare.")
            return
        }

        viewModelScope.launch {
            isLoading.postValue(true)
            currentExchangeRate.value = appRepository.getExchangeRate(selectedFromCurrency!!.symbol + selectedToCurrency!!.symbol)
            isLoading.postValue(false)
        }
    }

    fun fetchTimeSeriesExchangeRate() {
        if (selectedToCurrency == null || selectedFromCurrency == null) {
            timeSeriesData.value = RequestState.error(null, "Please select currencies to compare.")
            return
        } else if (selectedToCurrency?.id == selectedFromCurrency?.id) {
            timeSeriesData.value = RequestState.error(null, "Please select different currencies to compare.")
            return
        }

        viewModelScope.launch {
            isLoading.postValue(true)
            timeSeriesData.value = appRepository.getTimeSeries(selectedFromCurrency!!.symbol + selectedToCurrency!!.symbol)
            isLoading.postValue(false)
        }
    }

    // Pager
    fun fetchHistoriesPagerData(): LiveData<PagingData<ExchangeRateHistory>> {
        return exchangeRateHistoryRepository.getHistoriesPagingData()
            .cachedIn(viewModelScope)
    }

    // Work Manager
    fun startPeriodicCurrencyCheck() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresStorageNotLow(true)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<CurrencyUpdateWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30L, TimeUnit.MINUTES)
            .build()

        workManager.enqueueUniquePeriodicWork(
            SYNC_CURRENCIES_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    companion object {
        const val SYNC_CURRENCIES_WORK_NAME = "sync_currencies"
    }

}
