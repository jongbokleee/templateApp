package com.bienbetter.application

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bienbetter.application.databinding.FragmentSettingsBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
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

    override fun onResume() {
        super.onResume()

        // 로그인 상태 다시 확인
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            binding.btnDeleteAccount.visibility = View.VISIBLE
        } else {
            binding.btnDeleteAccount.visibility = View.GONE
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Firebase 인증 객체 초기화
        firebaseAuth = FirebaseAuth.getInstance()

        // 🔹 로그인 상태 확인하여 계정 탈퇴 버튼 보이기/숨기기
        if (firebaseAuth.currentUser != null) {
            binding.btnDeleteAccount.visibility = View.VISIBLE
        } else {
            binding.btnDeleteAccount.visibility = View.GONE
        }

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
                                deleteUserAccount(user)
//                                reauthenticateAndDeleteUser(user)
                            } else {
                                Toast.makeText(requireContext(), "사용자 데이터 삭제 실패", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(requireContext(), "일정 삭제 실패", Toast.LENGTH_SHORT).show()
                }
            }
    }

//    // ✅ Google 계정 재인증 후 Firebase 계정 삭제
//    private fun reauthenticateAndDeleteUser(user: FirebaseUser) {
//        val googleAccount = GoogleSignIn.getLastSignedInAccount(requireContext())
//
//        if (googleAccount != null) {
//            val credential = GoogleAuthProvider.getCredential(googleAccount.idToken, null)
//
//            user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
//                if (reauthTask.isSuccessful) {
//                    deleteUserAccount(user) // ✅ 인증 성공 시 계정 삭제
//                } else {
//                    Toast.makeText(requireContext(), "Google 로그인 재인증 실패", Toast.LENGTH_SHORT).show()
//                }
//            }
//        } else {
//            Toast.makeText(requireContext(), "Google 계정 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
//        }
//    }

    // 🔹 **Firebase 계정 삭제**
    private fun deleteUserAccount(user: FirebaseUser) {
        user.delete().addOnCompleteListener { deleteTask ->
            if (deleteTask.isSuccessful) {
                Toast.makeText(requireContext(), "계정이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                logoutAndNavigateHome()
            } else {
                Toast.makeText(requireContext(), "계정 삭제 실패: ${deleteTask.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ✅ 로그아웃 및 HomeFragment로 이동
    private fun logoutAndNavigateHome() {
        firebaseAuth.signOut() // ✅ Firebase 로그아웃

        // ✅ Google 계정 로그아웃
//        GoogleSignIn.getClient(requireContext(), GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()


        val intent = Intent(requireContext(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigateTo", "HomeFragment")
        }
        startActivity(intent)
    }
}
