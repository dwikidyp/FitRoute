package com.fitroute.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fitroute.databinding.ItemSessionHistoryBinding
import com.fitroute.databinding.ItemWeekHeaderBinding

// Sealed class untuk 2 jenis item di list
sealed class HistoryListItem {
    data class Header(val weekLabel: String) : HistoryListItem()
    data class Session(val model: SessionUiModel) : HistoryListItem()
}

class HistoryAdapter(
    private val onItemClick: (sessionId: String) -> Unit
) : ListAdapter<HistoryListItem, RecyclerView.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<HistoryListItem>() {
        override fun areItemsTheSame(a: HistoryListItem, b: HistoryListItem) =
            when {
                a is HistoryListItem.Header && b is HistoryListItem.Header ->
                    a.weekLabel == b.weekLabel
                a is HistoryListItem.Session && b is HistoryListItem.Session ->
                    a.model.id == b.model.id
                else -> false
            }
        override fun areContentsTheSame(a: HistoryListItem, b: HistoryListItem) =
            a == b
    }

    // ViewHolder untuk header minggu
    inner class HeaderViewHolder(
        private val binding: ItemWeekHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HistoryListItem.Header) {
            binding.tvWeekLabel.text = item.weekLabel
        }
    }

    // ViewHolder untuk item sesi
    inner class SessionViewHolder(
        private val binding: ItemSessionHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SessionUiModel) {
            binding.tvEmoji.text = item.activityEmoji

            // Nama aktivitas
            val activityName = when (item.activityType) {
                "RUNNING" -> "Lari pagi"
                "CYCLING" -> "Sepeda pagi"
                "HIKING"  -> "Hiking"
                else      -> item.activityType
            }
            binding.tvActivityName.text = activityName
            binding.tvDateDuration.text = "${item.dateFormatted} · ${item.durationFormatted}"
            binding.tvCalories.text     = item.caloriesKcal

            // Stats berbeda per aktivitas
            when (item.activityType) {
                "HIKING" -> {
                    // Hiking: tampilkan elevasi
                    binding.tvMainStat.text      = item.distanceKm.replace(" km", "")
                    binding.tvMainStatLabel.text  = "km"
                    binding.tvSecondStat.text    = item.elevGainM
                    binding.tvSecondStatLabel.text = "elevasi"
                }
                "CYCLING" -> {
                    // Sepeda: tampilkan kecepatan avg
                    binding.tvMainStat.text      = item.distanceKm.replace(" km", "")
                    binding.tvMainStatLabel.text  = "km"
                    binding.tvSecondStat.text    = "17.0"
                    binding.tvSecondStatLabel.text = "km/h avg"
                }
                else -> {
                    // Lari: tampilkan pace
                    binding.tvMainStat.text      = item.distanceKm.replace(" km", "")
                    binding.tvMainStatLabel.text  = "km"
                    binding.tvSecondStat.text    = item.durationFormatted
                    binding.tvSecondStatLabel.text = "min/km"
                }
            }

            binding.root.setOnClickListener { onItemClick(item.id) }
        }
    }

    override fun getItemViewType(position: Int) =
        when (getItem(position)) {
            is HistoryListItem.Header  -> 0
            is HistoryListItem.Session -> 1
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        when (viewType) {
            0 -> HeaderViewHolder(
                ItemWeekHeaderBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            else -> SessionViewHolder(
                ItemSessionHistoryBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is HistoryListItem.Header  -> (holder as HeaderViewHolder).bind(item)
            is HistoryListItem.Session -> (holder as SessionViewHolder).bind(item.model)
        }
    }
}