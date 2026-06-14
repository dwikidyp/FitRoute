package com.fitroute.ui.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fitroute.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    private val viewModel: AuthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Cek token & init sensor (1.5 detik)
        lifecycleScope.launch {
            delay(1500)
            viewModel.checkSession()
        }

        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                AuthState.LoggedIn ->
                    findNavController().navigate(R.id.action_splash_to_dashboard)
                AuthState.LoggedOut ->
                    findNavController().navigate(R.id.action_splash_to_login)
            }
        }
    }
}