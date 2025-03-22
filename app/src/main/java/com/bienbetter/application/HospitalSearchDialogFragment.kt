package com.bienbetter.application

import android.app.AlertDialog
import android.app.Dialog
import android.view.LayoutInflater
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.google.firebase.database.*

class HospitalSearchDialogFragment(
    private val onHospitalSelected: (String) -> Unit
) : DialogFragment() {

    private var selectedCityCode: String? = null
    private var selectedDistrictCode: String? = null
    private val database by lazy { FirebaseDatabase.getInstance().reference }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_search_hospital, null)

        val citySpinner = view.findViewById<Spinner>(R.id.spinnerCity)
        val districtSpinner = view.findViewById<Spinner>(R.id.spinnerDistrict)
        val etSearch = view.findViewById<EditText>(R.id.etHospitalSearch)
        val btnSearch = view.findViewById<Button>(R.id.btnSearchHospital)
        val btnClose = view.findViewById<Button>(R.id.btnCloseDialog)  // 닫기 버튼

        builder.setView(view)

        loadCitySpinner(citySpinner, districtSpinner)

        btnSearch.setOnClickListener {
            val query = etSearch.text.toString().trim()
            if (query.isNotEmpty()) {
                if (selectedCityCode != null && selectedDistrictCode != null) {
                    searchHospitals(query)
                } else {
                    Toast.makeText(requireContext(), "시도/시군구를 먼저 선택해주세요", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "검색어를 입력하세요", Toast.LENGTH_SHORT).show()
            }
        }

        btnClose.setOnClickListener {
            dismiss()
        }

        return builder.create()
    }

    private fun loadCitySpinner(citySpinner: Spinner, districtSpinner: Spinner) {
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

        database.child("hospitals").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val cityCode = child.key ?: continue
                    val cityName = cityCodeToNameMap[cityCode] ?: continue
                    cityNames.add(cityName)
                    nameToCodeMap[cityName] = cityCode
                }

                val cityAdapter  = ArrayAdapter(requireContext(), R.layout.spinner_item, cityNames)
                cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                citySpinner.adapter = cityAdapter

                if (cityNames.isNotEmpty()) {
                    val defaultCity = cityNames[0]
                    selectedCityCode = nameToCodeMap[defaultCity]
                    selectedCityCode?.let { loadDistrictSpinner(it, districtSpinner) }
                }

                citySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        val cityName = cityNames[position]
                        selectedCityCode = nameToCodeMap[cityName]
                        selectedCityCode?.let { loadDistrictSpinner(it, districtSpinner) }
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

                val districtAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item, districtNameList)
                districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                districtSpinner.adapter = districtAdapter

                districtSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        selectedDistrictCode = districtCodeList[position]
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun searchHospitals(query: String) {
        val ref = database.child("hospitals").child(selectedCityCode!!).child(selectedDistrictCode!!)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<String>()
                for (child in snapshot.children) {
                    val name = child.child("name").getValue(String::class.java) ?: continue
                    if (name.contains(query)) list.add(name)
                }

                if (list.isNotEmpty()) {
                    showSelectionDialog(list)
                } else {
                    Toast.makeText(requireContext(), "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showSelectionDialog(list: List<String>) {
        AlertDialog.Builder(requireContext())
            .setTitle("병원 선택")
            .setItems(list.toTypedArray()) { _, which ->
                onHospitalSelected(list[which])
                dismiss()
            }
            .setNegativeButton("취소", null)
            .show()
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
            Pair("41", "111") to "수원시 장안구",
            Pair("41", "113") to "수원시 권선구",
            Pair("41", "115") to "수원시 팔달구",
            Pair("41", "117") to "수원시 영통구",
            Pair("41", "131") to "성남시 수정구",
            Pair("41", "133") to "성남시 중원구",
            Pair("41", "135") to "성남시 분당구",
            Pair("41", "150") to "의정부시",
            Pair("41", "171") to "안양시 만안구",
            Pair("41", "173") to "안양시 동안구",
            Pair("41", "192") to "부천시 원미구",
            Pair("41", "194") to "부천시 소사구",
            Pair("41", "196") to "부천시 오정구",
            Pair("41", "210") to "광명시",
            Pair("41", "220") to "평택시",
            Pair("41", "250") to "동두천시",
            Pair("41", "271") to "안산시 상록구",
            Pair("41", "273") to "안산시 단원구",
            Pair("41", "281") to "고양시 덕양구",
            Pair("41", "285") to "고양시 일산동구",
            Pair("41", "287") to "고양시 일산서구",
            Pair("41", "290") to "과천시",
            Pair("41", "310") to "구리시",
            Pair("41", "360") to "남양주시",
            Pair("41", "370") to "오산시",
            Pair("41", "390") to "시흥시",
            Pair("41", "430") to "의왕시",
            Pair("41", "410") to "군포시",
            Pair("41", "461") to "용인시 처인구",
            Pair("41", "463") to "용인시 기흥구",
            Pair("41", "465") to "용인시 수지구",
            Pair("41", "450") to "하남시",
            Pair("41", "480") to "파주시",
            Pair("41", "500") to "이천시",
            Pair("41", "550") to "안성시",
            Pair("41", "570") to "김포시",
            Pair("41", "590") to "화성시",
            Pair("41", "610") to "광주시",
            Pair("41", "630") to "양주시",
            Pair("41", "650") to "포천시",
            Pair("41", "670") to "여주시",
            Pair("41", "800") to "연천군",
            Pair("41", "820") to "가평군",
            Pair("41", "830") to "양평군",

            // ✅ 충청북도 (43)
            Pair("43", "111") to "청주시 상당구",
            Pair("43", "112") to "청주시 서원구",
            Pair("43", "113") to "청주시 흥덕구",
            Pair("43", "114") to "청주시 청원구",
            Pair("43", "130") to "충주시",
            Pair("43", "150") to "제천시",
            Pair("43", "720") to "보은군",
            Pair("43", "730") to "옥천군",
            Pair("43", "740") to "영동군",
            Pair("43", "745") to "증평군",
            Pair("43", "750") to "진천군",
            Pair("43", "760") to "괴산군",
            Pair("43", "770") to "음성군",
            Pair("43", "800") to "단양군",

            // ✅ 충청남도 (시도코드: 44)
            Pair("44", "131") to "천안시 동남구",
            Pair("44", "133") to "천안시 서북구",
            Pair("44", "150") to "공주시",
            Pair("44", "180") to "보령시",
            Pair("44", "200") to "아산시",
            Pair("44", "210") to "서산시",
            Pair("44", "230") to "논산시",
            Pair("44", "250") to "계룡시",
            Pair("44", "270") to "당진시",
            Pair("44", "710") to "금산군",
            Pair("44", "760") to "부여군",
            Pair("44", "770") to "서천군",
            Pair("44", "790") to "청양군",
            Pair("44", "800") to "홍성군",
            Pair("44", "810") to "예산군",
            Pair("44", "825") to "태안군",

            // ✅ 전라남도 (시도코드: 46)
            Pair("46", "110") to "목포시",
            Pair("46", "130") to "여수시",
            Pair("46", "150") to "순천시",
            Pair("46", "170") to "나주시",
            Pair("46", "230") to "광양시",
            Pair("46", "710") to "담양군",
            Pair("46", "720") to "곡성군",
            Pair("46", "730") to "구례군",
            Pair("46", "770") to "고흥군",
            Pair("46", "780") to "보성군",
            Pair("46", "790") to "화순군",
            Pair("46", "800") to "장흥군",
            Pair("46", "810") to "강진군",
            Pair("46", "820") to "해남군",
            Pair("46", "830") to "영암군",
            Pair("46", "840") to "무안군",
            Pair("46", "860") to "함평군",
            Pair("46", "870") to "영광군",
            Pair("46", "880") to "장성군",
            Pair("46", "890") to "완도군",
            Pair("46", "900") to "진도군",
            Pair("46", "910") to "신안군",

            // ✅ 경상북도 (시도코드: 47)
            Pair("47", "111") to "포항시 남구",
            Pair("47", "113") to "포항시 북구",
            Pair("47", "130") to "경주시",
            Pair("47", "150") to "김천시",
            Pair("47", "170") to "안동시",
            Pair("47", "190") to "구미시",
            Pair("47", "210") to "영주시",
            Pair("47", "230") to "영천시",
            Pair("47", "250") to "상주시",
            Pair("47", "280") to "문경시",
            Pair("47", "290") to "경산시",
            Pair("47", "710") to "군위군",
            Pair("47", "730") to "의성군",
            Pair("47", "750") to "청송군",
            Pair("47", "760") to "영양군",
            Pair("47", "770") to "영덕군",
            Pair("47", "820") to "청도군",
            Pair("47", "830") to "고령군",
            Pair("47", "840") to "성주군",
            Pair("47", "850") to "칠곡군",
            Pair("47", "900") to "예천군",
            Pair("47", "920") to "봉화군",
            Pair("47", "930") to "울진군",
            Pair("47", "940") to "울릉군",

            // ✅ 경상남도 (시도코드: 48)
            Pair("48", "121") to "창원시 의창구",
            Pair("48", "123") to "창원시 성산구",
            Pair("48", "125") to "창원시 마산합포구",
            Pair("48", "127") to "창원시 마산회원구",
            Pair("48", "129") to "창원시 진해구",
            Pair("48", "170") to "진주시",
            Pair("48", "220") to "통영시",
            Pair("48", "240") to "사천시",
            Pair("48", "250") to "김해시",
            Pair("48", "270") to "밀양시",
            Pair("48", "310") to "거제시",
            Pair("48", "330") to "양산시",
            Pair("48", "720") to "의령군",
            Pair("48", "730") to "함안군",
            Pair("48", "740") to "창녕군",
            Pair("48", "820") to "고성군",
            Pair("48", "840") to "남해군",
            Pair("48", "850") to "하동군",
            Pair("48", "860") to "산청군",
            Pair("48", "870") to "함양군",
            Pair("48", "880") to "거창군",
            Pair("48", "890") to "합천군",

            // ✅ 제주특별자치도 (시도코드: 50)
            Pair("50", "110") to "제주시",
            Pair("50", "130") to "서귀포시"

        )
        return districtCodeMap[Pair(cityCode, districtCode)] ?: districtCode
    }
}