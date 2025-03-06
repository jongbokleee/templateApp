package com.example.myapplication

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityCalendarBinding

class CalendarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalendarBinding

    // Mock 일정 데이터 (Key: 날짜, Value: 일정 내용)
    private val mockSchedules = mapOf(
        "2025-03-05" to "건강검진 서울 중앙병원"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val historySet = getSharedPreferences("검진기록", Context.MODE_PRIVATE)
            .getStringSet("historyList", setOf()) ?: setOf()

        val schedules = historySet.toList()

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = "$year-${month + 1}-$dayOfMonth"
            val schedule = schedules.find { it.contains(selectedDate) } ?: "선택된 일정이 없습니다."
            binding.tvSelectedSchedule.text = schedule
        }
    }
}
