package com.fitroute.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fitroute.databinding.ItemSessionHistoryBinding

class HistoryAdapter(
    private val onItemClick: (sessionId: String) -> Unit
) : ListAdapter<SessionUiModel, HistoryAdapter.SessionViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<SessionUiModel>() {
        override fun areItemsTheSame(a: SessionUiModel, b: SessionUiModel) =
            a.id == b.id
        override fun areContentsTheSame(a: SessionUiModel, b: SessionUiModel) =
            a == b
    }

    inner class SessionViewHolder(
        private val binding: ItemSessionHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SessionUiModel) {
            binding.tvEmoji.text        = item.activityEmoji
            binding.tvDate.text         = item.dateFormatted
            binding.tvDistance.text     = item.distanceKm
            binding.tvDuration.text     = item.durationFormatted
            binding.tvCalories.text     = item.caloriesKcal
            binding.tvElevation.text    = item.elevGainM

            binding.root.setOnClickListener {
                onItemClick(item.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val binding = ItemSessionHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SessionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}