package com.fitroute.ui.notification

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fitroute.R
import com.fitroute.data.local.NotificationEntity
import com.fitroute.databinding.ItemDateHeaderBinding
import com.fitroute.databinding.ItemNotificationBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Sealed class untuk 2 jenis item
sealed class NotifListItem {
    data class Header(val label: String) : NotifListItem()
    data class Item(val entity: NotificationEntity) : NotifListItem()
}

class NotificationAdapter(
    private val onItemClick: (NotificationEntity) -> Unit
) : ListAdapter<NotifListItem, RecyclerView.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<NotifListItem>() {
        override fun areItemsTheSame(a: NotifListItem, b: NotifListItem) = when {
            a is NotifListItem.Header && b is NotifListItem.Header ->
                a.label == b.label
            a is NotifListItem.Item && b is NotifListItem.Item ->
                a.entity.id == b.entity.id
            else -> false
        }
        override fun areContentsTheSame(a: NotifListItem, b: NotifListItem) = a == b
    }

    inner class HeaderViewHolder(
        private val binding: ItemDateHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: NotifListItem.Header) {
            binding.tvDateHeader.text = item.label
        }
    }

    inner class ItemViewHolder(
        private val binding: ItemNotificationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(entity: NotificationEntity) {

            // Icon & warna background berdasarkan tipe
            val (emoji, bgRes) = when (entity.type) {
                "PR"     -> Pair("🏆", R.drawable.bg_notif_icon_pr)
                "TARGET" -> Pair("🎯", R.drawable.bg_notif_icon_target)
                "STREAK" -> Pair("🔥", R.drawable.bg_notif_icon_streak)
                "SYNC"   -> Pair("☁",  R.drawable.bg_notif_icon_sync)
                "REPORT" -> Pair("📊", R.drawable.bg_notif_icon_report)
                else     -> Pair("🔔", R.drawable.bg_activity_icon)
            }

            binding.tvNotifIcon.text = emoji
            binding.iconContainer.setBackgroundResource(bgRes)

            binding.tvNotifTitle.text = entity.title
            binding.tvNotifBody.text  = entity.body
            binding.tvNotifTime.text  = getRelativeTime(entity.createdAt)

            // Dot & background berdasarkan status baca
            binding.dotUnread.visibility =
                if (entity.isRead) View.INVISIBLE else View.VISIBLE

            binding.notifContainer.setBackgroundColor(
                if (entity.isRead) Color.parseColor("#F8F8F8")
                else Color.WHITE
            )

            // Judul lebih terang jika sudah dibaca
            binding.tvNotifTitle.setTextColor(
                Color.parseColor(if (entity.isRead) "#666666" else "#1B4332")
            )

            binding.root.setOnClickListener { onItemClick(entity) }
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is NotifListItem.Header -> 0
        is NotifListItem.Item   -> 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        when (viewType) {
            0 -> HeaderViewHolder(
                ItemDateHeaderBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            else -> ItemViewHolder(
                ItemNotificationBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is NotifListItem.Header -> (holder as HeaderViewHolder).bind(item)
            is NotifListItem.Item   -> (holder as ItemViewHolder).bind(item.entity)
        }
    }

    private fun getRelativeTime(timestamp: Long): String {
        val diff  = System.currentTimeMillis() - timestamp
        val menit = diff / 60_000
        val jam   = menit / 60
        val hari  = jam / 24

        return when {
            menit < 1  -> "Baru saja"
            menit < 60 -> "$menit menit lalu"
            jam   < 24 -> "$jam jam lalu"
            hari  == 1L -> "Kemarin"
            hari  <  7 -> "$hari hari lalu"
            else -> SimpleDateFormat("dd MMM yyyy", Locale("id"))
                .format(Date(timestamp))
        }
    }
}