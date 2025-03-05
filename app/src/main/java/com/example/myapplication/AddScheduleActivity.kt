package com.example.myapplication

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityAddScheduleBinding
import java.util.*

class AddScheduleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddScheduleBinding
    private var selectedDate: String? = null // 선택한 날짜 저장 변수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewBinding 초기화
        binding = ActivityAddScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 뒤로 가기 버튼 클릭 시 액티비티 종료
        binding.backButton.setOnClickListener {
            finish()
        }



        // 목데이터(병원 선택 스피너)
        val hospitalList = listOf("서울 중앙병원", "부산 시민병원", "대구 메디컬센터", "광주 한마음병원", "대전 건강센터")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, hospitalList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerHospital.adapter = adapter
        binding.spinnerHospital.setSelection(0)

        // 캘린더 기본 날짜를 현재 날짜로 설정
        binding.calendarView.date = Calendar.getInstance().timeInMillis

        // 캘린더에서 선택한 날짜 저장
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = "$year-${month + 1}-$dayOfMonth"
            Toast.makeText(this, "선택한 날짜: $selectedDate", Toast.LENGTH_SHORT).show()
        }

        // 마감 리마인더 버튼 클릭 시
        binding.btnReminder.setOnClickListener {
            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val reminderDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                Toast.makeText(this, "마감 리마인더 설정: $reminderDate", Toast.LENGTH_SHORT).show()
            }, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH))

            datePickerDialog.show()
        }
    }
}
