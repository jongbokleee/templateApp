package com.bienbetter.application

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bienbetter.application.adapter.HistoryAdapter
import com.bienbetter.application.databinding.FragmentHistoryBinding
import com.bienbetter.application.model.HistoryItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class HistoryFragment : Fragment() {

    private lateinit var binding: FragmentHistoryBinding
    private lateinit var historyAdapter: HistoryAdapter
    private var historyList = mutableListOf<HistoryItem>()
    private var filteredList = mutableListOf<HistoryItem>() // ✅ 검색된 데이터 리스트

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ RecyclerView 설정 (filteredList와 연결)
        historyAdapter = HistoryAdapter(filteredList)
        binding.recyclerViewHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewHistory.adapter = historyAdapter

        // ✅ 기록 데이터 불러오기
        loadHistoryData()

        // ✅ 검색 버튼 클릭 시 검색 실행
        binding.btnSearchHistory.setOnClickListener {
            val searchQuery = binding.etSearchHistory.text.toString().trim()
            filterHistory(searchQuery)
        }
    }

    override fun onResume() {
        super.onResume()
        loadHistoryData() // ✅ 새로고침 시 기록 다시 불러오기
    }

    // 📌 기록 데이터 로드 (Firebase에서 데이터 가져오기)
    private fun loadHistoryData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            // 비회원인 경우 처리
            filteredList.clear()
            historyAdapter.notifyDataSetChanged()

            binding.recyclerViewHistory.visibility = View.GONE
            binding.tvNoHistory.visibility = View.VISIBLE
            return
        }

        val database = FirebaseDatabase.getInstance().reference.child("schedules").child(userId)

        database.get().addOnSuccessListener { snapshot ->
            historyList.clear()
            filteredList.clear()

            if (snapshot.exists()) {
                for (child in snapshot.children) {
                    val hospital = child.child("hospital").value as? String ?: "병원 정보 없음"
                    val date = child.child("date").value as? String ?: "날짜 정보 없음"
                    val historyText = "$date - $hospital"
                    if (!historyList.contains(HistoryItem(historyText))) {
                        historyList.add(HistoryItem(historyText))
                    }
                }

                historyList.sortByDescending { parseDate(it.text) }

                filteredList.addAll(historyList)
                binding.recyclerViewHistory.visibility = View.VISIBLE
                binding.tvNoHistory.visibility = View.GONE
            } else {
                // 기록이 아예 없는 경우
                binding.recyclerViewHistory.visibility = View.GONE
                binding.tvNoHistory.visibility = View.VISIBLE
            }

            historyAdapter.notifyDataSetChanged()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "기록을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // 🔍 검색 버튼을 눌렀을 때만 검색 실행
    private fun filterHistory(query: String) {
        if (query.isEmpty()) {
            // ✅ 검색어가 비어 있으면 전체 리스트 보여줌
            filteredList.clear()
            filteredList.addAll(historyList)
        } else {
            // ✅ 검색어 포함된 항목만 필터링
            filteredList.clear()
            filteredList.addAll(historyList.filter { it.text.contains(query, ignoreCase = true) })
        }

        // ✅ 검색 결과가 없으면 "검색 결과 없음" 메시지 표시
        if (filteredList.isEmpty()) {
            binding.tvNoHistory.visibility = View.VISIBLE
            binding.recyclerViewHistory.visibility = View.GONE
        } else {
            binding.tvNoHistory.visibility = View.GONE
            binding.recyclerViewHistory.visibility = View.VISIBLE
        }

        historyAdapter.notifyDataSetChanged() // ✅ RecyclerView 갱신
    }

    // 🔹 날짜를 비교하기 위해 문자열을 Date 객체로 변환하는 함수
    private fun parseDate(dateString: String): Date {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            sdf.parse(dateString.split(" - ")[0]) ?: Date()
        } catch (e: Exception) {
            Date()
        }
    }
}
