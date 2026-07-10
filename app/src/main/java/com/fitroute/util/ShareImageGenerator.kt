package com.fitroute.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.fitroute.data.local.WorkoutSessionEntity

object ShareImageGenerator {

    // Buat kartu ringkasan sebagai Bitmap 1080x1080
    fun generateShareCard(
        context: Context,
        session: WorkoutSessionEntity
    ): Bitmap {
        val size   = 1080
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background hijau gelap
        canvas.drawColor(Color.parseColor("#1F5C2E"))

        // Gambar overlay stats
        drawStatsOverlay(canvas, session, size)

        return bitmap
    }

    private fun drawStatsOverlay(
        canvas: Canvas,
        session: WorkoutSessionEntity,
        size: Int
    ) {
        val padding = 80f

        // === JUDUL APP ===
        val titlePaint = Paint().apply {
            color     = Color.WHITE
            textSize  = 60f
            typeface  = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("FitRoute", padding, 120f, titlePaint)

        // === TIPE AKTIVITAS ===
        val activityEmoji = when (session.activityType) {
            "RUNNING" -> "🏃 Lari"
            "CYCLING" -> "🚴 Bersepeda"
            "HIKING"  -> "🥾 Hiking"
            else      -> session.activityType
        }
        val actPaint = Paint().apply {
            color     = Color.parseColor("#52B788")
            textSize  = 48f
            typeface  = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(activityEmoji, padding, 200f, actPaint)

        // === GARIS PEMISAH ===
        val linePaint = Paint().apply {
            color       = Color.parseColor("#52B788")
            strokeWidth = 3f
        }
        canvas.drawLine(padding, 230f, size - padding, 230f, linePaint)

        // === STATS UTAMA ===
        drawStatBox(canvas, "%.1f".format(session.distanceKm),
            "KM", padding, 320f, size)
        drawStatBox(canvas, formatDuration(session.durationSec),
            "DURASI", size / 2f, 320f, size)

        // === GARIS PEMISAH 2 ===
        canvas.drawLine(padding, 500f, size - padding, 500f, linePaint)

        // === STATS TAMBAHAN ===
        drawStatBox(canvas, "%.0f".format(session.caloriesKcal),
            "KCAL", padding, 620f, size)
        drawStatBox(canvas, "+%.0fm".format(session.elevGainM),
            "ELEVASI", size / 2f, 620f, size)

        // === GARIS PEMISAH 3 ===
        canvas.drawLine(padding, 780f, size - padding, 780f, linePaint)

        // === TANGGAL ===
        val dateFmt = java.text.SimpleDateFormat(
            "dd MMMM yyyy", java.util.Locale("id")
        )
        val dateStr = dateFmt.format(java.util.Date(session.startedAt))
        val datePaint = Paint().apply {
            color     = Color.parseColor("#99FFFFFF")
            textSize  = 38f
            isAntiAlias = true
        }
        canvas.drawText(dateStr, padding, 870f, datePaint)

        // === WATERMARK ===
        val wmPaint = Paint().apply {
            color     = Color.parseColor("#60FFFFFF")
            textSize  = 32f
            isAntiAlias = true
        }
        canvas.drawText("fitroute.app", padding, size - padding, wmPaint)
    }

    private fun drawStatBox(
        canvas: Canvas,
        value: String,
        label: String,
        x: Float,
        y: Float,
        size: Int
    ) {
        val valuePaint = Paint().apply {
            color     = Color.WHITE
            textSize  = 100f
            typeface  = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val labelPaint = Paint().apply {
            color     = Color.parseColor("#99FFFFFF")
            textSize  = 36f
            isAntiAlias = true
        }
        canvas.drawText(value, x, y, valuePaint)
        canvas.drawText(label, x, y + 50f, labelPaint)
    }

    private fun formatDuration(seconds: Int): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        return if (h > 0) "%d:%02d jam".format(h, m)
        else "%d mnt".format(m)
    }
}