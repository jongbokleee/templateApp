package com.example.myapplication

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapter.HistoryAdapter
import com.example.myapplication.databinding.ActivityHistoryBinding
import com.example.myapplication.model.HistoryItem

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var historyAdapter: HistoryAdapter
    private var historyList = mutableListOf<HistoryItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ ViewBinding 초기화
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ 기록 데이터 불러오기
        loadHistoryData()

        // ✅ RecyclerView 설정
        historyAdapter = HistoryAdapter(historyList)
        binding.recyclerViewHistory.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewHistory.adapter = historyAdapter

        // 뒤로 가기 버튼 클릭 시 액티비티 종료
        binding.backButton.setOnClickListener {
            finish()
        }
    }

    // 📌 기록 데이터 로드 (기본값 + 저장된 일정 불러오기)
    private fun loadHistoryData() {
        val sharedPreferences = getSharedPreferences("검진기록", Context.MODE_PRIVATE)
        val savedHistory = sharedPreferences.getStringSet("historyList", setOf()) ?: setOf()

        historyList.clear() // ✅ 기존 데이터 초기화 (중복 방지)

        // 저장된 일정 추가
        for (history in savedHistory) {
            historyList.add(HistoryItem(history))
        }
    }
}
