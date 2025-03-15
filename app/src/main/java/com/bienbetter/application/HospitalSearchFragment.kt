package com.bienbetter.application

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bienbetter.application.adapter.HospitalAdapter
import com.bienbetter.application.databinding.FragmentHospitalSearchBinding
import com.bienbetter.application.model.Hospital
import com.bienbetter.application.model.HospitalResponse
import com.bienbetter.application.model.ResponseBody
import com.google.gson.Gson
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

        // ✅ Fragment가 처음 열릴 때 기본 병원 리스트 불러오기
        fetchHospitals("")

        // ✅ 검색 버튼 클릭 시 검색 실행
        binding.btnSearch.setOnClickListener {
            val searchQuery = binding.etSearch.text.toString().trim()

            if (searchQuery.isNotEmpty()) {
                Log.d("API_DEBUG", "Search button clicked, query: $searchQuery")
                fetchHospitals(searchQuery)
            } else {
                Log.d("API_DEBUG", "검색어가 비어있음")
            }
        }
    }

    private fun fetchHospitals(searchQuery: String) {
        Log.d("API_DEBUG", "Fetching hospitals with query: $searchQuery")

        val call = RetrofitClient.instance.getHospitals(
            serviceKey = "",  // ✅ 반드시 URL 인코딩된 값 사용
            hmcNm = searchQuery,
            siDoCd = 11,
            siGunGuCd = 590
        )
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.d("API_DEBUG", "Response code: ${response.code()}")

                val rawResponse = response.body()
                Log.d("API_DEBUG", "Raw Response: $rawResponse")  // ✅ 서버 응답 내용 출력

                if (response.isSuccessful) {
                    val rawResponse = response.body()
                    Log.d("API_DEBUG", "Raw Response: $rawResponse")  // ✅ 서버 응답 내용 출력
//
//                    val hospitals = response.body()?.response?.body?.items?.item?.map {
//                        Hospital(it.hmcNm, it.locAddr, it.hmcTelNo)
//                    } ?: listOf()
//
//                    Log.d("API_DEBUG", "Hospitals list size: ${hospitals.size}")
//                    hospitalAdapter.updateList(hospitals)
                } else {
                    Log.e("API_ERROR", "Response failed: ${response.errorBody()?.string()}")
                }
            }

//            override fun onFailure(call: Call<HospitalResponse>, t: Throwable) {
//                Log.e("API_ERROR", "Network request failed", t)
//            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("API_ERROR", "Network request failed", t)
            }
        })
    }
}
