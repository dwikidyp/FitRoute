package com.fitroute.ui.analytics

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class DailyDistanceChartView(
    context: Context, attrs: AttributeSet?
) : View(context, attrs) {

    private var data = listOf<Float>()
    private var activeIndex = 2

    private val linePaint = Paint().apply {
        color       = Color.parseColor("#2D6A4F")
        strokeWidth = 4f
        style       = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val fillPaint = Paint().apply {
        color = Color.parseColor("#20D8F3DC")
        style = Paint.Style.FILL
    }

    private val dotPaint = Paint().apply {
        color       = Color.parseColor("#2D6A4F")
        style       = Paint.Style.FILL
        isAntiAlias = true
    }

    private val dotActivePaint = Paint().apply {
        color       = Color.WHITE
        style       = Paint.Style.FILL
        isAntiAlias = true
    }

    private val dotActiveBorderPaint = Paint().apply {
        color       = Color.parseColor("#2D6A4F")
        style       = Paint.Style.STROKE
        strokeWidth = 3f
        isAntiAlias = true
    }

    fun setData(values: List<Float>, activeDay: Int = 2) {
        data        = values
        activeIndex = activeDay
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (data.isEmpty()) return

        val w      = width.toFloat()
        val h      = height.toFloat()
        val maxVal = data.maxOrNull() ?: 1f
        val padH   = h * 0.15f

        // Hitung posisi tiap titik
        val points = data.mapIndexed { i, v ->
            val x = w * i / (data.size - 1).coerceAtLeast(1)
            val y = padH + (h - padH * 2) * (1f - v / maxVal)
            PointF(x, y)
        }

        // Gambar area fill
        val fillPath = Path()
        fillPath.moveTo(points.first().x, h)
        points.forEach { fillPath.lineTo(it.x, it.y) }
        fillPath.lineTo(points.last().x, h)
        fillPath.close()
        canvas.drawPath(fillPath, fillPaint)

        // Gambar garis
        val linePath = Path()
        points.forEachIndexed { i, p ->
            if (i == 0) linePath.moveTo(p.x, p.y)
            else linePath.lineTo(p.x, p.y)
        }
        canvas.drawPath(linePath, linePaint)

        // Gambar titik
        points.forEachIndexed { i, p ->
            if (i == activeIndex) {
                // Titik aktif: lingkaran putih + border hijau
                canvas.drawCircle(p.x, p.y, 10f, dotActivePaint)
                canvas.drawCircle(p.x, p.y, 10f, dotActiveBorderPaint)
            } else {
                canvas.drawCircle(p.x, p.y, 5f, dotPaint)
            }
        }
    }
}