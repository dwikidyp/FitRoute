package com.fitroute.ui.tracking

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class ElevationChartView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var barometerData = listOf<Float>()
    private var gpsData = listOf<Float>()

    private val barometerPaint = Paint().apply {
        color = Color.parseColor("#2D6A4F")
        strokeWidth = 5f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val gpsPaint = Paint().apply {
        color = Color.parseColor("#AAAAAA")
        strokeWidth = 3f
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(10f, 8f), 0f)
        isAntiAlias = true
    }

    private val fillPaint = Paint().apply {
        color = Color.parseColor("#1AD8F3DC")
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    // Set data dari luar
    fun setData(barometer: List<Float>, gps: List<Float>) {
        barometerData = barometer
        gpsData = gps
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (barometerData.isEmpty()) return

        val w = width.toFloat()
        val h = height.toFloat()
        val maxVal = barometerData.maxOrNull() ?: 1f
        val minVal = barometerData.minOrNull() ?: 0f
        val range = (maxVal - minVal).coerceAtLeast(1f)

        // Gambar garis barometer + fill area
        val path = Path()
        val fillPath = Path()
        barometerData.forEachIndexed { i, value ->
            val x = w * i / (barometerData.size - 1).coerceAtLeast(1)
            val y = h - ((value - minVal) / range * h)

            if (i == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, h)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }
        fillPath.lineTo(w, h)
        fillPath.close()

        canvas.drawPath(fillPath, fillPaint)
        canvas.drawPath(path, barometerPaint)

        // Gambar garis GPS
        if (gpsData.isNotEmpty()) {
            val gpsPath = Path()
            gpsData.forEachIndexed { i, value ->
                val x = w * i / (gpsData.size - 1).coerceAtLeast(1)
                val y = h - ((value - minVal) / range * h)
                if (i == 0) gpsPath.moveTo(x, y) else gpsPath.lineTo(x, y)
            }
            canvas.drawPath(gpsPath, gpsPaint)
        }
    }
}