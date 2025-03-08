package com.example.myapplication

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityAddScheduleBinding
import java.text.SimpleDateFormat
import java.util.*

class AddScheduleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddScheduleBinding
    private var selectedDate: String? = null // 선택한 날짜 저장 변수
    private var selectedHospital: String? = null // 선택한 날짜 저장 변수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewBinding 초기화
        binding = ActivityAddScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 목데이터(병원 선택 스피너)
        val hospitalList = listOf("서울 중앙병원", "부산 시민병원", "대구 메디컬센터", "광주 한마음병원", "대전 건강센터")
        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, hospitalList) {
            override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                val view = super.getView(position, convertView, parent)
                (view as TextView).setTextColor(Color.BLACK) // ✅ Make Spinner text black
                return view
            }

            override fun getDropDownView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                val view = super.getDropDownView(position, convertView, parent)
                (view as TextView).setTextColor(Color.BLACK) // ✅ Make dropdown text black
                return view
            }
        }
        binding.spinnerHospital.adapter = adapter
        binding.spinnerHospital.setSelection(0)
        binding.spinnerHospital.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedHospital = hospitalList[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // 캘린더에서 선택한 날짜 저장
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = "$year-${month + 1}-$dayOfMonth"
            Toast.makeText(this, "선택한 날짜: $selectedDate", Toast.LENGTH_SHORT).show()
        }


        // 일정 추가 버튼 클릭 시 데이터 저장 후 홈 & 기록 & 일정 탭에 반영
        binding.btnAddSchedule.setOnClickListener {
            if (selectedHospital == null || selectedDate == null) {
                Toast.makeText(this, "모든 정보를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newSchedule = "건강검진 | $selectedHospital | $selectedDate"
            saveSchedule(newSchedule) // ✅ 일정 저장
            setReminderNotification(selectedDate!!) // 자동으로 전날 알림 설정

            // 홈 화면 및 기록 탭으로 데이터 전달
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("newSchedule", newSchedule)
            startActivity(intent)
            Toast.makeText(this, "일정이 추가되었습니다.", Toast.LENGTH_SHORT).show()
            finish()
        }

        // 뒤로 가기 버튼 클릭 시 액티비티 종료
        binding.backButton.setOnClickListener {
            finish()
        }
    }

    // 일정 데이터를 SharedPreferences에 저장하는 함수
    private fun saveSchedule(scheduleText: String) {
        val sharedPreferences = getSharedPreferences("검진기록", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // 기존 데이터 불러오기
        val savedHistory = sharedPreferences.getStringSet("historyList", setOf())?.toMutableSet()
        savedHistory?.add(scheduleText) // 새로운 일정 추가

        // 데이터 저장
        editor.putStringSet("historyList", savedHistory)
        editor.apply()
    }

    // **📌 검진 예약일 하루 전 알림 설정**
    private fun setReminderNotification(selectedDate: String) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val selectedCalendar = Calendar.getInstance()

        try {
            val parsedDate = dateFormat.parse(selectedDate)
            if (parsedDate != null) {
                selectedCalendar.time = parsedDate
                selectedCalendar.add(Calendar.DAY_OF_MONTH, -1) // 선택한 날짜 하루 전으로 설정

                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val intent = Intent(this, ReminderReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

                alarmManager.setExact(AlarmManager.RTC_WAKEUP, selectedCalendar.timeInMillis, pendingIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
