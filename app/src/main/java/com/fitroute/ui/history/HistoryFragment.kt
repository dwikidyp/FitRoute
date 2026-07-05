package com.fitroute.ui.history

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitroute.R
import com.fitroute.databinding.FragmentHistoryBinding
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HistoryViewModel by viewModels()
    private lateinit var adapter: HistoryAdapter
    private var activeFilter = ActivityFilter.ALL

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFilterChips()
        observeHistory()
    }

    private fun setupRecyclerView() {
        adapter = HistoryAdapter { sessionId ->
            // TODO: navigate ke detail sesi
            // findNavController().navigate(...)
        }
        binding.recyclerHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HistoryFragment.adapter
        }
    }

    private fun setupFilterChips() {
        ActivityFilter.values().forEach { filter ->
            val chip = TextView(requireContext()).apply {
                text = filter.label
                textSize = 13f
                setPadding(32, 16, 32, 16)

                // Style chip
                setBackgroundResource(
                    if (filter == activeFilter) R.drawable.bg_activity_card_selected
                    else R.drawable.bg_activity_card
                )
                setTextColor(
                    if (filter == activeFilter)
                        Color.parseColor("#1B4332")
                    else Color.parseColor("#888888")
                )

                val params = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { marginEnd = 8.dpToPx() }
                layoutParams = params

                setOnClickListener {
                    activeFilter = filter
                    viewModel.onFilterSelected(filter)
                    refreshFilterChips()
                }
            }
            binding.filterContainer.addView(chip)
        }
    }

    private fun refreshFilterChips() {
        val filters = ActivityFilter.values()
        filters.forEachIndexed { index, filter ->
            val chip = binding.filterContainer.getChildAt(index) as? TextView ?: return
            val isActive = filter == activeFilter

            chip.setBackgroundResource(
                if (isActive) R.drawable.bg_activity_card_selected
                else R.drawable.bg_activity_card
            )
            chip.setTextColor(
                if (isActive) Color.parseColor("#1B4332")
                else Color.parseColor("#888888")
            )
        }
    }

    private fun observeHistory() {
        lifecycleScope.launch {
            viewModel.historyList.collect { sessions ->
                adapter.submitList(sessions)
            }
        }
    }

    private fun Int.dpToPx(): Int =
        (this * resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}