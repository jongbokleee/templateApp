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
                val cityCodeToNameMap = mapOf(
                    "11" to "서울특별시", "26" to "부산광역시", "27" to "대구광역시",
                    "28" to "인천광역시", "29" to "광주광역시", "30" to "대전광역시",
                    "31" to "울산광역시", "36" to "세종특별자치시", "41" to "경기도",
                    "42" to "강원특별자치도", "43" to "충청북도", "44" to "충청남도",
                    "45" to "전북특별자치도", "46" to "전라남도", "47" to "경상북도",
                    "48" to "경상남도", "50" to "제주특별자치도"
                )

                val nameToCodeMap = mutableMapOf<String, String>()
                val cityNames = mutableListOf<String>()

                for (child in snapshot.children) {
                    val cityCode = child.key ?: continue
                    val cityName = cityCodeToNameMap[cityCode] ?: continue
                    cityNames.add(cityName)
                    nameToCodeMap[cityName] = cityCode
                }

                val cityAdapter = ArrayAdapter(this@AddScheduleActivity, android.R.layout.simple_spinner_dropdown_item, cityNames)
                citySpinner.adapter = cityAdapter

                // ✅ 스피너 기본값 설정 시 자동 district 로딩
                if (cityNames.isNotEmpty()) {
                    val defaultCity = cityNames[0]
                    selectedCityCode = nameToCodeMap[defaultCity]
                    selectedCityCode?.let { loadDistrictSpinner(it, districtSpinner) }
                }

                citySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        val selectedCityName = cityNames[position]
                        selectedCityCode = nameToCodeMap[selectedCityName]
                        selectedCityCode?.let { loadDistrictSpinner(it, districtSpinner) } // ✅ 이 부분이 핵심
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun loadDistrictSpinner(cityCode: String, districtSpinner: Spinner) {
        val districtRef = database.child("hospitals").child(cityCode)

        districtRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val districtCodeList = snapshot.children.mapNotNull { it.key }
                val districtNameList = districtCodeList.map { getDistrictName(cityCode, it) }

                setupDistrictSpinner(districtSpinner, districtNameList, districtCodeList, cityCode)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // 스피너 어댑터 설정 + 이벤트 리스너 바인딩
    private fun setupDistrictSpinner(
        spinner: Spinner,
        displayNames: List<String>,
        codeList: List<String>,
        cityCode: String
    ) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, displayNames)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedDistrictCode = codeList[position]
                selectedCityCode = cityCode
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
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

    private fun getDistrictName(cityCode: String, districtCode: String): String {
        val districtCodeMap = mapOf(

            // ✅ 서울특별시 (code: 11)
            Pair("11", "110") to "종로구",
            Pair("11", "140") to "중구",
            Pair("11", "170") to "용산구",
            Pair("11", "200") to "성동구",
            Pair("11", "215") to "광진구",
            Pair("11", "230") to "동대문구",
            Pair("11", "260") to "중랑구",
            Pair("11", "290") to "성북구",
            Pair("11", "305") to "강북구",
            Pair("11", "320") to "도봉구",
            Pair("11", "350") to "노원구",
            Pair("11", "380") to "은평구",
            Pair("11", "410") to "서대문구",
            Pair("11", "440") to "마포구",
            Pair("11", "470") to "양천구",
            Pair("11", "500") to "강서구",
            Pair("11", "530") to "구로구",
            Pair("11", "545") to "금천구",
            Pair("11", "560") to "영등포구",
            Pair("11", "590") to "동작구",
            Pair("11", "620") to "관악구",
            Pair("11", "650") to "서초구",
            Pair("11", "680") to "강남구",
            Pair("11", "710") to "송파구",
            Pair("11", "740") to "강동구",

            // ✅ 부산광역시 (code: 26)
            Pair("26", "110") to "중구",
            Pair("26", "140") to "서구",
            Pair("26", "170") to "동구",
            Pair("26", "200") to "영도구",
            Pair("26", "230") to "부산진구",
            Pair("26", "260") to "동래구",
            Pair("26", "290") to "남구",
            Pair("26", "320") to "북구",
            Pair("26", "350") to "해운대구",
            Pair("26", "380") to "사하구",
            Pair("26", "410") to "금정구",
            Pair("26", "440") to "강서구",
            Pair("26", "470") to "연제구",
            Pair("26", "500") to "수영구",
            Pair("26", "530") to "사상구",
            Pair("26", "710") to "기장군",

            // ✅ 대구광역시 (code: 27)
            Pair("27", "720") to "군위군",
            Pair("27", "200") to "남구",
            Pair("27", "290") to "달서구",
            Pair("27", "710") to "달성군",
            Pair("27", "140") to "동구",
            Pair("27", "230") to "북구",
            Pair("27", "170") to "서구",
            Pair("27", "260") to "수성구",
            Pair("27", "110") to "중구",

            // ✅ 인천광역시 (code: 28)
            Pair("28", "710") to "강화군",
            Pair("28", "245") to "계양구",
            Pair("28", "200") to "남동구",
            Pair("28", "140") to "동구",
            Pair("28", "177") to "미추홀구",
            Pair("28", "237") to "부평구",
            Pair("28", "260") to "서구",
            Pair("28", "185") to "연수구",
            Pair("28", "720") to "옹진군",
            Pair("28", "110") to "중구",

            // ✅ 광주광역시 (29)
            Pair("29", "110") to "동구",
            Pair("29", "140") to "서구",
            Pair("29", "155") to "남구",
            Pair("29", "170") to "북구",
            Pair("29", "200") to "광산구",

            // ✅ 대전광역시 (30)
            Pair("30", "110") to "동구",
            Pair("30", "140") to "중구",
            Pair("30", "170") to "서구",
            Pair("30", "200") to "유성구",
            Pair("30", "230") to "대덕구",

            // ✅ 울산광역시 (31)
            Pair("31", "110") to "중구",
            Pair("31", "140") to "남구",
            Pair("31", "170") to "동구",
            Pair("31", "200") to "북구",
            Pair("31", "710") to "울주군",

            // ✅ 세종특별자치시 (36)
            Pair("36", "110") to "세종시",

            // ✅ 경기도 (41)
            Pair("41", "110") to "수원시 장안구",
            Pair("41", "113") to "수원시 권선구",
            Pair("41", "115") to "수원시 팔달구",
            Pair("41", "117") to "수원시 영통구",
            Pair("41", "130") to "성남시 수정구",
            Pair("41", "131") to "성남시 중원구",
            Pair("41", "133") to "성남시 분당구",
            Pair("41", "150") to "의정부시",
            Pair("41", "170") to "안양시 만안구",
            Pair("41", "171") to "안양시 동안구",
            Pair("41", "190") to "부천시",
            Pair("41", "210") to "광명시",
            Pair("41", "220") to "평택시",
            Pair("41", "250") to "동두천시",
            Pair("41", "271") to "안산시 상록구",
            Pair("41", "273") to "안산시 단원구",
            Pair("41", "281") to "고양시 덕양구",
            Pair("41", "283") to "고양시 일산동구",
            Pair("41", "285") to "고양시 일산서구",
            Pair("41", "287") to "과천시",
            Pair("41", "290") to "구리시",
            Pair("41", "310") to "남양주시",
            Pair("41", "360") to "오산시",
            Pair("41", "370") to "시흥시",
            Pair("41", "390") to "군포시",
            Pair("41", "400") to "의왕시",
            Pair("41", "410") to "하남시",
            Pair("41", "430") to "용인시 처인구",
            Pair("41", "431") to "용인시 기흥구",
            Pair("41", "433") to "용인시 수지구",
            Pair("41", "450") to "파주시",
            Pair("41", "480") to "이천시",
            Pair("41", "500") to "안성시",
            Pair("41", "550") to "김포시",
            Pair("41", "570") to "화성시",
            Pair("41", "590") to "광주시",
            Pair("41", "610") to "양주시",
            Pair("41", "630") to "포천시",
            Pair("41", "650") to "여주시",
            Pair("41", "800") to "연천군",
            Pair("41", "820") to "가평군",
            Pair("41", "830") to "양평군",

            // ✅ 충청북도 (43)
            Pair("43", "110") to "청주시 상당구",
            Pair("43", "111") to "청주시 서원구",
            Pair("43", "112") to "청주시 흥덕구",
            Pair("43", "113") to "청주시 청원구",
            Pair("43", "130") to "충주시",
            Pair("43", "150") to "제천시",
            Pair("43", "720") to "보은군",
            Pair("43", "730") to "옥천군",
            Pair("43", "740") to "영동군",
            Pair("43", "750") to "진천군",
            Pair("43", "760") to "괴산군",
            Pair("43", "770") to "음성군",
            Pair("43", "800") to "단양군",
            Pair("43", "810") to "증평군"

            )
        return districtCodeMap[Pair(cityCode, districtCode)] ?: districtCode
    }
}
