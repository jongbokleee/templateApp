package com.bienbetter.application

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bienbetter.application.databinding.ActivityAddScheduleBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class AddScheduleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddScheduleBinding
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val database by lazy { FirebaseDatabase.getInstance().reference }
    private var selectedDate: String? = null
    private var selectedHospital: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCalendar()

        // 🔹 검색 버튼 클릭 시 팝업 실행
        binding.btnSearch.setOnClickListener {
            showSearchPopup()
        }

        binding.backButton.setOnClickListener {
            finish()  // 현재 액티비티 종료
        }

        binding.btnAddSchedule.setOnClickListener {
            val uid = auth.currentUser?.uid
            if (uid != null && selectedDate != null && selectedHospital != null) {
                val schedule = mapOf(
                    "date" to selectedDate,
                    "hospital" to selectedHospital,
                    "uid" to uid,
                    "userId" to uid
                )
                database.child("schedules").child(uid).push().setValue(schedule)
                    .addOnSuccessListener {
                        Toast.makeText(this, "일정이 저장되었습니다.", Toast.LENGTH_SHORT).show()
                        // ✅ 홈으로 명시적으로 이동
                        val intent = Intent(this, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()

                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "날짜와 병원을 모두 선택하세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ✅ 캘린더 선택 기능 추가
    private fun setupCalendar() {
        val today = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        selectedDate = sdf.format(today.time)

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            selectedDate = sdf.format(calendar.time)

            Toast.makeText(this, "선택한 날짜: $selectedDate", Toast.LENGTH_SHORT).show()
        }
    }

    // ✅ 병원 검색 팝업 표시
    private fun showSearchPopup() {
        val dialog = HospitalSearchDialogFragment { selected ->
            selectedHospital = selected
            binding.etSearch.setText(selected)

            // ✅ 병원명 표시 포맷팅
            val displayName = selectedHospital?.let {
                if (it.length > 14) {
                    val prefix = it.take(6)
                    val suffix = it.takeLast(6)
                    "$prefix...$suffix"
                } else {
                    it
                }
            } ?: ""

            binding.tvEditedSchedule.text = "선택 병원: $displayName"
        }
        dialog.show(supportFragmentManager, "HospitalSearchDialog")
    }
}
