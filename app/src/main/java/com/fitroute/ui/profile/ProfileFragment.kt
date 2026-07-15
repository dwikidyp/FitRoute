package com.fitroute.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fitroute.R
import com.fitroute.databinding.FragmentProfileBinding
import com.fitroute.ui.auth.AuthState
import com.fitroute.ui.auth.AuthViewModel
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Buat profil dummy jika belum ada
        viewModel.createDummyProfile()

        // Observe profil
        lifecycleScope.launch {
            viewModel.profileState.collect { state ->
                if (state.isLoading) return@collect

                state.user?.let { user ->

                    // Inisial avatar dari nama
                    val initials = user.fullName
                        .split(" ")
                        .take(2)
                        .joinToString("") { it.first().uppercase() }
                    binding.tvAvatar.text = initials

                    // Info header
                    binding.tvName.text  = user.fullName
                    binding.tvEmail.text = user.email

                    // BMI
                    binding.tvBmi.text         = "%.1f".format(state.bmi)
                    binding.tvBmiCategory.text = state.bmiCategory
                    binding.tvBmiCategory.setTextColor(
                        android.graphics.Color.parseColor(
                            when (state.bmiCategory) {
                                "Normal"   -> "#2D6A4F"
                                "Kurang"   -> "#FF9800"
                                "Lebih"    -> "#FF9800"
                                "Obesitas" -> "#F44336"
                                else       -> "#2D6A4F"
                            }
                        )
                    )

                    // Aktivitas favorit
                    binding.tvActivityPref.text = when (user.activityPref) {
                        "RUNNING" -> "🏃"
                        "CYCLING" -> "🚴"
                        "HIKING"  -> "🥾"
                        else      -> "🏃"
                    }

                    // Data fisik
                    binding.tvAge.text    = "${user.age} tahun"
                    binding.tvGender.text = user.gender
                    binding.etWeight.setText(user.weightKg.toInt().toString())
                    binding.etHeight.setText(user.heightCm.toInt().toString())

                    // Tombol Simpan
                    binding.btnSaveProfile.setOnClickListener {
                        val weightStr = binding.etWeight.text.toString()
                        val heightStr = binding.etHeight.text.toString()

                        if (weightStr.isBlank() || heightStr.isBlank()) {
                            Toast.makeText(requireContext(),
                                "Berat dan tinggi tidak boleh kosong",
                                Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        val updated = user.copy(
                            weightKg = weightStr.toFloat(),
                            heightCm = heightStr.toFloat()
                        )
                        viewModel.saveProfile(updated)
                        Toast.makeText(requireContext(),
                            "Profil disimpan ✓",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Tombol Logout
        binding.btnLogout.setOnClickListener {
            authViewModel.logout()
        }

        // Observe logout
        authViewModel.authState.observe(viewLifecycleOwner) { state ->
            if (state is AuthState.LoggedOut) {
                findNavController().navigate(
                    R.id.action_profileFragment_to_loginFragment
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}