package com.fitroute.ui.tracking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.fitroute.R
import com.fitroute.databinding.FragmentElevationProfileBinding
import com.fitroute.databinding.ItemElevationSegmentBinding

class ElevationProfileFragment : Fragment() {

    private var _binding: FragmentElevationProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentElevationProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Contoh data dummy
        val barometerData = listOf(100f, 110f, 130f, 150f, 180f, 200f, 190f, 170f, 160f, 148f)
        val gpsData = listOf(98f, 112f, 128f, 148f, 178f, 198f, 192f, 168f, 158f, 150f)

        binding.elevationChart.setData(barometerData, gpsData)

        binding.tvElevGain.text = "+148m"
        binding.tvElevLoss.text = "-62m"
        binding.tvElevMax.text  = "342m"

        // Data segmen per km
        val segments = listOf(
            Triple("km 1", 22, 0.6f),
            Triple("km 2", 58, 1.0f),
            Triple("km 3", 40, 0.7f),
            Triple("km 4", 18, 0.3f),
            Triple("km 5", 10, 0.2f)
        )

        // Tambahkan item segmen secara dinamis
        segments.forEach { (label, value, progress) ->
            val itemBinding = ItemElevationSegmentBinding.inflate(
                layoutInflater, binding.segmentContainer, false
            )
            itemBinding.tvSegmentLabel.text = label
            itemBinding.tvSegmentValue.text = "+${value}m"

            // Atur lebar bar sesuai progress
            itemBinding.root.post {
                val parentWidth = itemBinding.tvSegmentLabel.parent.let {
                    (it as ViewGroup).width
                }
                val maxBarWidth = parentWidth - 100
                val params = itemBinding.viewSegmentBar.layoutParams
                params.width = (maxBarWidth * progress).toInt()
                itemBinding.viewSegmentBar.layoutParams = params
            }

            binding.segmentContainer.addView(itemBinding.root)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}