package com.fitroute.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fitroute.R
import com.fitroute.databinding.FragmentDashboardBinding
import com.fitroute.service.TrackingService
import com.fitroute.ui.auth.AuthState
import com.fitroute.ui.auth.AuthViewModel
import com.fitroute.util.PermissionHelper
import android.Manifest

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()


    private lateinit var locationLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        locationLauncher = PermissionHelper.registerLocationLauncher(
            fragment = this,
            onGranted = {
                Toast.makeText(requireContext(),
                    "Izin lokasi diberikan!", Toast.LENGTH_SHORT).show()
                // TODO: mulai tracking di sini
            },
            onDenied = {
                Toast.makeText(requireContext(),
                    "Izin lokasi diperlukan untuk tracking",
                    Toast.LENGTH_LONG).show()
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewProgressFill.post {
            val parentWidth = (binding.viewProgressFill.parent as View).width
            val params = binding.viewProgressFill.layoutParams
            params.width = (parentWidth * 0.68).toInt()
            binding.viewProgressFill.layoutParams = params
        }

        // Tap icon notifikasi di header dashboard
        binding.ivProfile.setOnClickListener {
            findNavController().navigate(
                R.id.action_dashboard_to_notifications
            )
        }

        // Tombol mulai sesi
        binding.btnStartSession.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_activityPicker)
            Toast.makeText(requireContext(), "Memulai sesi...", Toast.LENGTH_SHORT).show()
        }

        // Bottom navigation
        binding.navHome.setOnClickListener {  }
        binding.navRoute.setOnClickListener {
            // TODO: navigate ke rute
        }
        binding.navHistory.setOnClickListener {
            // TODO: navigate ke riwayat
        }
        binding.navProfile.setOnClickListener {
            // TODO: navigate ke profil
        }


        if (!PermissionHelper.hasFineLocationPermission(requireActivity())) {
            locationLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACTIVITY_RECOGNITION
                )
            )
        }



        
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}