package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapter.HomeSectionAdapter
import com.example.myapplication.databinding.ActivityHomeBinding
import com.example.myapplication.model.HomeSection

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var homeSectionAdapter: HomeSectionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewBinding 초기화
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 홈 화면 Mock 데이터 로드
        val newSchedule = intent.getStringExtra("newSchedule")
        val mockData = loadMockData(newSchedule)

        // RecyclerView 설정
        homeSectionAdapter = HomeSectionAdapter(mockData) { sectionTitle ->
            when (sectionTitle) {
                "📅 다가오는 건강검진 일정" -> navigateToScheduleTab()
                "📊 마지막 검진 기록" -> navigateToHistoryTab() // Move to History tab
            }
        }

        binding.recyclerViewHome.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewHome.adapter = homeSectionAdapter

        // 검진 일정 추가 버튼 클릭 시 AddScheduleActivity로 이동
        binding.btnAddSchedule.setOnClickListener {
            val intent = Intent(this, AddScheduleActivity::class.java)
            startActivity(intent)
        }


        // 하단 내비게이션 클릭 리스너 설정
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true // 현재 화면 유지
                R.id.nav_calendar -> {
                    startActivity(Intent(this, CalendarActivity::class.java))
                    true
                }
                R.id.nav_hospital -> {
                    startActivity(Intent(this, HospitalSearchActivity::class.java))
                    true
                }
                R.id.nav_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    // 📌 일정 탭으로 이동하는 함수
    private fun navigateToScheduleTab() {
        binding.bottomNavigation.selectedItemId = R.id.nav_calendar
        startActivity(Intent(this, CalendarActivity::class.java))
    }
    // 📌 기록 탭으로 이동하는 함수
    private fun navigateToHistoryTab() {
        binding.bottomNavigation.selectedItemId = R.id.nav_history
        startActivity(Intent(this, HistoryActivity::class.java))
    }


    // 📌 Mock 데이터 생성 (새 일정 추가 포함)
    private fun loadMockData(newSchedule: String?): List<HomeSection> {
        val sharedPreferences = getSharedPreferences("검진기록", Context.MODE_PRIVATE)
        val historySet = sharedPreferences.getStringSet("historyList", setOf())?.toMutableSet() ?: mutableSetOf()

        // 새 일정이 있으면 추가
        if (!newSchedule.isNullOrEmpty()) {
            historySet.add(newSchedule)
            // 저장된 일정 업데이트
            sharedPreferences.edit().putStringSet("historyList", historySet).apply()
        }

        // 일정들을 날짜 순으로 정렬
        val sortedSchedules = historySet.sortedBy { extractDate(it) }

        val upcomingCheckup = sortedSchedules.lastOrNull() ?: "현재 예정된 건강검진 일정 없음"
        val lastCheckup = sortedSchedules.dropLast(1).lastOrNull() ?: "이전 건강검진 기록 없음"

        return listOf(
            HomeSection("📅 다가오는 건강검진 일정", listOf(upcomingCheckup)),
            HomeSection("🔔 검진 마감일 알림", listOf("⏳ 이달 말까지 검진을 받아야 합니다.")),
            HomeSection("📊 마지막 검진 기록", listOf(lastCheckup))
        )
    }

    // 📌 날짜를 추출하는 함수 (문자열에서 날짜 부분만 가져오기)
    private fun extractDate(schedule: String): String {
        return schedule.split("|").last().trim() // 예: "건강검진 | 서울 중앙병원 | 2025-03-05" → "2025-03-05"
    }
}
