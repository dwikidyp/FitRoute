package com.fitroute.ui.tracking

import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fitroute.R
import com.fitroute.databinding.FragmentTrackingBinding
import com.fitroute.service.TrackingService
import com.fitroute.util.ActivityType
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TrackingFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentTrackingBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LiveTrackingViewModel by viewModels()

    private var googleMap: GoogleMap? = null
    private val routePoints = mutableListOf<LatLng>()
    private var selectedActivity = ActivityType.RUNNING
    private val weightKg = 70.0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrackingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ambil aktivitas dari arguments
        arguments?.getString("selected_activity")?.let {
            selectedActivity = ActivityType.valueOf(it)
        }

        // Setup Google Maps
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Mulai tracking service
        requireContext().startForegroundService(
            Intent(requireContext(), TrackingService::class.java)
        )

        // Observe stats dari ViewModel
        viewModel.startObserving(weightKg, selectedActivity.name)
        viewModel.liveStats.observe(viewLifecycleOwner) { stats ->
            binding.tvDuration.text  = stats.durationFormatted
            binding.tvDistance.text  = "%.1f".format(stats.distanceKm)
            binding.tvCalories.text  = "%.0f".format(stats.calories)
            binding.tvElevation.text = "+%.0f".format(stats.elevationGainM)

            val paceMin = stats.pace.toInt()
            val paceSec = ((stats.pace - paceMin) * 60).toInt()
            binding.tvPace.text = "%d:%02d".format(paceMin, paceSec)
        }

        // Update peta dan kecepatan tiap detik
        lifecycleScope.launch {
            while (TrackingService.isRunning) {
                TrackingService.latestLocation?.let { point ->
                    updateMap(point)
                }

                // Hitung kecepatan
                val distKm   = TrackingService.totalDistanceKm
                val durHours = TrackingService.durationSeconds / 3600.0
                val speed    = if (durHours > 0) distKm / durHours else 0.0
                binding.tvSpeed.text = "%.1f".format(speed)

                delay(1000)
            }
        }

        // Tombol Pause/Resume
        viewModel.isPaused.observe(viewLifecycleOwner) { paused ->
            binding.tvPauseIcon.text = if (paused) "▶" else "⏸"
        }
        binding.btnPause.setOnClickListener {
            if (viewModel.isPaused.value == true) viewModel.resumeSession()
            else viewModel.pauseSession()
        }

        // Tombol Stop
        binding.btnStop.setOnClickListener {
            viewModel.stopSession()
        }
        viewModel.navigateToSummary.observe(viewLifecycleOwner) { navigate ->
            if (navigate) {
                requireContext().stopService(
                    Intent(requireContext(), TrackingService::class.java)
                )
                findNavController().popBackStack(R.id.dashboardFragment, false)
            }
        }

        // Zoom In/Out
        binding.btnZoomIn.setOnClickListener {
            googleMap?.animateCamera(CameraUpdateFactory.zoomIn())
        }
        binding.btnZoomOut.setOnClickListener {
            googleMap?.animateCamera(CameraUpdateFactory.zoomOut())
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.apply {
            uiSettings.isZoomControlsEnabled   = false
            uiSettings.isMyLocationButtonEnabled = false
            try { isMyLocationEnabled = true } catch (e: SecurityException) { }
            mapType = GoogleMap.MAP_TYPE_NORMAL
        }
    }

    private fun updateMap(newPoint: LatLng) {
        if (routePoints.isEmpty() ||
            routePoints.last() != newPoint) {

            routePoints.add(newPoint)

            // Gambar polyline rute
            if (routePoints.size >= 2) {
                googleMap?.addPolyline(
                    PolylineOptions()
                        .addAll(routePoints)
                        .color(Color.parseColor("#2D6A4F"))
                        .width(10f)
                )
            }

            // Gerakkan kamera ke lokasi terbaru
            googleMap?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(newPoint, 17f)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}