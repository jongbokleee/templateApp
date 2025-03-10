package com.bienbetter.application

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bienbetter.application.databinding.FragmentSettingsBinding
import com.google.firebase.auth.*
import com.google.firebase.database.FirebaseDatabase

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Firebase 인증 객체 초기화
        firebaseAuth = FirebaseAuth.getInstance()

        // 푸시 알림 설정 스위치 변경 리스너
        binding.switchPushNotifications.setOnCheckedChangeListener { _, isChecked ->
            val message = if (isChecked) "푸시 알림이 활성화되었습니다." else "푸시 알림이 비활성화되었습니다."
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        // 계정 탈퇴 버튼 클릭 시 경고 메시지 표시
        binding.btnDeleteAccount.setOnClickListener {
            showDeleteAccountDialog()
        }
    }

    // 🔹 계정 탈퇴 확인 다이얼로그
    private fun showDeleteAccountDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("계정 탈퇴")
            .setMessage("정말 계정을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.")
            .setPositiveButton("탈퇴") { _, _ ->
                deleteUserDataAndAccount()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    // 🔹 **Firebase 데이터 삭제 + 계정 탈퇴**
    private fun deleteUserDataAndAccount() {
        val user = firebaseAuth.currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "사용자 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = user.uid
        val databaseRef = FirebaseDatabase.getInstance().reference

        // ✅ 1. `schedules/{userId}` 데이터 삭제
        databaseRef.child("schedules").child(userId).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // ✅ 2. `users/{userId}` 데이터 삭제
                    databaseRef.child("users").child(userId).removeValue()
                        .addOnCompleteListener { userTask ->
                            if (userTask.isSuccessful) {
                                // ✅ 3. 계정 삭제
                                reauthenticateAndDeleteUser(user)
                            } else {
                                Toast.makeText(requireContext(), "사용자 데이터 삭제 실패", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(requireContext(), "일정 삭제 실패", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // 🔹 **Google 계정 자동 재인증 후 삭제**
    private fun reauthenticateAndDeleteUser(user: FirebaseUser) {
        user.getIdToken(true)
            .addOnCompleteListener { tokenTask ->
                if (tokenTask.isSuccessful) {
                    val idToken = tokenTask.result?.token
                    if (!idToken.isNullOrEmpty()) {
                        val credential = GoogleAuthProvider.getCredential(idToken, null)
                        user.reauthenticate(credential)
                            .addOnCompleteListener { reauthTask ->
                                if (reauthTask.isSuccessful) {
                                    deleteUserAccount(user) // ✅ 인증 성공 시 계정 삭제
                                } else {
                                    Toast.makeText(requireContext(), "Google 로그인 재인증 실패", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(requireContext(), "ID 토큰 가져오기 실패", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "토큰 갱신 실패: ${tokenTask.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // 🔹 **Firebase 계정 삭제**
    private fun deleteUserAccount(user: FirebaseUser) {
        user.delete().addOnCompleteListener { deleteTask ->
            if (deleteTask.isSuccessful) {
                Toast.makeText(requireContext(), "계정이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                navigateToLogin()
            } else {
                Toast.makeText(requireContext(), "계정 삭제 실패: ${deleteTask.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 🔹 로그인 화면으로 이동
    private fun navigateToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }
}
