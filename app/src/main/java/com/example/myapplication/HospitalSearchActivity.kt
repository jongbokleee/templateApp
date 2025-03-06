package com.example.myapplication

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapter.HospitalAdapter
import com.example.myapplication.databinding.ActivityHospitalSearchBinding
import com.example.myapplication.model.Hospital

class HospitalSearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHospitalSearchBinding
    private lateinit var hospitalAdapter: HospitalAdapter
    private var hospitalList = mutableListOf<Hospital>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewBinding 초기화
        binding = ActivityHospitalSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 병원 리스트 초기화
        loadHospitals()

        // RecyclerView 설정
        hospitalAdapter = HospitalAdapter(hospitalList)
        binding.recyclerViewHospitals.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewHospitals.adapter = hospitalAdapter

        // 검색 기능
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterHospitals(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // 병원 리스트 데이터
    private fun loadHospitals() {
        hospitalList.add(Hospital("서울 중앙병원", "서울 강남구 테헤란로 123", "02-123-4567"))
        hospitalList.add(Hospital("서울 건강검진센터", "서울 종로구 종로 45", "02-765-4321"))
        hospitalList.add(Hospital("부산 종합병원", "부산 해운대구 해운로 77", "051-987-6543"))
    }

    // 검색 필터링
    private fun filterHospitals(query: String) {
        val filteredList = hospitalList.filter {
            it.name.contains(query, ignoreCase = true) || it.address.contains(query, ignoreCase = true)
        }
        hospitalAdapter.updateList(filteredList)
    }
}
