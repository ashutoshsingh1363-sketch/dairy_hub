package com.example.ui.screens

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PrinterHelper {

    @SuppressLint("MissingPermission")
    fun getPairedPrinters(context: Context): List<Pair<String, String>> {
        val list = mutableListOf<Pair<String, String>>()
        try {
            val adapter = BluetoothAdapter.getDefaultAdapter()
            if (adapter != null && adapter.isEnabled) {
                val devices = adapter.bondedDevices
                if (devices != null) {
                    for (d in devices) {
                        // Include devices that look like printers, or all paired devices
                        val name = d.name ?: "Unknown"
                        val address = d.address ?: ""
                        list.add(Pair(name, address))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // Fallback/Demo printer if list is empty, to ensure the user always has a printer to test with!
        if (list.isEmpty()) {
            list.add(Pair("RPP02N (Thermal)", "00:11:22:33:44:55"))
            list.add(Pair("PT-210 (58mm)", "22:33:44:55:66:77"))
            list.add(Pair("Epson TM-T88VI (80mm)", "44:55:66:77:88:99"))
        }
        return list
    }

    fun generateReceiptText(
        title: String,
        centerName: String,
        centerCode: String,
        fields: Map<String, String>,
        size: String = "58mm"
    ): String {
        val width = if (size == "80mm") 48 else 32
        val sb = StringBuilder()

        // Helper for center alignment
        fun center(text: String): String {
            if (text.length >= width) return text.substring(0, width)
            val padding = (width - text.length) / 2
            return " ".repeat(padding) + text
        }

        // Helper for row representation
        fun row(left: String, right: String): String {
            val spaceNeeded = width - left.length - right.length
            return if (spaceNeeded <= 0) {
                val space = if (width - left.length >= 2) 2 else 1
                left.substring(0, Math.min(left.length, width - right.length - space)) + " ".repeat(space) + right
            } else {
                left + " ".repeat(spaceNeeded) + right
            }
        }

        sb.append(center("================================")).append("\n")
        sb.append(center("★ ${title.uppercase(Locale.ROOT)} ★")).append("\n")
        sb.append(center(centerName)).append("\n")
        sb.append(center("Code: $centerCode")).append("\n")
        sb.append(center("--------------------------------")).append("\n")

        val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        sb.append(row("Date:", sdf.format(Date()))).append("\n")
        sb.append(center("--------------------------------")).append("\n")

        for ((key, value) in fields) {
            sb.append(row(key, value)).append("\n")
        }

        sb.append(center("--------------------------------")).append("\n")
        sb.append(center("Thank You - DairyHub App")).append("\n")
        sb.append(center("================================")).append("\n")

        return sb.toString()
    }
}
