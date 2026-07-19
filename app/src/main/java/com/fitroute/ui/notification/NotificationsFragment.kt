package com.fitroute.ui.notification

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitroute.data.local.AppDatabase
import com.fitroute.data.local.NotificationEntity
import com.fitroute.data.repository.NotificationRepository
import com.fitroute.databinding.FragmentNotificationsBinding
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ViewModel inline untuk kesederhanaan
class NotificationsViewModel(app: Application) : AndroidViewModel(app) {

    private val db   = AppDatabase.getInstance(app)
    private val repo = NotificationRepository(db.notificationDao())
    private val userId = "user_123"

    val notifications = repo.getAll(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val unreadCount = repo.getUnreadCount(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)

    fun markAsRead(id: String) {
        viewModelScope.launch { repo.markAsRead(id) }
    }

    fun markAllAsRead() {
        viewModelScope.launch { repo.markAllAsRead(userId) }
    }

    // Tambah notifikasi dummy untuk testing
    fun addDummyNotifications() {
        viewModelScope.launch {
            val dummies = listOf(
                Triple("PR",     "Personal Record Baru! 🏆",
                    "Jarak terpanjang: 18.4 km · Sepeda"),
                Triple("TARGET", "Target Mingguan Tercapai! 🎯",
                    "Kamu sudah berlari 50 km minggu ini"),
                Triple("STREAK", "Streak 7 Hari! 🔥",
                    "Kamu aktif berolahraga selama 7 hari berturut"),
                Triple("SYNC",   "Sinkronisasi Selesai ☁",
                    "5 sesi berhasil disimpan ke cloud"),
                Triple("REPORT", "Laporan Mingguan Tersedia 📊",
                    "Lihat ringkasan performa minggu ini")
            )
            dummies.forEach { (type, title, body) ->
                repo.create(userId, type, title, body)
            }
        }
    }
}

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NotificationsViewModel by viewModels()
    private lateinit var adapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.addDummyNotifications()
        setupRecyclerView()
        observeData()

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnMarkAll.setOnClickListener {
            viewModel.markAllAsRead()
        }
    }

    private fun setupRecyclerView() {
        adapter = NotificationAdapter { notif ->
            viewModel.markAsRead(notif.id)
        }
        binding.recyclerNotifications.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@NotificationsFragment.adapter
        }
    }

    private fun observeData() {
        lifecycleScope.launch {
            viewModel.notifications.collect { list ->
                // Tambahkan header tanggal
                val grouped = buildGroupedList(list)
                adapter.submitList(grouped)

                binding.recyclerNotifications.visibility =
                    if (list.isEmpty()) View.GONE else View.VISIBLE
                binding.emptyState.visibility =
                    if (list.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            viewModel.unreadCount.collect { count ->
                binding.tvUnreadCount.text = count.toString()
            }
        }
    }

    // Kelompokkan notifikasi per hari dengan header
    private fun buildGroupedList(
        list: List<NotificationEntity>
    ): List<NotifListItem> {
        val result    = mutableListOf<NotifListItem>()
        var lastLabel = ""

        list.forEach { entity ->
            val label = getDateLabel(entity.createdAt)
            if (label != lastLabel) {
                result.add(NotifListItem.Header(label))
                lastLabel = label
            }
            result.add(NotifListItem.Item(entity))
        }
        return result
    }

    private fun getDateLabel(timestamp: Long): String {
        val now      = Calendar.getInstance()
        val itemCal  = Calendar.getInstance().apply { timeInMillis = timestamp }

        return when {
            isSameDay(now, itemCal) -> "Hari ini"
            isYesterday(now, itemCal) -> "Kemarin"
            else -> SimpleDateFormat("EEEE, dd MMM", Locale.forLanguageTag("id"))
                .format(Date(timestamp))
        }
    }

    private fun isSameDay(a: Calendar, b: Calendar) =
        a.get(Calendar.YEAR)         == b.get(Calendar.YEAR) &&
                a.get(Calendar.DAY_OF_YEAR)  == b.get(Calendar.DAY_OF_YEAR)

    private fun isYesterday(today: Calendar, other: Calendar): Boolean {
        val yesterday = Calendar.getInstance().apply {
            timeInMillis = today.timeInMillis
            add(Calendar.DAY_OF_YEAR, -1)
        }
        return isSameDay(yesterday, other)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}