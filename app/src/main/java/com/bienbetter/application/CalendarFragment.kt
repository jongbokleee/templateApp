package com.bienbetter.application

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
    private val scheduleMap = mutableMapOf<String, MutableList<Triple<String, String, String>>>() // date -> List<Triple<hospital, date, key>>
    private val scheduleDates = mutableListOf<CalendarDay>()
    private var selectedDate: String? = null

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

    override fun onResume() {
        super.onResume()
        loadSchedulesFromFirebase()
    }

    private fun setupCalendarView() {
        binding.calendarView.state().edit()
            .setCalendarDisplayMode(CalendarMode.MONTHS)
            .setFirstDayOfWeek(DayOfWeek.SUNDAY)
            .commit()

        // ✅ 상단 타이틀 (YYYY년 MM월)
        binding.calendarView.setTitleFormatter { day ->
            val calendar = Calendar.getInstance()
            calendar.set(day.year, day.month - 1, 1)
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
            val selectedDateStr = formatDate("${date.year}-${date.month}-${date.day}")
            displaySchedulesForDate(selectedDateStr)
        }
    }

    private fun displaySchedulesForDate(dateStr: String) {
        binding.scheduleListContainer.removeAllViews()
        val schedules = scheduleMap[dateStr]

        if (schedules.isNullOrEmpty()) {
            val tv = TextView(requireContext()).apply {
                text = "선택된 일정이 없습니다."
                setPadding(16, 16, 16, 16)
            }
            binding.scheduleListContainer.addView(tv)
            return
        }

        for ((hospital, date, key) in schedules) {
            val itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_schedule_editable, null)
            val tvHospital = itemView.findViewById<TextView>(R.id.tvHospital)
            val btnEdit = itemView.findViewById<TextView>(R.id.btnEdit)

            tvHospital.text = "$hospital | $date"
            btnEdit.setOnClickListener {
                val intent = Intent(requireContext(), EditScheduleActivity::class.java).apply {
                    putExtra("selected_date", date)
                    putExtra("selected_hospital", hospital)
                    putExtra("schedule_key", key)
                }
                startActivity(intent)
            }
            binding.scheduleListContainer.addView(itemView)
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
                    val key = child.key ?: continue
                    val hospital = child.child("hospital").getValue(String::class.java) ?: continue
                    val date = child.child("date").getValue(String::class.java) ?: continue
                    val formattedDate = formatDate(date)

                    val list = scheduleMap.getOrPut(formattedDate) { mutableListOf() }
                    list.add(Triple(hospital, formattedDate, key))

                    parseDateToCalendarDay(formattedDate)?.let { scheduleDates.add(it) }
                }

                binding.calendarView.removeDecorators()
                binding.calendarView.addDecorator(EventDecorator(Color.RED, scheduleDates))

                // ✅ HomeFragment에서 넘어온 날짜를 캘린더에 반영 (Firebase 데이터 로딩 후)
                selectedDate?.let { date ->
                    val parsed = parseDateToCalendarDay(date)
                    parsed?.let {
                        binding.calendarView.setDateSelected(it, true)
                        binding.calendarView.currentDate = it
                        displaySchedulesForDate(date)
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
            val inputFormat = SimpleDateFormat("yyyy-M-d", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
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
                CalendarDay.from(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1,
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
