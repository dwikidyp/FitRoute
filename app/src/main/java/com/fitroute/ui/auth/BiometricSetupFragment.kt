package com.fitroute.ui.auth

import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fitroute.R
import com.fitroute.databinding.FragmentBiometricSetupBinding
import java.security.KeyPair
import java.security.KeyPairGenerator

class BiometricSetupFragment : Fragment() {

    private var _binding: FragmentBiometricSetupBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBiometricSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnAktifkan.setOnClickListener {
            // Generate RSA key pair di Android Keystore
            val keyPair = generateRsaKeyPair()
            val publicKeyB64 = Base64.encodeToString(
                keyPair.public.encoded,
                Base64.NO_WRAP
            )

            // Kirim public key ke server
            viewModel.enrollBiometric(publicKeyB64)
        }

        // Navigasi ke dashboard setelah enroll berhasil
        viewModel.biometricEnrollResult.observe(viewLifecycleOwner) {
            if (it is Result.Success) {
                findNavController().navigate(R.id.action_biometric_to_dashboard)
            }
        }
    }

    private fun generateRsaKeyPair(): KeyPair {
        val spec = KeyGenParameterSpec.Builder(
            "fitroute_rsa_key",
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        )
            .setKeySize(2048)
            .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
            .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
            .build()

        return KeyPairGenerator
            .getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore")
            .apply { initialize(spec) }
            .generateKeyPair()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
