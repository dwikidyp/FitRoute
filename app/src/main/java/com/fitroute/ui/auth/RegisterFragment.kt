package com.fitroute.ui.auth

import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fitroute.R
import com.fitroute.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Tombol back
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
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

        // Tombol Lanjut/Daftar
        binding.btnDaftar.setOnClickListener {
            val name     = binding.etName.text.toString()
            val email    = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val age      = binding.etAge.text.toString()
            val weight   = binding.etWeight.text.toString()
            val height   = binding.etHeight.text.toString()
            val gender   = binding.spinnerGender.selectedItem.toString()

            // Validasi input
            when {
                name.isBlank() ->
                    Toast.makeText(requireContext(), "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
                email.isBlank() ->
                    Toast.makeText(requireContext(), "Email tidak boleh kosong", Toast.LENGTH_SHORT).show()
                password.length < 8 ->
                    Toast.makeText(requireContext(), "Password minimal 8 karakter", Toast.LENGTH_SHORT).show()
                age.isBlank() ->
                    Toast.makeText(requireContext(), "Usia tidak boleh kosong", Toast.LENGTH_SHORT).show()
                weight.isBlank() ->
                    Toast.makeText(requireContext(), "Berat tidak boleh kosong", Toast.LENGTH_SHORT).show()
                height.isBlank() ->
                    Toast.makeText(requireContext(), "Tinggi tidak boleh kosong", Toast.LENGTH_SHORT).show()
                gender == "Pilih" ->
                    Toast.makeText(requireContext(), "Pilih jenis kelamin", Toast.LENGTH_SHORT).show()
                else -> {
                    val user = UserRequest(
                        name     = name,
                        email    = email,
                        password = password,
                        age      = age.toInt(),
                        weightKg = weight.toFloat(),
                        heightCm = height.toFloat(),
                        gender   = gender,
                        deviceUid = android.provider.Settings.Secure.getString(
                            requireContext().contentResolver,
                            android.provider.Settings.Secure.ANDROID_ID
                        )
                    )
                    viewModel.register(user)
                }
            }
        }

        // Observasi hasil register
        viewModel.registerResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.btnDaftar.alpha = 0.7f
                }
                is Result.Success -> {
                    binding.btnDaftar.alpha = 1f
                    findNavController().navigate(R.id.action_register_to_biometric)
                }
                is Result.Error -> {
                    binding.btnDaftar.alpha = 1f
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}