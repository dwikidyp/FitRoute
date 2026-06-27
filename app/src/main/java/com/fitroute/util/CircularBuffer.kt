package com.fitroute.util

class CircularBuffer(private val size: Int) {

    private val buffer = DoubleArray(size)
    private var index = 0
    private var count = 0

    // Tambahkan nilai baru ke buffer
    fun add(value: Double) {
        buffer[index % size] = value
        index++
        if (count < size) count++
    }

    // Ambil semua data yang sudah terisi
    fun getAll(): DoubleArray = buffer.copyOf(count)

    // Hitung frekuensi puncak (langkah per detik)
    fun peakFrequency(): Double {
        val data = getAll()
        if (data.size < 2) return 0.0

        val avg = data.average()
        var crossings = 0

        // Hitung berapa kali nilai melewati rata-rata (naik)
        for (i in 1 until data.size) {
            if (data[i - 1] < avg && data[i] >= avg) {
                crossings++
            }
        }

        // Asumsikan sensor 50Hz, buffer 50 sampel = 1 detik
        return crossings.toDouble()
    }

    fun clear() {
        index = 0
        count = 0
    }
}