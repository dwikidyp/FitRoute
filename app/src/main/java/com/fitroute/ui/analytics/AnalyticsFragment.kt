package com.fitroute.ui.analytics

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.fitroute.R
import com.fitroute.databinding.FragmentAnalyticsBinding
import kotlinx.coroutines.launch

class AnalyticsFragment : Fragment() {

    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AnalyticsViewModel by viewModels()

    // Data chart per hari
    private val weeklyChart = listOf(5.4f, 0f, 7.8f, 6.2f, 0f, 12.1f, 2.7f)
    private val monthlyChart = listOf(22f, 18f, 34f, 28f, 15f, 40f, 32f)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Muat data awal
        viewModel.loadWeeklyData()

        // Observe data dari ViewModel
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                if (state.isLoading) return@collect

                val agg = state.aggregate
                val pr  = state.personalRecords

                // Stats ringkasan
                binding.tvTotalKm.text =
                    "%.1f".format(agg.totalDistance)
                binding.tvTotalKcal.text =
                    "%,.0f".format(agg.totalCalories)
                        .replace(",", ".")
                binding.tvTotalSessions.text =
                    agg.sessionCount.toString()

                // Personal records
                binding.tvPRDistance.text =
                    "%.1f km · ${pr.longestDistanceActivity}"
                        .format(pr.longestDistanceKm)
                binding.tvPRCalories.text =
                    "%,.0f kcal · ${pr.highestCaloriesActivity}"
                        .format(pr.highestCalories)
                        .replace(",", ".")
                binding.tvBestElevation.text =
                    "+%.0f m · ${pr.highestElevationActivity}"
                        .format(pr.highestElevationM)

                val paceMin = pr.bestPaceMinKm.toInt()
                val paceSec = ((pr.bestPaceMinKm - paceMin) * 60).toInt()
                binding.tvBestPace.text =
                    "%d:%02d min/km · ${pr.bestPaceActivity}"
                        .format(paceMin, paceSec)
            }
        }

        // Chart mingguan
        binding.distanceChart.setData(weeklyChart, activeDay = 2)

        // Period selector
        binding.chipWeek.setOnClickListener {
            setActivePeriod(binding.chipWeek)
            viewModel.loadWeeklyData()
            binding.distanceChart.setData(weeklyChart, activeDay = 2)
        }
        binding.chipMonth.setOnClickListener {
            setActivePeriod(binding.chipMonth)
            viewModel.loadMonthlyData()
            binding.distanceChart.setData(monthlyChart, activeDay = 3)
        }
        binding.chipYear.setOnClickListener {
            setActivePeriod(binding.chipYear)
            viewModel.loadMonthlyData()
            binding.distanceChart.setData(monthlyChart, activeDay = 5)
        }
    }

    private fun setActivePeriod(active: TextView) {
        listOf(binding.chipWeek, binding.chipMonth, binding.chipYear)
            .forEach { chip ->
                val isActive = chip == active
                chip.setBackgroundResource(
                    if (isActive) R.drawable.bg_period_chip_active
                    else R.drawable.bg_period_chip_inactive
                )
                chip.setTextColor(
                    if (isActive) Color.parseColor("#1B4332")
                    else Color.parseColor("#99FFFFFF")
                )
                chip.typeface = android.graphics.Typeface.create(
                    android.graphics.Typeface.DEFAULT,
                    if (isActive) android.graphics.Typeface.BOLD
                    else android.graphics.Typeface.NORMAL
                )
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}