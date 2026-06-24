package com.fitroute.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fitroute.R
import com.fitroute.databinding.FragmentDashboardBinding
import com.fitroute.service.TrackingService
import com.fitroute.ui.auth.AuthState
import com.fitroute.ui.auth.AuthViewModel
import com.fitroute.util.PermissionHelper
import com.google.android.gms.maps.model.LatLng

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Cek dan minta izin lokasi saat fragment dibuka
        if (!PermissionHelper.hasFineLocationPermission(requireActivity())) {
            PermissionHelper.requestLocationPermissions(
                activity = requireActivity() as AppCompatActivity,
                onGranted = {
                    // Izin diberikan → mulai tracking
                    Toast.makeText(requireContext(), "Izin lokasi diberikan!", Toast.LENGTH_SHORT).show()
                },
                onDenied = {
                    // Izin ditolak → tampilkan pesan
                    Toast.makeText(requireContext(), "Izin lokasi diperlukan untuk tracking", Toast.LENGTH_LONG).show()
                }
            )
        }

        // Tombol logout
        binding.btnLogout.setOnClickListener {
            viewModel.logout()
        }

        // Observasi state → jika LoggedOut, kembali ke Login
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            if (state is AuthState.LoggedOut) {
                findNavController().navigate(R.id.action_dashboard_to_login)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ▶ Mulai tracking
    private fun startTracking() {
        val intent = Intent(requireContext(), TrackingService::class.java)
        requireContext().startForegroundService(intent)
    }

    // ⏹ Hentikan tracking
    private fun stopTracking() {
        val intent = Intent(requireContext(), TrackingService::class.java)
        requireContext().stopService(intent)
    }

    // Cek apakah sedang tracking
    private fun isTracking(): Boolean {
        return TrackingService.isRunning
    }

    // Ambil lokasi terbaru
    private fun getLatestLocation(): LatLng? {
        return TrackingService.latestLocation
    }
}
