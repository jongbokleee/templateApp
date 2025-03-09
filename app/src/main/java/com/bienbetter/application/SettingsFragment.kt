package com.bienbetter.application

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bienbetter.application.databinding.FragmentSettingsBinding
import com.google.firebase.auth.FirebaseAuth

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

        // Firebase 인증 객체 초기화 (계정 탈퇴 기능용)
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

    // 계정 탈퇴 확인 다이얼로그
    private fun showDeleteAccountDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("계정 탈퇴")
            .setMessage("정말 계정을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.")
            .setPositiveButton("탈퇴") { _, _ ->
                deleteAccount()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    // Firebase 계정 삭제
    private fun deleteAccount() {
        val user = firebaseAuth.currentUser
        user?.delete()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireContext(), "계정이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                val intent = Intent(requireContext(), LoginActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            } else {
                Toast.makeText(requireContext(), "계정 삭제 실패. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
