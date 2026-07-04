package com.example.chatchat.data

import com.example.chatchat.model.CallLogItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AppRepository {

    fun buildRoomId(firstUid: String, secondUid: String): String {
        return if (firstUid < secondUid) {
            "${firstUid}_${secondUid}"
        } else {
            "${secondUid}_${firstUid}"
        }
    }

    fun sampleCallLogs(): List<CallLogItem> {
        return listOf(
            CallLogItem("Mr Silva", formatDate(System.currentTimeMillis() - 86_400_000), false),
            CallLogItem("Sew", formatDate(System.currentTimeMillis() - 172_800_000), true),
            CallLogItem("Pamo", formatDate(System.currentTimeMillis() - 259_200_000), false)
        )
    }

    private fun formatDate(timeMillis: Long): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timeMillis))
    }
}
