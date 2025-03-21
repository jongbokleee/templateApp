package com.bienbetter.application

import android.app.AlertDialog
import android.view.LayoutInflater
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
    private val database by lazy { FirebaseDatabase.getInstance().reference }
    private var selectedDate: String? = null
    private var selectedHospital: String? = null
    private var selectedCityCode: String? = null
    private var selectedDistrictCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCalendar()

        // 🔹 검색 버튼 클릭 시 팝업 실행
        binding.btnSearch.setOnClickListener {
            showSearchPopup()
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

    // ✅ 병원 검색 팝업 표시
    private fun showSearchPopup() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_search_hospital, null)
        val citySpinner = dialogView.findViewById<Spinner>(R.id.spinnerCity)
        val districtSpinner = dialogView.findViewById<Spinner>(R.id.spinnerDistrict)
        val etSearch = dialogView.findViewById<EditText>(R.id.etHospitalSearch)
        val btnSearchHospital = dialogView.findViewById<Button>(R.id.btnSearchHospital)

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        builder.setTitle("병원 검색")
        val dialog = builder.create()

        loadCitySpinner(citySpinner, districtSpinner)

        // 🔹 병원 검색 버튼 클릭 시 실행
        btnSearchHospital.setOnClickListener {
            val searchQuery = etSearch.text.toString().trim()
            if (searchQuery.isNotEmpty()) {
                searchHospitals(searchQuery, citySpinner, districtSpinner, dialog)
            } else {
                Toast.makeText(this, "검색어를 입력하세요", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    // ✅ Firebase에서 시도(광역시/도) 목록 가져오기
    private fun loadCitySpinner(citySpinner: Spinner, districtSpinner: Spinner) {
        val cityRef = database.child("hospitals")

        cityRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cityMap = mutableMapOf<String, String>()
                val cityNames = mutableListOf<String>()

                for (child in snapshot.children) {
                    val cityCode = child.key ?: continue
                    cityMap[cityCode] = cityCode
                    cityNames.add(cityCode)
                }

                val cityAdapter = ArrayAdapter(this@AddScheduleActivity, android.R.layout.simple_spinner_dropdown_item, cityNames)
                citySpinner.adapter = cityAdapter

                citySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        selectedCityCode = cityMap[cityNames[position]]
                        selectedCityCode?.let { loadDistrictSpinner(it, districtSpinner) }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // ✅ Firebase에서 시군구 목록 가져오기
    private fun loadDistrictSpinner(cityCode: String, districtSpinner: Spinner) {
        val districtRef = database.child("hospitals").child(cityCode)

        districtRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val districtMap = mutableMapOf<String, String>()
                val districtNames = mutableListOf<String>()

                for (child in snapshot.children) {
                    val districtCode = child.key ?: continue
                    districtMap[districtCode] = districtCode
                    districtNames.add(districtCode)
                }

                val districtAdapter = ArrayAdapter(this@AddScheduleActivity, android.R.layout.simple_spinner_dropdown_item, districtNames)
                districtSpinner.adapter = districtAdapter

                districtSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        selectedDistrictCode = districtMap[districtNames[position]]
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // ✅ 병원 검색 기능
    private fun searchHospitals(query: String, citySpinner: Spinner, districtSpinner: Spinner, dialog: AlertDialog) {
        val hospitalRef = database.child("hospitals").child(selectedCityCode!!).child(selectedDistrictCode!!)
        val hospitalList = mutableListOf<String>()
        val hospitalMap = mutableMapOf<String, String>()

        hospitalRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val hospitalName = child.child("name").getValue(String::class.java)
                    val hospitalAddress = child.child("address").getValue(String::class.java)

                    if (!hospitalName.isNullOrEmpty() && hospitalName.contains(query, ignoreCase = true)) {
                        hospitalList.add(hospitalName)
                        hospitalMap[hospitalName] = hospitalAddress ?: "주소 없음"
                    }
                }

                if (hospitalList.isNotEmpty()) {
                    showHospitalSelectionDialog(hospitalList, hospitalMap, dialog)
                } else {
                    Toast.makeText(this@AddScheduleActivity, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // ✅ 병원 선택 다이얼로그
    private fun showHospitalSelectionDialog(hospitalList: List<String>, hospitalMap: Map<String, String>, parentDialog: AlertDialog) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("병원 선택")

        val hospitalArray = hospitalList.toTypedArray()
        builder.setItems(hospitalArray) { _, which ->
            selectedHospital = hospitalArray[which]
            binding.etSearch.setText(selectedHospital)
            binding.tvEditedSchedule.text = "선택된 병원: $selectedHospital\n주소: ${hospitalMap[selectedHospital]}"
            parentDialog.dismiss() // 부모 팝업 닫기
        }

        builder.setNegativeButton("취소", null)
        builder.show()
    }
}
