package com.bienbetter.application

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bienbetter.application.adapter.HospitalAdapter
import com.bienbetter.application.api.RetrofitClient
import com.bienbetter.application.databinding.FragmentHospitalSearchBinding
import com.bienbetter.application.model.Hospital
import com.bienbetter.application.model.HospitalResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HospitalSearchFragment : Fragment() {

    private lateinit var binding: FragmentHospitalSearchBinding
    private lateinit var hospitalAdapter: HospitalAdapter
    private var hospitalList = mutableListOf<Hospital>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHospitalSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("FRAGMENT_DEBUG", "HospitalSearchFragment created")  // ✅ Fragment 생성 확인
        hospitalAdapter = HospitalAdapter(hospitalList)
        binding.recyclerViewHospitals.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewHospitals.adapter = hospitalAdapter

        // ✅ 스피너 어댑터 설정
        val spinnerItems = resources.getStringArray(R.array.search_options)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, spinnerItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerSearchType.adapter = adapter // 스피너에 어댑터 적용

        // ✅ Fragment가 처음 열릴 때 기본 병원 리스트 불러오기
        fetchHospitals("", "")

        // ✅ 검색 버튼 클릭 시 검색 실행
        binding.btnSearch.setOnClickListener {
            val searchQuery = binding.etSearch.text.toString().trim()
            val searchType = binding.spinnerSearchType.selectedItem.toString() // 스피너 선택 값 가져오기

            when {
                searchQuery.isNotEmpty() -> { // ✅ 검색어가 있을 때
                    if (searchType == "병원명") {
                        Log.d("API_DEBUG", "병원명 검색 실행: $searchQuery")
                        fetchHospitals(hospitalName = searchQuery, location = "")
                    } else {
                        Log.d("API_DEBUG", "지역 검색 실행: $searchQuery")
                        fetchHospitals(hospitalName = "", location = searchQuery)
                    }
                }

                else -> { // ✅ 검색어가 비어있을 때 전체 병원 리스트 가져오기
                    Log.d("API_DEBUG", "검색어가 비어 있음, 전체 병원 리스트 조회")
                    fetchHospitals("", "") // 병원명과 지역 모두 빈 문자열로 API 요청
                }
            }
        }
    }

    private fun fetchHospitals(hospitalName: String, location: String) {
        Log.d("API_DEBUG", "Fetching hospitals with hospitalName: $hospitalName, location: $location")

        val call = RetrofitClient.instance.getHospitals(
            serviceKey = "",
            hmcNm = hospitalName, // 병원명 검색어
            locAddr = location, // 지역명 검색어
            siDoCd = 11,
            siGunGuCd = 590
        )

        call.enqueue(object : Callback<HospitalResponse> {
            override fun onResponse(
                call: Call<HospitalResponse>,
                response: Response<HospitalResponse>
            ) {
                Log.d("API_DEBUG", "Response code: ${response.code()}")

                if (response.isSuccessful) {
                    val hospitalResponse = response.body()

                    if (hospitalResponse == null || hospitalResponse.response?.body?.items?.item.isNullOrEmpty()) {
                        Log.e("API_ERROR", "No hospital data found")
                        binding.tvNoResults.visibility = View.VISIBLE // ✅ "검색 결과가 없습니다." 표시
                        binding.recyclerViewHospitals.visibility = View.GONE // ✅ 리스트 숨기기
                        return
                    }

                    val hospitals = hospitalResponse.response?.body?.items?.item?.map {
                        Hospital(
                            it.hmcNm ?: "이름 없음",
                            it.locAddr ?: "주소 없음",
                            it.hmcTelNo ?: "전화번호 없음"
                        )
                    } ?: emptyList()

                    Log.d("API_DEBUG", "Hospitals list size: ${hospitals.size}")
                    hospitalAdapter.updateList(hospitals)

                    // ✅ 데이터가 있으면 "검색 결과 없음" 텍스트를 숨기고 RecyclerView 표시
                    binding.tvNoResults.visibility = View.GONE
                    binding.recyclerViewHospitals.visibility = View.VISIBLE
                } else {
                    Log.e("API_ERROR", "Response failed: ${response.errorBody()?.string()}")
                    binding.tvNoResults.visibility = View.VISIBLE // ✅ 실패 시 "검색 결과 없음" 표시
                    binding.recyclerViewHospitals.visibility = View.GONE // ✅ 리스트 숨기기
                    hospitalAdapter.updateList(emptyList())
                }
            }

            override fun onFailure(call: Call<HospitalResponse>, t: Throwable) {
                Log.e("API_ERROR", "Network request failed", t)
                binding.tvNoResults.visibility = View.VISIBLE // ✅ 네트워크 오류 시 "검색 결과 없음" 표시
                binding.recyclerViewHospitals.visibility = View.GONE // ✅ 리스트 숨기기
                hospitalAdapter.updateList(emptyList())
            }
        })
    }
}