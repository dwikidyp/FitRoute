package com.fitroute.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fitroute.R
import com.fitroute.databinding.FragmentAboutBinding
import com.fitroute.ui.auth.AuthState
import com.fitroute.ui.auth.AuthViewModel

class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Tombol back
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Kebijakan Privasi
        binding.menuPrivacy.setOnClickListener {
            openUrl("https://fitroute.app/privacy")
        }

        // Syarat & Ketentuan
        binding.menuTerms.setOnClickListener {
            openUrl("https://fitroute.app/terms")
        }

        // Beri Ulasan
        binding.menuReview.setOnClickListener {
            openUrl("https://play.google.com/store/apps/details?id=com.fitroute")
        }

        // Keluar dari akun
        binding.btnLogout.setOnClickListener {
            authViewModel.logout()
        }

        // Observe logout
        authViewModel.authState.observe(viewLifecycleOwner) { state ->
            if (state is AuthState.LoggedOut) {
                findNavController().navigate(
                    R.id.action_aboutFragment_to_loginFragment
                )
            }
        }
    }

    private fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            Toast.makeText(requireContext(),
                "Tidak bisa membuka halaman", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}