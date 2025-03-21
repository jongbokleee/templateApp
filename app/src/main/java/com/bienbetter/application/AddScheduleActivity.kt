package com.bienbetter.application

import android.content.Intent
import android.os.Bundle
import android.view.View
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
    private val database by lazy { FirebaseDatabase.getInstance().reference.child("hospitals") }

    private var selectedDate: String? = null
    private var selectedHospital: String? = null
    private var selectedCityCode: String? = null
    private var selectedDistrictCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCitySpinner()
        setupCalendar()
        setupButtons()

        binding.backButton.setOnClickListener { finish() }
    }

    // ✅ `hospitals` 하위 데이터에서 시도(광역시/도) 목록 불러오기
    private fun setupCitySpinner() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cityMap = mutableMapOf<String, String>()
                val cityNames = mutableListOf<String>()

                for (child in snapshot.children) {
                    val cityCode = child.key ?: continue
                    cityMap[cityCode] = cityCode // 예: "11", "41"
                    cityNames.add(cityCode)
                }

                updateCitySpinner(cityNames, cityMap)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // ✅ 시도(광역시/도) 스피너 업데이트
    private fun updateCitySpinner(cityNames: List<String>, cityMap: Map<String, String>) {
        val cityAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, cityNames)
        binding.spinnerCity.adapter = cityAdapter

        binding.spinnerCity.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCityCode = cityMap[cityNames[position]]
                selectedCityCode?.let { loadDistricts(it) }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    // ✅ `hospitals > {시도코드}` 하위에서 시군구 목록 불러오기
    private fun loadDistricts(cityCode: String) {
        database.child(cityCode).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val districtMap = mutableMapOf<String, String>()
                val districtNames = mutableListOf<String>()

                for (child in snapshot.children) {
                    val districtCode = child.key ?: continue
                    districtMap[districtCode] = districtCode // 예: "110", "140"
                    districtNames.add(districtCode)
                }

                updateDistrictSpinner(districtNames, districtMap)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // ✅ 시군구 스피너 업데이트
    private fun updateDistrictSpinner(districtNames: List<String>, districtMap: Map<String, String>) {
        val districtAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, districtNames)
        binding.spinnerDistrict.adapter = districtAdapter

        binding.spinnerDistrict.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedDistrictCode = districtMap[districtNames[position]]
                selectedCityCode?.let { cityCode ->
                    selectedDistrictCode?.let { districtCode ->
                        loadHospitals(cityCode, districtCode)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    // ✅ `hospitals > {시도코드} > {시군구코드}` 하위에서 병원 목록 불러오기
    private fun loadHospitals(cityCode: String, districtCode: String) {
        database.child(cityCode).child(districtCode).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val hospitalNames = mutableListOf<String>()

                for (child in snapshot.children) {
                    val hospitalName = child.child("name").getValue(String::class.java) ?: continue
                    hospitalNames.add(hospitalName)
                }

                updateHospitalSpinner(hospitalNames)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // ✅ 병원 스피너 업데이트
    private fun updateHospitalSpinner(hospitalNames: List<String>) {
        val hospitalAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, hospitalNames)
        binding.spinnerHospital.adapter = hospitalAdapter

        binding.spinnerHospital.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedHospital = hospitalNames[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    // ✅ 캘린더 선택 기능 추가
    private fun setupCalendar() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            selectedDate = sdf.format(calendar.time)

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

            saveScheduleToFirebase(newSchedule)
        }
    }

    private fun saveScheduleToFirebase(schedule: Map<String, Any?>) {
        val userId = auth.currentUser?.uid ?: return
        val scheduleId = FirebaseDatabase.getInstance().reference.child("schedules").child(userId).push().key ?: return

        FirebaseDatabase.getInstance().reference.child("schedules").child(userId).child(scheduleId).setValue(schedule)
            .addOnSuccessListener {
                Toast.makeText(this, "일정이 추가되었습니다.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java).apply {
                    putExtra("navigateTo", "HistoryFragment")
                })
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "일정 추가 실패: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }
}
