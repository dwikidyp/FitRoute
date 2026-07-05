package com.fitroute.ui.history

// Model untuk tiap item sesi di RecyclerView
data class SessionUiModel(
    val id: String,
    val activityType: String,
    val activityEmoji: String,
    val distanceKm: String,
    val durationFormatted: String,
    val caloriesKcal: String,
    val elevGainM: String,
    val dateFormatted: String,
    val weekLabel: String
)

// Extension: konversi list entity ke list UI model + grouping per minggu
fun List<com.fitroute.data.local.WorkoutSessionEntity>.toUiModels(): List<SessionUiModel> {
    return this.map { session ->
        val emoji = when (session.activityType) {
            "RUNNING" -> "🏃"
            "CYCLING" -> "🚴"
            "HIKING"  -> "🥾"
            else      -> "🏃"
        }

        val jam   = session.durationSec / 3600
        val menit = (session.durationSec % 3600) / 60
        val detik = session.durationSec % 60
        val durasi = if (jam > 0)
            "%d:%02d:%02d".format(jam, menit, detik)
        else "%d:%02d".format(menit, detik)

        val fmt = java.text.SimpleDateFormat("EEE, dd MMM yyyy", java.util.Locale("id"))
        val date = fmt.format(java.util.Date(session.startedAt))

        val weekLabel = getWeekLabel(session.startedAt)

        SessionUiModel(
            id               = session.id,
            activityType     = session.activityType,
            activityEmoji    = emoji,
            distanceKm       = "%.1f km".format(session.distanceKm),
            durationFormatted = durasi,
            caloriesKcal     = "%.0f kcal".format(session.caloriesKcal),
            elevGainM        = "+%.0fm".format(session.elevGainM),
            dateFormatted    = date,
            weekLabel        = weekLabel
        )
    }
}

private fun getWeekLabel(timestamp: Long): String {
    val now = java.util.Calendar.getInstance()
    val sessionCal = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }

    val nowWeek     = now.get(java.util.Calendar.WEEK_OF_YEAR)
    val sessionWeek = sessionCal.get(java.util.Calendar.WEEK_OF_YEAR)
    val nowYear     = now.get(java.util.Calendar.YEAR)
    val sessionYear = sessionCal.get(java.util.Calendar.YEAR)

    return when {
        nowYear == sessionYear && nowWeek == sessionWeek     -> "Minggu ini"
        nowYear == sessionYear && nowWeek - sessionWeek == 1 -> "Minggu lalu"
        else -> {
            val fmt = java.text.SimpleDateFormat("MMM yyyy", java.util.Locale("id"))
            fmt.format(java.util.Date(timestamp))
        }
    }
}