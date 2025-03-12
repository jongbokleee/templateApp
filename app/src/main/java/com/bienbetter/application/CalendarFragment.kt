package com.bienbetter.application

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bienbetter.application.databinding.FragmentCalendarBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment() {

    private lateinit var binding: FragmentCalendarBinding
    private lateinit var database: DatabaseReference
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val scheduleMap = mutableMapOf<String, String>() // 🔹 날짜별 일정 저장

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance().getReference("schedules")
        loadSchedulesFromFirebase() // 🔹 Firebase에서 일정 불러오기

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = "$year-${month + 1}-$dayOfMonth"
            val formattedDate = formatDate(selectedDate)

            val schedule = scheduleMap[formattedDate] ?: "선택된 일정이 없습니다."
            binding.tvSelectedSchedule.text = schedule
        }
    }

    // 🔹 Firebase에서 일정 불러오기
    private fun loadSchedulesFromFirebase() {
        val userId = auth.currentUser?.uid ?: return
        database.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                scheduleMap.clear()
                for (child in snapshot.children) {
                    val hospital = child.child("hospital").getValue(String::class.java) ?: "알 수 없음"
                    val date = child.child("date").getValue(String::class.java) ?: "날짜 없음"
                    val formattedDate = formatDate(date) // 🔹 날짜 형식 변환
                    scheduleMap[formattedDate] = "$hospital | $date"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "일정 불러오기 실패: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // 🔹 날짜 형식을 통일하는 함수
    private fun formatDate(date: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-M-d", Locale.getDefault()) // 예: 2025-3-13
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // 예: 2025-03-13
            val parsedDate = inputFormat.parse(date) ?: return date
            outputFormat.format(parsedDate)
        } catch (e: Exception) {
            date
        }
    }
}
