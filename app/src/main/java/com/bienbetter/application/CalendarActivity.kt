package com.bienbetter.application

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bienbetter.application.databinding.ActivityCalendarBinding

class CalendarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalendarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val historySet = getSharedPreferences("검진기록", Context.MODE_PRIVATE)
            .getStringSet("historyList", setOf()) ?: setOf()

        val schedules = historySet.toList()

        // 뒤로 가기 버튼 클릭 시 액티비티 종료
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = "$year-${month + 1}-$dayOfMonth"
            val schedule = schedules.find { it.contains(selectedDate) } ?: "선택된 일정이 없습니다."
            binding.tvSelectedSchedule.text = schedule
        }
    }


}
