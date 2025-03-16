package com.bienbetter.application.model

import java.text.SimpleDateFormat
import java.util.*

data class ScheduleItem(
    val hospitalName: String,
    val date: String,
    val dateTimestamp: Long
) {
    companion object {
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        fun create(hospitalName: String, date: String): ScheduleItem {
            val timestamp = try {
                dateFormat.parse(date)?.time ?: 0L
            } catch (e: Exception) {
                0L
            }
            return ScheduleItem(hospitalName, date, timestamp)
        }
    }
}
