package com.fitroute.ui.notification

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fitroute.data.local.NotificationEntity
import com.fitroute.databinding.ItemNotificationBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationAdapter(
    private val onItemClick: (NotificationEntity) -> Unit
) : ListAdapter<NotificationEntity, NotificationAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<NotificationEntity>() {
        override fun areItemsTheSame(a: NotificationEntity, b: NotificationEntity) =
            a.id == b.id
        override fun areContentsTheSame(a: NotificationEntity, b: NotificationEntity) =
            a == b
    }

    inner class ViewHolder(
        private val binding: ItemNotificationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: NotificationEntity) {

            // Icon berdasarkan tipe
            binding.tvNotifIcon.text = when (item.type) {
                "PR"     -> "🏆"
                "STREAK" -> "🔥"
                "TARGET" -> "🎯"
                "SYNC"   -> "☁"
                "REPORT" -> "📊"
                else     -> "🔔"
            }

            binding.tvNotifTitle.text = item.title
            binding.tvNotifBody.text  = item.body
            binding.tvNotifTime.text  = getRelativeTime(item.createdAt)

            // Tampilkan dot jika belum dibaca
            binding.dotUnread.visibility =
                if (item.isRead) View.GONE else View.VISIBLE

            // Background berbeda jika belum dibaca
            binding.notifContainer.setBackgroundColor(
                if (item.isRead) Color.WHITE
                else Color.parseColor("#F5FFF8")
            )

            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    private fun getRelativeTime(timestamp: Long): String {
        val diff   = System.currentTimeMillis() - timestamp
        val menit  = diff / 60_000
        val jam    = menit / 60
        val hari   = jam / 24

        return when {
            menit < 1  -> "Baru saja"
            menit < 60 -> "$menit menit lalu"
            jam < 24   -> "$jam jam lalu"
            hari < 7   -> "$hari hari lalu"
            else -> SimpleDateFormat("dd MMM yyyy", Locale("id"))
                .format(Date(timestamp))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            ItemNotificationBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))
}