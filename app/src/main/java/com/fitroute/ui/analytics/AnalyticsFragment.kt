package com.fitroute.ui.analytics

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.fitroute.R
import com.fitroute.databinding.FragmentAnalyticsBinding

class AnalyticsFragment : Fragment() {

    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!

    // Data dummy
    private val weeklyData = listOf(5.4f, 0f, 7.8f, 6.2f, 0f, 12.1f, 2.7f)
    private val monthlyData = listOf(22f, 18f, 34f, 28f, 15f, 40f, 32f)

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

        // Set data awal
        loadWeeklyData()

        // Period selector
        binding.chipWeek.setOnClickListener {
            setActivePeriod(binding.chipWeek)
            loadWeeklyData()
        }
        binding.chipMonth.setOnClickListener {
            setActivePeriod(binding.chipMonth)
            loadMonthlyData()
        }
        binding.chipYear.setOnClickListener {
            setActivePeriod(binding.chipYear)
            loadMonthlyData()
        }
    }

    private fun loadWeeklyData() {
        // Stats ringkasan
        binding.tvTotalKm.text       = "34.2"
        binding.tvTotalKcal.text     = "3.394"
        binding.tvTotalSessions.text = "5"

        // Grafik jarak harian
        binding.distanceChart.setData(weeklyData, activeDay = 2)

        // Personal record
        binding.tvPRDistance.text   = "18.4 km · Sepeda"
        binding.tvPRCalories.text   = "1.840 kcal · Hiking"
        binding.tvBestElevation.text = "+820 m · Hiking"
        binding.tvBestPace.text     = "5:48 min/km · Lari"
    }

    private fun loadMonthlyData() {
        binding.tvTotalKm.text       = "148.6"
        binding.tvTotalKcal.text     = "14.820"
        binding.tvTotalSessions.text = "22"

        binding.distanceChart.setData(monthlyData, activeDay = 2)

        binding.tvPRDistance.text   = "18.4 km · Sepeda"
        binding.tvPRCalories.text   = "1.840 kcal · Hiking"
        binding.tvBestElevation.text = "+820 m · Hiking"
        binding.tvBestPace.text     = "5:48 min/km · Lari"
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