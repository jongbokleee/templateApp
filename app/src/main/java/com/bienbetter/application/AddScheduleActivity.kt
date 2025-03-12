package com.bienbetter.application

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bienbetter.application.databinding.ActivityAddScheduleBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class AddScheduleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddScheduleBinding
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val database by lazy { FirebaseDatabase.getInstance().reference.child("schedules") }

    private var selectedDate: String? = null
    private var selectedHospital: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewBinding 초기화
        binding = ActivityAddScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupHospitalSpinner()
        setupCalendar()
        setupButtons()

        // 뒤로 가기 버튼 클릭 시 액티비티 종료
        binding.backButton.setOnClickListener {
            finish()
        }
    }

    // ✅ 병원 선택 스피너 설정
    private fun setupHospitalSpinner() {
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
        binding.spinnerHospital.adapter = adapter

        binding.spinnerHospital.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedHospital = hospitalList[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    // ✅ 캘린더 선택 기능 추가
    private fun setupCalendar() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = "$year-${month + 1}-$dayOfMonth"
            Toast.makeText(this, "선택한 날짜: $selectedDate", Toast.LENGTH_SHORT).show()
        }
    }

    // ✅ 버튼 클릭 리스너 설정
    private fun setupButtons() {
        binding.btnAddSchedule.setOnClickListener {
            if (selectedHospital.isNullOrEmpty() || selectedDate.isNullOrEmpty()) {
                Toast.makeText(this, "모든 정보를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newSchedule = mapOf(
                "hospital" to selectedHospital,
                "date" to selectedDate,
                "userId" to auth.currentUser?.uid
            )

            saveScheduleToFirebase(newSchedule) // ✅ Firebase 저장
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    // **📌 Firebase Realtime Database에 일정 저장 (uid 기반 저장)**
    private fun saveScheduleToFirebase(schedule: Map<String, Any?>) {
        val userId = auth.currentUser?.uid ?: return // 🔹 로그인한 사용자 UID 가져오기
        val scheduleId = database.child(userId).push().key ?: return // 🔹 UID 하위에 일정 저장

        val scheduleWithUid = schedule.toMutableMap().apply {
            put("uid", userId) // 🔹 UID 추가
        }

        database.child(userId).child(scheduleId).setValue(scheduleWithUid)
            .addOnSuccessListener {
                Toast.makeText(this, "일정이 추가되었습니다.", Toast.LENGTH_SHORT).show()
                // setReminderNotification() 호출 → 검진 하루 전 알람 설정
                setReminderNotification(schedule["date"].toString()) // 🔹 알람 설정

                // ✅ HomeFragment로 이동하며 일정 데이터 전달
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("navigateTo", "HomeFragment")
                intent.putExtra("newSchedule", scheduleWithUid.toString()) // 🔹 일정 데이터 전달
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "일정 추가 실패: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    // **📌 검진 예약일 하루 전 알림 설정**
    private fun setReminderNotification(selectedDate: String) {
        if (selectedDate.isEmpty()) return

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val selectedCalendar = Calendar.getInstance()

        try {
            val parsedDate = dateFormat.parse(selectedDate) ?: return
            selectedCalendar.time = parsedDate
            selectedCalendar.add(Calendar.DAY_OF_MONTH, -1)

            val userId = auth.currentUser?.uid ?: return
            val timestamp = selectedCalendar.timeInMillis

            sendPushNotificationToUser(userId, selectedDate, timestamp)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sendPushNotificationToUser(userId: String, selectedDate: String, timestamp: Long) {
        val notificationData = mapOf(
            "userId" to userId,
            "title" to "예약 하루 전 알림",
            "body" to "예약일 ($selectedDate) 하루 전입니다. 병원을 확인하세요!",
            "timestamp" to timestamp
        )

        val fcmDatabase = FirebaseDatabase.getInstance().reference.child("notifications")
        fcmDatabase.child(userId).push().setValue(notificationData)
    }

}