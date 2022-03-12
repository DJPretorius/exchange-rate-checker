package com.example.exchangerates.views.fragments

import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.exchangerates.viewmodels.HomeViewModel
import com.example.exchangerates.R
import com.example.exchangerates.databinding.HomeFragmentBinding
import com.example.exchangerates.modles.api.RequestState
import com.example.exchangerates.modles.db.Currency
import com.example.exchangerates.modles.db.ExchangeRateHistory
import com.example.exchangerates.views.adapters.HistoryAdapter
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.DataPoint

import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@ExperimentalPagingApi
class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()
    private var _binding : HomeFragmentBinding? = null
    private val binding : HomeFragmentBinding get() =_binding!!
    private val fromAdapter by lazy {  ArrayAdapter<Currency>(requireContext(), android.R.layout.simple_spinner_item) }
    private val toAdapter by lazy {  ArrayAdapter<Currency>(requireContext(), android.R.layout.simple_spinner_item) }
    private val pagerAdapter by lazy { HistoryAdapter(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(layoutInflater, R.layout.home_fragment, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.startPeriodicCurrencyCheck()

        return binding.root
    }

    private val fromSpinnerOnItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(arg0: AdapterView<*>?, arg1: View?, position: Int, id: Long) {
            viewModel.setSelectedFromCurrency(position)
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
        }
    }

    private val toSpinnerOnItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(arg0: AdapterView<*>?, arg1: View?, position: Int, id: Long) {
            viewModel.setSelectedToCurrency(position)
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toEdit.inputType = InputType.TYPE_NULL
        binding.fromEdit.inputType = InputType.TYPE_NULL

        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.fromSpinner.onItemSelectedListener = fromSpinnerOnItemSelectedListener

        viewModel.fromCurrencyList.observe(viewLifecycleOwner) {
            if (it != null) {
                fromAdapter.clear()
                fromAdapter.addAll(it)
                binding.fromSpinner.adapter = fromAdapter
            }
        }

        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.toSpinner.onItemSelectedListener = toSpinnerOnItemSelectedListener

        viewModel.toCurrencyList.observe(viewLifecycleOwner) {
            if (it != null) {
                toAdapter.clear()
                toAdapter.addAll(it)
                binding.toSpinner.adapter = toAdapter
            }
        }

        viewModel.allCurrencies.observe(viewLifecycleOwner) {
            if (it.status == RequestState.CurrentStatus.SUCCESS) {
                viewModel.fromCurrencyList.value = it.data
                viewModel.toCurrencyList.value = it.data?.map { c -> c.copy() }
            } else if (it.status == RequestState.CurrentStatus.ERROR) {
                Toast.makeText(requireContext(), "Something not success", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.currentExchangeRate.observe(viewLifecycleOwner) {
            if (it.status == RequestState.CurrentStatus.SUCCESS) {
                val exchangeRate = it.data!!
                binding.fromEdit.setText("1")
                binding.toEdit.setText(exchangeRate.price.toString())

                viewModel.fetchTimeSeriesExchangeRate()
            } else if (it.status == RequestState.CurrentStatus.ERROR) {
                Toast.makeText(requireContext(), "Something not success", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.timeSeriesData.observe(viewLifecycleOwner) {
            if (it.status == RequestState.CurrentStatus.SUCCESS) {
                val series: LineGraphSeries<DataPoint> = LineGraphSeries()
                val dataMap = it.data

                var minDate = ""

                dataMap?.let { map ->
                    map.forEach { point: Map.Entry<Long, Double> ->
                        val time = Date(point.key)
                        series.appendData(DataPoint(time, point.value), false, map.size)
                    }

                    val maxTime = map.keys.maxOrNull()?.toDouble() ?: 0.0
                    val minTime = map.keys.minOrNull()?.toDouble() ?: 0.0
                    minDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(minTime.toLong()))

                    binding.graph.viewport.setMinX(minTime)
                    binding.graph.viewport.setMaxX(maxTime)
                    binding.graph.viewport.isXAxisBoundsManual = true
                    binding.graph.gridLabelRenderer.numHorizontalLabels = 3

                    val maxY = map.values.maxOrNull() ?: 0.0
                    val minY = map.values.minOrNull() ?: 0.0
                    binding.graph.viewport.setMaxY(maxY)
                    binding.graph.viewport.setMinY(minY)
                    binding.graph.viewport.isYAxisBoundsManual = true
                    binding.graph.gridLabelRenderer.numVerticalLabels = 3

                }

                binding.graph.removeAllSeries()
                binding.graph.addSeries(series)

                binding.graph.gridLabelRenderer.labelFormatter =  DateAsXAxisLabelFormatter(requireContext())
                binding.graph.gridLabelRenderer.setHumanRounding(false)

                binding.graph.title = "Exchange Rate of ${viewModel.selectedFromCurrency?.symbol} to ${viewModel.selectedToCurrency?.symbol} since $minDate"
            } else if (it.status == RequestState.CurrentStatus.ERROR) {
                Toast.makeText(requireContext(), "Something not success", Toast.LENGTH_SHORT).show()
            }
        }

        binding.historicList.adapter = pagerAdapter
        binding.historicList.layoutManager = LinearLayoutManager(requireContext())

        viewModel.fetchHistoriesPagerData().observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                pagerAdapter.submitData(it)
            }
        }
    }
}