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

        // Load nilai awal
        loadSettings()

        // ===== KEAMANAN =====

        binding.switchFingerprint.setOnCheckedChangeListener { _, checked ->
            appSettings.fingerprintEnabled = checked
            showSaved("Login sidik jari ${if (checked) "aktif" else "nonaktif"}")
        }

        binding.switchAutoLock.setOnCheckedChangeListener { _, checked ->
            appSettings.autoLockEnabled = checked
            showSaved("Kunci otomatis ${if (checked) "aktif" else "nonaktif"}")
        }

        // ===== SENSOR & AKURASI =====

        binding.switchGpsHigh.setOnCheckedChangeListener { _, checked ->
            appSettings.gpsHighAccuracy = checked
            showSaved(if (checked) "GPS: Akurasi tinggi" else "GPS: Hemat baterai")
        }

        binding.switchBarometer.setOnCheckedChangeListener { _, checked ->
            appSettings.barometerEnabled = checked
            showSaved("Barometer ${if (checked) "aktif" else "nonaktif"}")
        }

        binding.switchAutoDetect.setOnCheckedChangeListener { _, checked ->
            appSettings.autoDetectActivity = checked
            showSaved("Deteksi aktivitas AI ${if (checked) "aktif" else "nonaktif"}")
        }

        // ===== PENYIMPANAN =====

        binding.switchCloudSync.setOnCheckedChangeListener { _, checked ->
            appSettings.cloudSyncEnabled = checked
            // Jika cloud sync dimatikan, aktifkan offline mode otomatis
            if (!checked) {
                binding.switchOfflineMode.isChecked = true
                appSettings.offlineModeEnabled = true
            }
            showSaved(if (checked) "Sinkronisasi cloud aktif" else "Sinkronisasi cloud nonaktif")
        }

        binding.switchOfflineMode.setOnCheckedChangeListener { _, checked ->
            appSettings.offlineModeEnabled = checked
            showSaved(if (checked) "Mode offline aktif" else "Mode offline nonaktif")
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun loadSettings() {
        binding.switchFingerprint.isChecked  = appSettings.fingerprintEnabled
        binding.switchAutoLock.isChecked     = appSettings.autoLockEnabled
        binding.switchGpsHigh.isChecked      = appSettings.gpsHighAccuracy
        binding.switchBarometer.isChecked    = appSettings.barometerEnabled
        binding.switchAutoDetect.isChecked   = appSettings.autoDetectActivity
        binding.switchCloudSync.isChecked    = appSettings.cloudSyncEnabled
        binding.switchOfflineMode.isChecked  = appSettings.offlineModeEnabled
    }

    private fun showSaved(message: String) {
        Toast.makeText(requireContext(), "✓ $message", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}