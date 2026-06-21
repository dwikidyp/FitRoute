package com.fitroute.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fitroute.R
import com.fitroute.databinding.FragmentDashboardBinding
import com.fitroute.ui.auth.AuthState
import com.fitroute.ui.auth.AuthViewModel

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
}