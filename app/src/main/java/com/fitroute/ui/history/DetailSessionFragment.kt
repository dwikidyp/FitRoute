package com.fitroute.ui.history

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fitroute.databinding.FragmentDetailSessionBinding
import com.fitroute.util.ShareImageGenerator
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import android.graphics.Color
import com.fitroute.R
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetailSessionFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentDetailSessionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DetailSessionViewModel by viewModels()
    private var googleMap: GoogleMap? = null

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

        // Setup peta
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapPreview) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val sessionId = arguments?.getString("session_id") ?: return
        viewModel.loadDetail(sessionId)

        lifecycleScope.launch {
            viewModel.detailState.collect { state ->
                if (state.isLoading) return@collect
                if (state.navigateBack) {
                    findNavController().popBackStack()
                    return@collect
                }

                state.session?.let { session ->

                    // Nama & emoji aktivitas
                    val (emoji, label) = when (session.activityType) {
                        "RUNNING" -> Pair("🏃", "Lari")
                        "CYCLING" -> Pair("🚴", "Sepeda")
                        "HIKING"  -> Pair("🥾", "Hiking")
                        else      -> Pair("🏃", session.activityType)
                    }
                    binding.tvActivityEmoji.text = "$emoji "
                    binding.tvActivityTag.text   = label

                    // Subtitle header
                    val dateFmt = SimpleDateFormat("dd MMM", Locale("id"))
                    val namaAktivitas = when (session.activityType) {
                        "HIKING"  -> "Hiking"
                        "RUNNING" -> "Lari pagi"
                        "CYCLING" -> "Sepeda pagi"
                        else      -> label
                    }
                    binding.tvHeaderSubtitle.text =
                        "$namaAktivitas · ${dateFmt.format(Date(session.startedAt))}"

                    // Durasi di header
                    val jam   = session.durationSec / 3600
                    val menit = (session.durationSec % 3600) / 60
                    binding.tvHeaderDuration.text = if (jam > 0)
                        "${jam}j ${menit}m" else "${menit}m"

                    // Jarak di header
                    binding.tvHeaderDistance.text =
                        "%.1f".format(session.distanceKm)

                    // Stats utama
                    binding.tvDistance.text =
                        "%.1f".format(session.distanceKm)
                    binding.tvCalories.text =
                        "%,.0f".format(session.caloriesKcal).replace(",", ".")
                    binding.tvElevGain.text =
                        "+%.0fm".format(session.elevGainM)

                    // Grafik elevasi
                    val elevData = listOf(
                        100f,115f,140f,170f,200f,230f,
                        250f,240f,220f,200f,185f,170f
                    )
                    binding.elevationChart.setData(
                        elevData,
                        elevData.map { it - 10f }
                    )
                }
            }
        }

        // Tombol back
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Tombol Hapus
        binding.btnDelete.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Hapus sesi?")
                .setMessage("Data sesi ini akan dihapus permanen.")
                .setPositiveButton("Hapus") { _, _ ->
                    viewModel.deleteSession(
                        arguments?.getString("session_id") ?: return@setPositiveButton
                    )
                }
                .setNegativeButton("Batal", null)
                .show()
        }

        // Tombol Bagikan
        binding.btnShare.setOnClickListener {
            val session = viewModel.detailState.value.session ?: return@setOnClickListener
            val text = """
                Sesi ${session.activityType.lowercase()} di FitRoute!
                📏 ${"%.1f".format(session.distanceKm)} km
                ⏱ ${session.durationSec / 60} menit
                🔥 ${session.caloriesKcal.toInt()} kcal
                ⛰ +${session.elevGainM.toInt()}m elevasi
                #FitRoute
            """.trimIndent()

            startActivity(
                Intent.createChooser(
                    Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, text)
                    }, "Bagikan via"
                )
            )
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.apply {
            uiSettings.isZoomControlsEnabled    = false
            uiSettings.isScrollGesturesEnabled  = false
            uiSettings.isZoomGesturesEnabled    = false
            mapType = GoogleMap.MAP_TYPE_NORMAL
        }

        // Gambar rute dummy
        val dummyRoute = listOf(
            LatLng(-6.7600, 107.0050),
            LatLng(-6.7580, 107.0080),
            LatLng(-6.7560, 107.0110),
            LatLng(-6.7540, 107.0140),
            LatLng(-6.7520, 107.0170)
        )

        googleMap?.addPolyline(
            PolylineOptions()
                .addAll(dummyRoute)
                .color(Color.parseColor("#E07856"))
                .width(8f)
        )

        googleMap?.moveCamera(
            CameraUpdateFactory.newLatLngZoom(dummyRoute[2], 14f)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}