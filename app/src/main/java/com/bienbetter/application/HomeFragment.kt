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
        val databaseRef = database.child(userId)

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                scheduleList.clear()

                var upcomingCheckup: ScheduleItem? = null
                var lastCheckup: ScheduleItem? = null
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val currentDate = System.currentTimeMillis()

                for (child in snapshot.children) {
                    val hospital = child.child("hospital").getValue(String::class.java) ?: "알 수 없음"
                    val dateStr = child.child("date").getValue(String::class.java) ?: "날짜 없음"

                    val dateTimestamp = try {
                        dateFormat.parse(dateStr)?.time ?: 0L
                    } catch (e: Exception) {
                        0L
                    }

                    val scheduleItem = ScheduleItem(hospital, dateStr, dateTimestamp)
                    scheduleList.add(scheduleItem)

                    // 🔹 최신 일정 찾기
                    if (dateTimestamp >= currentDate) {
                        if (upcomingCheckup == null || dateTimestamp < upcomingCheckup.dateTimestamp) {
                            upcomingCheckup = scheduleItem
                        }
                    } else {
                        if (lastCheckup == null || dateTimestamp > lastCheckup.dateTimestamp) {
                            lastCheckup = scheduleItem
                        }
                    }
                }

                // 🔹 UI에 최신 일정 반영
                binding.tvUpcomingCheckup.text = upcomingCheckup?.let {
                    "✔ 다가오는 건강검진 일정: ${it.hospitalName} - ${it.date}"
                } ?: "📅 다가오는 건강검진 일정"

                binding.tvLoginRequired.visibility =
                    if (scheduleList.isEmpty()) View.VISIBLE else View.GONE

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
