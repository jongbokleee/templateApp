package com.bienbetter.application

import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class EditScheduleActivity : AppCompatActivity() {

    private lateinit var spinnerHospital: Spinner
    private lateinit var calendarView: CalendarView
    private lateinit var btnEditSchedule: Button
    private lateinit var backButton: Button

    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance().getReference("schedules")

    private var selectedDate: String? = null
    private var selectedHospital: String? = null
    private var scheduleKey: String? = null // 🔹 Firebase에서 해당 일정의 키를 저장

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_schedule)

        // UI 요소 초기화
        spinnerHospital = findViewById(R.id.spinnerHospital)
        calendarView = findViewById(R.id.calendarView)
        btnEditSchedule = findViewById(R.id.btnAddSchedule)
        backButton = findViewById(R.id.backButton)

        auth = FirebaseAuth.getInstance()

        // 🔹 HomeFragment 또는 CalendarFragment에서 전달된 데이터 받기
        selectedDate = intent.getStringExtra("selected_date")
        selectedHospital = intent.getStringExtra("selected_hospital")
        scheduleKey = intent.getStringExtra("schedule_key")

        // 🔹 전달된 데이터 확인 (디버깅용)
        Toast.makeText(this, "수정할 일정: $selectedDate, 병원: $selectedHospital", Toast.LENGTH_LONG).show()

        setupSpinner() // 병원 목록 세팅
        setupCalendarView() // 날짜 선택 기능 추가

        // 🔹 기존 데이터 반영
        setInitialData()

        // 🔹 수정 버튼 클릭 시 데이터 업데이트
        btnEditSchedule.setOnClickListener {
            updateSchedule()
        }

        // 🔹 뒤로 가기 버튼
        backButton.setOnClickListener {
            finish()
        }
    }

    // 🔹 병원 목록을 스피너에 설정
    private fun setupSpinner() {
        val hospitalList = listOf("서울 중앙병원", "부산 시민병원", "대구 메디컬센터", "광주 한마음병원", "대전 건강센터")
        val adapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            hospitalList
        ) {
            override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                val view = super.getView(position, convertView, parent)
                (view as TextView).setTextColor(Color.BLACK)
                return view
            }

            override fun getDropDownView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                val view = super.getDropDownView(position, convertView, parent)
                (view as TextView).setTextColor(Color.BLACK)
                return view
            }
        }

        spinnerHospital.adapter = adapter

        // 🔹 기존 선택된 병원 설정 (지연 실행)
        selectedHospital?.let { hospital ->
            spinnerHospital.postDelayed({
                val position = hospitalList.indexOf(hospital)
                if (position >= 0) {
                    spinnerHospital.setSelection(position)
                } else {
                    Toast.makeText(this, "병원 목록에서 선택된 병원을 찾을 수 없음", Toast.LENGTH_SHORT).show()
                }
            }, 100) // 🔹 100ms 지연 (adapter 설정 후 실행)
        }
    }

    // 🔹 캘린더 초기 설정 (기존 날짜 선택)
    private fun setupCalendarView() {
        selectedDate?.let {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(it)
            date?.let {
                calendarView.date = date.time // 기존 날짜 선택
            }
        }
    }

    // 🔹 기존 데이터를 UI에 반영
    private fun setInitialData() {
        selectedDate?.let {
            Toast.makeText(this, "수정할 일정: $it, 병원: $selectedHospital", Toast.LENGTH_SHORT).show()
        }
    }

    // 🔹 Firebase 일정 업데이트
    private fun updateSchedule() {
        val userId = auth.currentUser?.uid ?: return
        val newHospital = spinnerHospital.selectedItem.toString()

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = calendarView.date
        val newDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        if (scheduleKey == null) {
            Toast.makeText(this, "일정 정보를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val updateData = mapOf(
            "hospital" to newHospital,
            "date" to newDate
        )

        // 🔹 Firebase 업데이트 실행
        database.child(userId).child(scheduleKey!!).updateChildren(updateData)
            .addOnSuccessListener {
                Toast.makeText(this, "일정이 수정되었습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "수정 실패: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
