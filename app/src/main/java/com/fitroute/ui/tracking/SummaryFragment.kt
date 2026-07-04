package com.fitroute.ui.tracking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fitroute.R
import com.fitroute.databinding.FragmentSummaryBinding
import kotlinx.coroutines.launch

class SummaryFragment : Fragment() {

    private var _binding: FragmentSummaryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SummaryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ambil session ID dari arguments
        val sessionId = arguments?.getString("session_id") ?: return

        // Muat data sesi
        viewModel.loadSummary(sessionId)

        // Observe UI state
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                if (state.isLoading) return@collect

                state.session?.let { session ->

                    // Tipe aktivitas
                    val activityLabel = when (session.activityType) {
                        "RUNNING" -> "🏃 Lari"
                        "CYCLING" -> "🚴 Bersepeda"
                        "HIKING"  -> "🥾 Hiking"
                        else      -> session.activityType
                    }
                    binding.tvActivityTag.text = activityLabel

                    // Waktu mulai
                    val fmt = java.text.SimpleDateFormat(
                        "EEEE, dd MMM yyyy", java.util.Locale("id"))
                    val prefix = when (session.activityType) {
                        "RUNNING" -> "Lari pagi"
                        "CYCLING" -> "Bersepeda"
                        else      -> "Hiking"
                    }
                    binding.tvDateTime.text =
                        "$prefix · ${fmt.format(java.util.Date(session.startedAt))}"

                    // Stats utama
                    binding.tvDistance.text = "%.1f".format(session.distanceKm)
                    binding.tvCalories.text = "%.0f".format(session.caloriesKcal)

                    // Format durasi
                    val jam   = session.durationSec / 3600
                    val menit = (session.durationSec % 3600) / 60
                    val detik = session.durationSec % 60
                    binding.tvDuration.text = if (jam > 0)
                        "%d:%02d:%02d".format(jam, menit, detik)
                    else
                        "%d:%02d".format(menit, detik)

                    // Stats tambahan
                    val pMin = session.avgPace.toInt()
                    val pSec = ((session.avgPace - pMin) * 60).toInt()
                    binding.tvPace.text    = "%d:%02d".format(pMin, pSec)
                    binding.tvElevGain.text = "+%.0fm".format(session.elevGainM)

                    // Badge Personal Record
                    if (state.isPersonalRecord) {
                        binding.tagPR.visibility = View.VISIBLE
                    }

                    // Grafik elevasi dummy
                    val elevData = listOf(100f,120f,140f,160f,180f,175f,170f,168f)
                    binding.elevationChart.setData(elevData, elevData.map { it - 5f })

                    // Pace per km
                    addPaceBarChart(state.pacePerKm)
                }
            }
        }

        // Tombol back
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Tombol Selesai → kembali ke Dashboard
        binding.btnDone.setOnClickListener {
            findNavController().navigate(R.id.action_summary_to_dashboard)
        }

        // Tombol Bagikan
        binding.btnShare.setOnClickListener {
            shareSession()
        }
    }

    private fun addPaceBarChart(paces: List<Double>) {
        binding.paceChartContainer.removeAllViews()
        if (paces.isEmpty()) return

        val maxPace = paces.maxOrNull() ?: 1.0

        paces.forEachIndexed { index, pace ->
            val col = LinearLayout(requireContext()).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                gravity = android.view.Gravity.BOTTOM or android.view.Gravity.CENTER_HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    0, android.view.ViewGroup.LayoutParams.MATCH_PARENT, 1f
                )
            }

            // Bar
            val bar = android.view.View(requireContext()).apply {
                background = if (index == paces.size / 2)
                    requireContext().getDrawable(R.drawable.bg_pace_bar_dark)
                else
                    requireContext().getDrawable(R.drawable.bg_pace_bar_light)

                layoutParams = LinearLayout.LayoutParams(
                    16.dpToPx(), (80 * pace / maxPace).toInt().dpToPx()
                )
            }

            // Label km
            val label = android.widget.TextView(requireContext()).apply {
                text = "${index + 1}"
                textSize = 9f
                setTextColor(android.graphics.Color.parseColor("#888888"))
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 4.dpToPx() }
            }

            col.addView(bar)
            col.addView(label)
            binding.paceChartContainer.addView(col)
        }
    }

    private fun Int.dpToPx(): Int =
        (this * resources.displayMetrics.density).toInt()

    private fun shareSession() {
        val state = viewModel.uiState.value
        val session = state.session ?: return
        val text = """
            Saya baru selesai ${session.activityType.lowercase()} bersama FitRoute!
            📏 ${session.distanceKm} km
            ⏱ ${session.durationSec / 60} menit
            🔥 ${session.caloriesKcal.toInt()} kcal
            ⛰ +${session.elevGainM.toInt()}m elevasi
        """.trimIndent()

        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, text)
        }
        startActivity(android.content.Intent.createChooser(intent, "Bagikan via"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}