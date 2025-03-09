package com.bienbetter.application

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bienbetter.application.adapter.ScheduleAdapter
import com.bienbetter.application.databinding.FragmentHomeBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var scheduleAdapter: ScheduleAdapter
    private val scheduleList = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ Firebase 초기화
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference.child("schedules")

        // ✅ 현재 로그인 상태 확인 후 UI 업데이트
        updateUI(auth.currentUser != null)

        // ✅ RecyclerView 설정
        setupRecyclerView()

        // ✅ Firebase에서 일정 데이터 불러오기
        loadSchedulesFromFirebase()

        // ✅ 로그인 여부 확인 → 검진 일정 추가 클릭 시 로그인 필요
        binding.homeBtnAddSchedule.setOnClickListener {
            if (auth.currentUser != null) {
                startActivity(Intent(requireContext(), AddScheduleActivity::class.java))
            } else {
                startActivity(Intent(requireContext(), LoginActivity::class.java))
            }
        }

        // ✅ 로그아웃 버튼 클릭 시 로그아웃 처리
        binding.btnLogout.setOnClickListener {
            logout()
        }
    }

    // ✅ Firebase에서 일정 데이터 불러오기
    private fun loadSchedulesFromFirebase() {
        val userId = auth.currentUser?.uid ?: return
        database.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                scheduleList.clear()

                var upcomingCheckup = "건강검진 일정 없음"
                var deadlineReminder = "검진 마감일 없음"
                var lastCheckup = "마지막 검진 기록 없음"

                for (child in snapshot.children) {
                    val hospital = child.child("hospital").getValue(String::class.java) ?: "알 수 없음"
                    val date = child.child("date").getValue(String::class.java) ?: "날짜 없음"
                    val scheduleText = "$hospital | $date"
                    scheduleList.add(scheduleText)

                    // 최신 일정 찾기
                    val currentDate = System.currentTimeMillis()
                    val scheduleTime = date.toLongOrNull() ?: currentDate

                    if (scheduleTime >= currentDate) {
                        upcomingCheckup = "✔ 다가오는 건강검진: $hospital - $date"
                        deadlineReminder = "⏳ 검진 마감일: $date 전까지 검진 필요"
                    } else {
                        lastCheckup = "📌 마지막 검진: $hospital - $date"
                    }
                }

                // 🔹 UI에 데이터 반영
                binding.tvUpcomingCheckup.text = upcomingCheckup

                // 🔹 RecyclerView 업데이트
                scheduleAdapter.updateList(scheduleList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "데이터 불러오기 실패: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // ✅ RecyclerView 설정
    private fun setupRecyclerView() {
        scheduleAdapter = ScheduleAdapter(scheduleList)
        binding.recyclerViewSchedules.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewSchedules.adapter = scheduleAdapter
    }



    // ✅ 로그인 상태에 따라 UI 업데이트
    private fun updateUI(isLoggedIn: Boolean) {
        binding.tvLoginRequired.visibility = if (isLoggedIn) View.GONE else View.VISIBLE
        binding.btnLogout.visibility = if (isLoggedIn) View.VISIBLE else View.GONE
    }
    // ✅ 로그아웃 처리
    private fun logout() {
        auth.signOut() // ✅ Firebase 로그아웃
        GoogleSignIn.getClient(requireContext(), GoogleSignInOptions.DEFAULT_SIGN_IN)
            .signOut()
            .addOnCompleteListener {
                updateUI(false)  // ✅ 로그아웃 후 UI 업데이트
                requireActivity().recreate() // ✅ UI 강제 새로고침
                Toast.makeText(requireContext(), "로그아웃되었습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        private const val REQUEST_SIGN_IN = 1001
    }
}
