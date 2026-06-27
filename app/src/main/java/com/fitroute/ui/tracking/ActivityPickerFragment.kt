package com.fitroute.ui.tracking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.fitroute.R
import com.fitroute.databinding.FragmentActivityPickerBinding
import com.fitroute.util.ActivityType

class ActivityPickerFragment : Fragment() {

    private var _binding: FragmentActivityPickerBinding? = null
    private val binding get() = _binding!!

    // Aktivitas yang dipilih
    private var selectedActivity = ActivityType.RUNNING

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActivityPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set tampilan awal
        updateSelection(ActivityType.RUNNING)

        // Klik card Lari
        binding.cardRunning.setOnClickListener {
            updateSelection(ActivityType.RUNNING)
        }

        // Klik card Bersepeda
        binding.cardCycling.setOnClickListener {
            updateSelection(ActivityType.CYCLING)
        }

        // Klik card Hiking
        binding.cardHiking.setOnClickListener {
            updateSelection(ActivityType.HIKING)
        }

        // Tombol back
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Tombol Mulai Sekarang
        binding.btnMulaiSekarang.setOnClickListener {
            // Kirim aktivitas yang dipilih ke tracking fragment
            val bundle = Bundle().apply {
                putString("selected_activity", selectedActivity.name)
            }
            findNavController().navigate(
                R.id.action_activityPicker_to_tracking,
                bundle
            )
        }
    }

    private fun updateSelection(activity: ActivityType) {
        selectedActivity = activity

        // Reset semua card ke state normal
        binding.cardRunning.setBackgroundResource(R.drawable.bg_activity_card)
        binding.cardCycling.setBackgroundResource(R.drawable.bg_activity_card)
        binding.cardHiking.setBackgroundResource(R.drawable.bg_activity_card)

        // Sembunyikan semua ceklis
        binding.checkRunning.visibility = View.INVISIBLE

        // Highlight card yang dipilih
        when (activity) {
            ActivityType.RUNNING -> {
                binding.cardRunning.setBackgroundResource(R.drawable.bg_activity_card_selected)
                binding.checkRunning.visibility = View.VISIBLE
            }
            ActivityType.CYCLING -> {
                binding.cardCycling.setBackgroundResource(R.drawable.bg_activity_card_selected)
            }
            ActivityType.HIKING -> {
                binding.cardHiking.setBackgroundResource(R.drawable.bg_activity_card_selected)
            }
            else -> {}
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}