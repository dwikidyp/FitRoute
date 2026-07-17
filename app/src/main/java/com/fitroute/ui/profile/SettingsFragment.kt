package com.fitroute.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.fitroute.databinding.FragmentSettingsBinding
import com.fitroute.util.AppSettings
import com.google.android.gms.location.Priority

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var appSettings: AppSettings

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appSettings = AppSettings(requireContext())

        // Load nilai awal dari AppSettings
        loadSettings()

        // ===== LISTENER SWITCH =====

        // Barometer
        binding.switchBarometer.setOnCheckedChangeListener { _, checked ->
            appSettings.barometerEnabled = checked
            showSaved("Barometer ${if (checked) "aktif" else "nonaktif"}")
        }

        // GPS akurasi tinggi
        binding.switchGpsHigh.setOnCheckedChangeListener { _, checked ->
            appSettings.gpsHighAccuracy = checked
            // Ubah mode GPS di TrackingService jika sedang berjalan
            val priority = if (checked)
                Priority.PRIORITY_HIGH_ACCURACY
            else
                Priority.PRIORITY_BALANCED_POWER_ACCURACY
            showSaved("GPS: ${if (checked) "Akurasi tinggi" else "Hemat baterai"}")
        }

        // Deteksi aktivitas otomatis
        binding.switchAutoDetect.setOnCheckedChangeListener { _, checked ->
            appSettings.autoDetectActivity = checked
            showSaved("Deteksi aktivitas ${if (checked) "aktif" else "nonaktif"}")
        }

        // Login sidik jari
        binding.switchFingerprint.setOnCheckedChangeListener { _, checked ->
            appSettings.fingerprintEnabled = checked
            showSaved("Login sidik jari ${if (checked) "aktif" else "nonaktif"}")
        }

        // Notifikasi PR
        binding.switchNotifyPR.setOnCheckedChangeListener { _, checked ->
            appSettings.notifyPersonalRecord = checked
        }

        // Notifikasi target mingguan
        binding.switchNotifyWeekly.setOnCheckedChangeListener { _, checked ->
            appSettings.notifyWeeklyGoal = checked
        }

        // Target mingguan — simpan saat focus hilang
        binding.etWeeklyTarget.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val value = binding.etWeeklyTarget.text.toString().toFloatOrNull()
                if (value != null && value > 0) {
                    appSettings.weeklyDistanceTarget = value
                    showSaved("Target: ${value.toInt()} km/minggu")
                }
            }
        }

        // Tombol back
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun loadSettings() {
        binding.switchBarometer.isChecked    = appSettings.barometerEnabled
        binding.switchGpsHigh.isChecked      = appSettings.gpsHighAccuracy
        binding.switchAutoDetect.isChecked   = appSettings.autoDetectActivity
        binding.switchFingerprint.isChecked  = appSettings.fingerprintEnabled
        binding.switchNotifyPR.isChecked     = appSettings.notifyPersonalRecord
        binding.switchNotifyWeekly.isChecked = appSettings.notifyWeeklyGoal
        binding.etWeeklyTarget.setText(
            appSettings.weeklyDistanceTarget.toInt().toString()
        )
    }

    private fun showSaved(message: String) {
        Toast.makeText(requireContext(), "✓ $message", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}