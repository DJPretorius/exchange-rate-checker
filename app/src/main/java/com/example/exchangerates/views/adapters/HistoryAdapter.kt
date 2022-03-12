package com.example.exchangerates.views.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.exchangerates.R
import com.example.exchangerates.databinding.ViewHolderExchangeRateHistoryBinding
import com.example.exchangerates.modles.db.ExchangeRateHistory

class HistoryAdapter(private val context : Context) : PagingDataAdapter<ExchangeRateHistory, HistoryAdapter.ViewHolder>(FX_COMPARATOR) {
    inner class ViewHolder(val binding : ViewHolderExchangeRateHistoryBinding) : RecyclerView.ViewHolder(binding.root) {}

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        binding.exchangeRate = getItem(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<ViewHolderExchangeRateHistoryBinding>(LayoutInflater.from(context),
            R.layout.view_holder_exchange_rate_history, parent, false)

        return ViewHolder(binding)
    }

    companion object {
        private val FX_COMPARATOR = object : DiffUtil.ItemCallback<ExchangeRateHistory>() {
            override fun areItemsTheSame(oldItem: ExchangeRateHistory, newItem: ExchangeRateHistory
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ExchangeRateHistory, newItem: ExchangeRateHistory
            ): Boolean {
                return oldItem.id == newItem.id
            }

        }
    }

}