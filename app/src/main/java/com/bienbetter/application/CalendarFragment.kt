package com.bienbetter.application

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bienbetter.application.databinding.FragmentCalendarBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.prolificinteractive.materialcalendarview.*
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import org.threeten.bp.DayOfWeek
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment() {

    private lateinit var binding: FragmentCalendarBinding
    private lateinit var database: DatabaseReference
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val scheduleMap = mutableMapOf<String, String>() // 🔹 날짜별 일정 저장
    private val scheduleDates = mutableListOf<CalendarDay>() // 🔹 캘린더에서 표시할 날짜 저장
    private var selectedDate: String? = null // 🔹 HomeFragment에서 넘어온 날짜 저장

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

        setupCalendarView() // ✅ 캘린더 설정
        loadSchedulesFromFirebase() // 🔹 Firebase에서 일정 불러오기

        // ✅ HomeFragment에서 선택한 날짜를 받아 저장
        selectedDate = arguments?.getString("selected_date")
    }

    // ✅ 캘린더 설정 (월 제목 및 요일 표시)
    private fun setupCalendarView() {
        binding.calendarView.state().edit()
            .setCalendarDisplayMode(CalendarMode.MONTHS)
            .setFirstDayOfWeek(DayOfWeek.SUNDAY)
            .commit()

        // ✅ 상단 타이틀 (YYYY년 MM월)
        binding.calendarView.setTitleFormatter { day ->
            val calendar = Calendar.getInstance()
            calendar.set(day.year, day.month - 1, 1) // 📌 `-1`로 보정 필요
            SimpleDateFormat("yyyy년 MM월", Locale.getDefault()).format(calendar.time)
        }

        // ✅ 요일 헤더 한글 변환
        binding.calendarView.setWeekDayFormatter { dayOfWeek ->
            when (dayOfWeek) {
                DayOfWeek.SUNDAY -> "일"
                DayOfWeek.MONDAY -> "월"
                DayOfWeek.TUESDAY -> "화"
                DayOfWeek.WEDNESDAY -> "수"
                DayOfWeek.THURSDAY -> "목"
                DayOfWeek.FRIDAY -> "금"
                DayOfWeek.SATURDAY -> "토"
                else -> ""
            }
        }

        // ✅ 날짜 선택 시 일정 표시
        binding.calendarView.setOnDateChangedListener { _, date, _ ->
            val selectedDateStr = formatDate("${date.year}-${date.month + 1}-${date.day}")
            binding.tvSelectedSchedule.text = scheduleMap[selectedDateStr] ?: "선택된 일정이 없습니다."
        }
    }

    // 🔹 Firebase에서 일정 불러오기
    private fun loadSchedulesFromFirebase() {
        val userId = auth.currentUser?.uid ?: return
        database.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                scheduleMap.clear()
                scheduleDates.clear()

                for (child in snapshot.children) {
                    val hospital = child.child("hospital").getValue(String::class.java) ?: "알 수 없음"
                    val date = child.child("date").getValue(String::class.java) ?: "날짜 없음"
                    val formattedDate = formatDate(date) // 🔹 날짜 형식 변환
                    scheduleMap[formattedDate] = "$hospital | $date"

                    parseDateToCalendarDay(formattedDate)?.let {
                        scheduleDates.add(it)
                    }
                }

                // ✅ 일정이 있는 날짜에 원(DotSpan) 추가
                binding.calendarView.addDecorator(EventDecorator(Color.BLUE, scheduleDates))

                // ✅ HomeFragment에서 넘어온 날짜를 캘린더에 반영 (Firebase 데이터 로딩 후)
                selectedDate?.let { date ->
                    val parsedDate = parseDateToCalendarDay(date)
                    parsedDate?.let {
                        binding.calendarView.setDateSelected(it, true)
                        binding.calendarView.currentDate = it
                        binding.tvSelectedSchedule.text = scheduleMap[date] ?: "선택된 일정이 없습니다."
                    }
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

    // 🔹 문자열 날짜를 CalendarDay로 변환
    private fun parseDateToCalendarDay(dateStr: String): CalendarDay? {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(dateStr)
            val calendar = Calendar.getInstance()
            date?.let {
                calendar.time = it
                return CalendarDay.from(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH), // ✅ `+1` 제거 (MaterialCalendarView는 0부터 시작)
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
            }
        } catch (e: Exception) {
            null
        }
    }
}

// ✅ 특정 날짜에 점 추가하는 Decorator 클래스
class EventDecorator(private val color: Int, private val dates: Collection<CalendarDay>) :
    DayViewDecorator {

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return dates.contains(day)
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(DotSpan(10F, color))
    }
}
