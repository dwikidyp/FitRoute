package com.fitroute.ui.tracking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fitroute.R
import com.fitroute.databinding.FragmentSummaryBinding
import com.fitroute.databinding.ItemElevationSegmentBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
                    binding.tvActivityType.text = activityLabel

                    // Waktu mulai
                    val dateFormat = SimpleDateFormat(
                        "EEEE, dd MMM yyyy · HH:mm",
                        Locale("id")
                    )
                    binding.tvDateTime.text = dateFormat.format(Date(session.startedAt))

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
                    val paceMin = session.avgPace.toInt()
                    val paceSec = ((session.avgPace - paceMin) * 60).toInt()
                    binding.tvPace.text    = "%d:%02d".format(paceMin, paceSec)
                    binding.tvElevGain.text = "+%.0fm".format(session.elevGainM)
                    binding.tvSpeed.text   = "%.1f".format(session.avgSpeedKmh)

                    // Badge Personal Record
                    if (state.isPersonalRecord) {
                        binding.tvPersonalRecord.visibility = View.VISIBLE
                    }

                    // Pace per km
                    addPacePerKm(state.pacePerKm)
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

    private fun addPacePerKm(paces: List<Double>) {
        val maxPace = paces.maxOrNull() ?: 1.0
        paces.forEachIndexed { index, pace ->
            val itemBinding = ItemElevationSegmentBinding.inflate(
                layoutInflater, binding.paceContainer, false
            )
            itemBinding.tvSegmentLabel.text = "km ${index + 1}"
            val paceMin = pace.toInt()
            val paceSec = ((pace - paceMin) * 60).toInt()
            itemBinding.tvSegmentValue.text = "%d:%02d".format(paceMin, paceSec)

            itemBinding.root.post {
                val parentWidth = (itemBinding.viewSegmentBar.parent as View).width
                val params = itemBinding.viewSegmentBar.layoutParams
                params.width = (parentWidth * (pace / maxPace)).toInt()
                itemBinding.viewSegmentBar.layoutParams = params
            }

            binding.paceContainer.addView(itemBinding.root)
        }
    }

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