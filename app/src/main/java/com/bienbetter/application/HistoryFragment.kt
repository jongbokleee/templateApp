package com.bienbetter.application

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bienbetter.application.adapter.HistoryAdapter
import com.bienbetter.application.databinding.FragmentHistoryBinding
import com.bienbetter.application.model.HistoryItem

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


//    override fun onCreate(savedInstanceState: Bundle?) {
//        // 뒤로 가기 버튼 클릭 시 액티비티 종료
//        binding.backButton.setOnClickListener {
//            finish()
//        }
//    }

    // 📌 기록 데이터 로드 (기본값 + 저장된 일정 불러오기)
    private fun loadHistoryData() {
        val sharedPreferences = requireContext().getSharedPreferences("검진기록", Context.MODE_PRIVATE)
        val savedHistory = sharedPreferences.getStringSet("historyList", setOf()) ?: setOf()

        historyList.clear() // ✅ 기존 데이터 초기화 (중복 방지)

        // 저장된 일정 추가
        for (history in savedHistory) {
            historyList.add(HistoryItem(history))
        }
    }
}