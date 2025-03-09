package com.bienbetter.application

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bienbetter.application.databinding.FragmentCalendarBinding

class CalendarFragment : Fragment() {

    private lateinit var binding: FragmentCalendarBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireContext().getSharedPreferences("검진기록", Context.MODE_PRIVATE)
        val historySet = sharedPreferences.getStringSet("historyList", setOf()) ?: setOf()
        val schedules = historySet.toList()

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = "$year-${month + 1}-$dayOfMonth"
            val schedule = schedules.find { it.contains(selectedDate) } ?: "선택된 일정이 없습니다."
            binding.tvSelectedSchedule.text = schedule
        }
    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//
//        // 뒤로 가기 버튼 클릭 시 액티비티 종료
//        binding.backButton.setOnClickListener {
//            finish()
//        }
//    }


}
