package com.bienbetter.application

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bienbetter.application.databinding.ActivityEditScheduleBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class EditScheduleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditScheduleBinding
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val database by lazy { FirebaseDatabase.getInstance().reference }
    private var selectedDate: String? = null
    private var selectedHospital: String? = null
    private var scheduleKey: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        selectedDate = intent.getStringExtra("selected_date")
        selectedHospital = intent.getStringExtra("selected_hospital")
        scheduleKey = intent.getStringExtra("schedule_key")

        setupCalendar()
        setInitialData()

        binding.btnSearch.setOnClickListener {
            showSearchPopup()
        }

        binding.btnAddSchedule.setOnClickListener {
            updateSchedule()
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun setInitialData() {
        binding.etSearch.setText(selectedHospital ?: "")
        selectedDate?.let {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(it)
            date?.let { binding.calendarView.date = date.time }
        }
        updateSelectedScheduleText()
    }

    private fun setupCalendar() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            selectedDate = sdf.format(calendar.time)
            updateSelectedScheduleText()
        }
    }

    private fun updateSelectedScheduleText() {
        val hospital = selectedHospital ?: "병원 미선택"
        val date = selectedDate ?: "날짜 미선택"
        val displayHospital = if (hospital.length > 14) "${hospital.take(6)}...${hospital.takeLast(6)}" else hospital
        binding.tvEditedSchedule.text = "$displayHospital | $date"
    }

    private fun showSearchPopup() {
        val dialog = HospitalSearchDialogFragment { selected ->
            selectedHospital = selected
            binding.etSearch.setText(selected)
            updateSelectedScheduleText()
        }
        dialog.show(supportFragmentManager, "HospitalSearchDialog")
    }

    private fun updateSchedule() {
        val userId = auth.currentUser?.uid ?: return
        val newHospital = selectedHospital ?: return

        if (selectedDate == null) {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = binding.calendarView.date
            selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        }

        val newDate = selectedDate ?: return

        if (scheduleKey == null) {
            Toast.makeText(this, "일정 정보를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val updateData = mapOf(
            "hospital" to newHospital,
            "date" to newDate
        )

        database.child("schedules").child(userId).child(scheduleKey!!).updateChildren(updateData)
            .addOnSuccessListener {
                Toast.makeText(this, "일정이 수정되었습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "수정 실패: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}