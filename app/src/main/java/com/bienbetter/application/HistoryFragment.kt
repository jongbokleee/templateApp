package com.bienbetter.application

import android.content.Context
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ 기록 데이터 불러오기
        loadHistoryData()

        // ✅ RecyclerView 설정
        historyAdapter = HistoryAdapter(historyList)
        binding.recyclerViewHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewHistory.adapter = historyAdapter
    }

    override fun onResume() {
        super.onResume()
        loadHistoryData() // ✅ 새로고침 시 기록 다시 불러오기
    }

    // 📌 기록 데이터 로드 (기본값 + 저장된 일정 불러오기)
    private fun loadHistoryData() {
        val sharedPreferences = requireContext().getSharedPreferences("검진기록", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear() // ✅ SharedPreferences 데이터 초기화
        editor.apply()

        val savedHistory = sharedPreferences.getStringSet("historyList", setOf()) ?: setOf()

        historyList.clear() // ✅ 기존 데이터 초기화 (중복 방지)

        // 🔹 1️⃣ SharedPreferences 데이터 추가
        for (history in savedHistory) {
            historyList.add(HistoryItem(history))
        }

        // 🔹 2️⃣ Firebase 데이터 추가
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance().reference.child("schedules").child(userId)

        database.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                for (child in snapshot.children) {
                    val hospital = child.child("hospital").value as? String ?: "병원 정보 없음"
                    val date = child.child("date").value as? String ?: "날짜 정보 없음"

                    val historyText = "$date - $hospital"
                    if (!historyList.contains(HistoryItem(historyText))) { // ✅ 중복 데이터 방지
                        historyList.add(HistoryItem(historyText))
                    }
                }

                // ✅ 최신 날짜가 먼저 나오도록 정렬 (내림차순)
                historyList.sortByDescending { parseDate(it.text) }

                historyAdapter.notifyDataSetChanged() // ✅ RecyclerView 갱신
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "기록을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
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
