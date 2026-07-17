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

        viewModel.createDummyProfile()

        lifecycleScope.launch {
            viewModel.profileState.collect { state ->
                if (state.isLoading) return@collect

                state.user?.let { user ->

                    // Avatar inisial
                    val initials = user.fullName
                        .split(" ").take(2)
                        .joinToString("") { it.first().uppercase() }
                    binding.tvAvatar.text = initials

                    // Header info
                    binding.tvName.text  = user.fullName
                    binding.tvEmail.text = user.email

                    // Stats header
                    binding.tvWeight.text = "${user.weightKg.toInt()}kg"

                    // Info pribadi
                    binding.tvAge.text          = "${user.age} tahun"
                    binding.tvGender.text       = user.gender
                    binding.tvHeight.text       = "${user.heightCm.toInt()} cm"
                    binding.tvWeightDetail.text = "${user.weightKg.toInt()} kg"
                    binding.tvActivityPref.text = when (user.activityPref) {
                        "RUNNING" -> "Lari"
                        "CYCLING" -> "Sepeda"
                        "HIKING"  -> "Hiking"
                        else      -> "Lari"
                    }

                    // BMI
                    binding.tvBmi.text         = "%.1f".format(state.bmi)
                    binding.tvBmiCategory.text = state.bmiCategory.lowercase()
                    binding.tvBmiCategory.setTextColor(
                        android.graphics.Color.parseColor(
                            when (state.bmiCategory) {
                                "Normal"   -> "#52B788"
                                "Kurang"   -> "#FF9800"
                                "Lebih"    -> "#FF9800"
                                "Obesitas" -> "#F44336"
                                else       -> "#52B788"
                            }
                        )
                    )
                }
            }
        }

        // Tombol Edit
        binding.btnEdit.setOnClickListener {
            // TODO: navigate ke EditProfileFragment
            Toast.makeText(requireContext(),
                "Fitur edit akan segera hadir", Toast.LENGTH_SHORT).show()
        }

        // Tombol Logout
        binding.btnLogout.setOnClickListener {
            authViewModel.logout()
        }

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