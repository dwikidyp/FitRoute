package com.fitroute.ui.history

enum class ActivityFilter(val apiValue: String, val label: String) {
    ALL("ALL", "Semua"),
    RUNNING("RUNNING", "Lari"),
    CYCLING("CYCLING", "Sepeda"),
    HIKING("HIKING", "Hiking")
}