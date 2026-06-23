package com.fitroute.ui.auth

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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout splash
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvStatus = view.findViewById<TextView>(R.id.tvLoadingStatus)

        // Animasi teks loading bertahap
        val statusMessages = listOf(
            "Memuat pengaturan sensor...",
            "Menginisialisasi GPS...",
            "Memeriksa sesi login..."
        )

        lifecycleScope.launch {
            statusMessages.forEach { msg ->
                tvStatus.text = msg
                delay(500)
            }
            viewModel.checkSession()
        }

        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.LoggedIn ->
                    findNavController().navigate(R.id.action_splash_to_dashboard)
                is AuthState.LoggedOut ->
                    findNavController().navigate(R.id.action_splash_to_login)
            }
        }
    }
}