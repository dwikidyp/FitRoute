package com.fitroute.ui.history

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fitroute.databinding.FragmentDetailSessionBinding
import com.fitroute.util.ShareImageGenerator
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetailSessionFragment : Fragment() {

    private var _binding: FragmentDetailSessionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DetailSessionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailSessionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sessionId = arguments?.getString("session_id") ?: return
        viewModel.loadDetail(sessionId)

        // Observe state
        lifecycleScope.launch {
            viewModel.detailState.collect { state ->
                if (state.isLoading) return@collect

                // Navigasi balik jika sudah dihapus
                if (state.navigateBack) {
                    findNavController().popBackStack()
                    return@collect
                }

                state.session?.let { session ->

                    // Tipe aktivitas
                    val label = when (session.activityType) {
                        "RUNNING" -> "🏃 Lari pagi"
                        "CYCLING" -> "🚴 Sepeda pagi"
                        "HIKING"  -> "🥾 Hiking"
                        else      -> session.activityType
                    }
                    binding.tvActivityType.text = label

                    // Tanggal & waktu
                    val fmt = SimpleDateFormat(
                        "EEEE, dd MMM yyyy · HH:mm", Locale("id")
                    )
                    binding.tvDateTime.text =
                        fmt.format(Date(session.startedAt))

                    // Stats utama
                    binding.tvDistance.text = "%.1f".format(session.distanceKm)
                    binding.tvCalories.text = "%.0f".format(session.caloriesKcal)

                    val jam   = session.durationSec / 3600
                    val menit = (session.durationSec % 3600) / 60
                    val detik = session.durationSec % 60
                    binding.tvDuration.text = if (jam > 0)
                        "%d:%02d:%02d".format(jam, menit, detik)
                    else "%d:%02d".format(menit, detik)

                    // Stats tambahan
                    val pMin = session.avgPace.toInt()
                    val pSec = ((session.avgPace - pMin) * 60).toInt()
                    binding.tvPace.text    = "%d:%02d".format(pMin, pSec)
                    binding.tvElevGain.text = "+%.0fm".format(session.elevGainM)
                    binding.tvSpeed.text   = "%.1f".format(session.avgSpeedKmh)

                    // Grafik elevasi dummy
                    val elev = listOf(100f,115f,135f,160f,180f,175f,165f,155f)
                    binding.elevationChart.setData(elev, elev.map { it - 8f })

                    // Tombol Bagikan
                    binding.btnShare.setOnClickListener {
                        shareSession(session)
                    }

                    // Tombol Simpan foto
                    binding.btnSave.setOnClickListener {
                        saveShareCard(session)
                    }
                }
            }
        }

        // Tombol back
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Tombol Hapus — tampilkan konfirmasi dulu
        binding.btnDelete.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Hapus sesi?")
                .setMessage("Sesi ini akan dihapus permanen dan tidak bisa dikembalikan.")
                .setPositiveButton("Hapus") { _, _ ->
                    viewModel.deleteSession(sessionId)
                }
                .setNegativeButton("Batal", null)
                .show()
        }
    }

    // Bagikan sebagai teks
    private fun shareSession(session: com.fitroute.data.local.WorkoutSessionEntity) {
        val text = """
            Sesi ${session.activityType.lowercase()} saya di FitRoute:
            📏 ${"%.1f".format(session.distanceKm)} km
            ⏱ ${session.durationSec / 60} menit
            🔥 ${session.caloriesKcal.toInt()} kcal
            ⛰ +${session.elevGainM.toInt()}m elevasi
            
            #FitRoute #Fitness
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, "Bagikan via"))
    }

    // Simpan kartu ringkasan sebagai gambar
    private fun saveShareCard(session: com.fitroute.data.local.WorkoutSessionEntity) {
        val bitmap = ShareImageGenerator.generateShareCard(requireContext(), session)

        try {
            // Simpan ke Pictures/FitRoute
            val filename = "fitroute_${System.currentTimeMillis()}.png"
            val resolver = requireContext().contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/FitRoute")
            }

            val uri = resolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
            )
            uri?.let {
                resolver.openOutputStream(it)?.use { stream ->
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
                }
                android.widget.Toast.makeText(
                    requireContext(),
                    "Gambar disimpan ke Galeri ✓",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            android.widget.Toast.makeText(
                requireContext(),
                "Gagal menyimpan: ${e.message}",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}