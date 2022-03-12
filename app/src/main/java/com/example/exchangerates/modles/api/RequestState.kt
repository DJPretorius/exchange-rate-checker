package com.example.exchangerates.modles.api

data class RequestState<out T> (val status : CurrentStatus, val data : T?, val message: String?) {
    enum class CurrentStatus {
        LOADING,
        ERROR,
        SUCCESS
    }

    companion object {
        fun <T> success(data : T?) : RequestState<T> {
            return RequestState(CurrentStatus.SUCCESS, data, null)
        }

        fun <T> error(data : T?, message : String?) : RequestState<T> {
            return RequestState(CurrentStatus.ERROR, data, message)
        }

        fun <T> loading(data : T?) : RequestState<T> {
            return RequestState(CurrentStatus.LOADING, data, null)
        }
    }
}

