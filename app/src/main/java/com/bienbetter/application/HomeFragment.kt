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
import com.bienbetter.application.model.ScheduleItem
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var scheduleAdapter: ScheduleAdapter
    private val scheduleList = mutableListOf<ScheduleItem>()

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

        // ✅ 로그인 상태 확인 후 UI 업데이트
        updateUI(auth.currentUser != null)

        // ✅ RecyclerView 설정
        setupRecyclerView()

        // ✅ Firebase에서 일정 데이터 불러오기
        loadSchedulesFromFirebase()

        // ✅ 로그인 여부 확인 후 검진 일정 추가
        binding.homeBtnAddSchedule.setOnClickListener {
            if (auth.currentUser != null) {
                startActivity(Intent(requireContext(), AddScheduleActivity::class.java))
            } else {
                startActivity(Intent(requireContext(), LoginActivity::class.java))
            }
        }

        // ✅ "건강검진 받을 병원 알아보기" 버튼 클릭 시 병원 검색 화면으로 이동
        binding.homeBtnFindHospital.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.putExtra("navigateTo", "HospitalSearchFragment")
            startActivity(intent)
        }

        // ✅ 로그아웃 버튼 클릭 시 로그아웃 처리
        binding.btnLogout.setOnClickListener {
            logout()
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI(auth.currentUser != null)
        loadSchedulesFromFirebase() // ✅ 화면 복귀 시 데이터 새로고침
    }

    // ✅ Firebase에서 일정 데이터 불러오기
    private fun loadSchedulesFromFirebase() {
        val userId = auth.currentUser?.uid ?: return

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayDate = sdf.format(Date()) // 📌 오늘 날짜 (예: 2025-03-16)

        val databaseRef = database.child(userId)
            .orderByChild("date")
            .startAt(todayDate)

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tempList = mutableListOf<ScheduleItem>()
                for (child in snapshot.children) {
                    val hospital = child.child("hospital").getValue(String::class.java) ?: "알 수 없음"
                    val dateStr = child.child("date").getValue(String::class.java) ?: "날짜 없음"

                    tempList.add(ScheduleItem.create(hospital, dateStr))
                }

                scheduleList.clear()
                scheduleList.addAll(tempList.sortedBy { it.date }.take(3))

                // ✅ 로그인 상태일 때 "추가된 일정 없음" 문구 숨기기
                if (scheduleList.isNotEmpty()) {
                    binding.tvNoSchedules.visibility = View.GONE
                    binding.recyclerViewSchedules.visibility = View.VISIBLE
                } else {
                    binding.tvNoSchedules.visibility = View.VISIBLE
                    binding.recyclerViewSchedules.visibility = View.GONE
                }

                // 🔹 RecyclerView 갱신
                scheduleAdapter.updateList(scheduleList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "데이터 불러오기 실패: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // ✅ RecyclerView 설정
    private fun setupRecyclerView() {
        scheduleAdapter = ScheduleAdapter(requireActivity(), scheduleList)
        binding.recyclerViewSchedules.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewSchedules.adapter = scheduleAdapter
    }

    // ✅ 로그인 상태에 따라 UI 업데이트
    private fun updateUI(isLoggedIn: Boolean) {
        binding.tvLoginRequired.visibility = if (isLoggedIn) View.GONE else View.VISIBLE
        binding.btnLogout.visibility = if (isLoggedIn) View.VISIBLE else View.GONE
        binding.tvScheduleLimitNotice.visibility = if (isLoggedIn) View.VISIBLE else View.GONE
    }

    // ✅ 로그아웃 처리
    private fun logout() {
        auth.signOut()
        GoogleSignIn.getClient(requireContext(), GoogleSignInOptions.DEFAULT_SIGN_IN)
            .signOut()
            .addOnCompleteListener {
                updateUI(false)
                requireActivity().recreate()
                Toast.makeText(requireContext(), "로그아웃되었습니다.", Toast.LENGTH_SHORT).show()
            }
    }
}
