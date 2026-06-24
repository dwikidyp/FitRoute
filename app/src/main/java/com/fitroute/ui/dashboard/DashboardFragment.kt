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


        if (!PermissionHelper.hasFineLocationPermission(requireActivity())) {
            locationLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACTIVITY_RECOGNITION
                )
            )
        }

        // Tombol logout
        binding.btnLogout.setOnClickListener {
            viewModel.logout()
        }

        // Observasi state logout
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
}