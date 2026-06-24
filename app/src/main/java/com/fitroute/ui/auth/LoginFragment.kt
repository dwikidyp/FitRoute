package com.fitroute.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fitroute.R
import com.fitroute.databinding.FragmentLoginBinding
import com.google.android.material.snackbar.Snackbar

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Tombol Masuk
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val pass = binding.etPassword.text.toString()

            if (email.isBlank() || pass.isBlank()) {
                Toast.makeText(requireContext(),
                    "Email dan password tidak boleh kosong",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.loginWithEmail(email, pass)
        }

        // Tombol Sidik Jari
        binding.btnBiometric.setOnClickListener {
            showBiometricPrompt()
        }

        // Link ke Register
        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        // Toggle show/hide password
        binding.ivTogglePassword.setOnClickListener {
            val et = binding.etPassword
            if (et.inputType == android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                et.inputType = android.text.InputType.TYPE_CLASS_TEXT or
                        android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            } else {
                et.inputType = android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            }
            et.setSelection(et.text.length)
        }

        // Observasi hasil login
        observeLoginResult()
    }

    private fun showBiometricPrompt() {
        val prompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(requireContext()),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    viewModel.loginWithBiometric()
                }

                override fun onAuthenticationError(
                    errorCode: Int, errString: CharSequence
                ) {
                    showError(errString.toString())
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Masuk ke FitRoute")
            .setSubtitle("Gunakan sidik jari")
            .setNegativeButtonText("Batalkan")
            .build()

        prompt.authenticate(promptInfo)
    }

    // 4b. Observasi hasil login dari ViewModel
    private fun observeLoginResult() {
        viewModel.loginResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Success -> {
                    saveTokenSecurely(result.data.token)
                    findNavController().navigate(R.id.action_login_to_dashboard)
                }
                is Result.Error -> {
                    showSnackbar(result.message)
                }
                is Result.Loading -> {
                    binding.btnLogin.isEnabled = false
                    binding.progressBar.isVisible = true
                }
            }
        }
    }

    private fun saveTokenSecurely(token: String) {
        viewModel.saveToken(token)
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
