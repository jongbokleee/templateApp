package com.bienbetter.application

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bienbetter.application.databinding.ActivityHomeBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Firebase 초기화
        auth = FirebaseAuth.getInstance()

        // ✅ 현재 로그인 상태 확인 후 UI 업데이트
        updateUI(auth.currentUser)

        // ✅ 로그인 여부 확인 → 검진일정 추가 클릭 시 로그인 안 되어 있으면 로그인 페이지로 이동
        binding.homeBtnAddSchedule.setOnClickListener {
            if (auth.currentUser != null) {
                // ✅ 로그인 상태 → 검진 일정 추가 화면으로 이동
                startActivity(Intent(this, AddScheduleActivity::class.java))
            } else {
                // ✅ 로그인 안 된 경우 → 로그인 화면으로 이동
                startActivityForResult(Intent(this, LoginActivity::class.java), REQUEST_SIGN_IN)
            }
        }

        // ✅ 로그아웃 버튼 클릭 시 로그아웃 처리
        binding.btnLogout.setOnClickListener {
            logout()
        }
    }

    // ✅ "뒤로 가기" 했을 때 로그인 상태 즉시 반영
    override fun onResume() {
        super.onResume()
        updateUI(auth.currentUser)
    }

    // ✅ 로그인 상태에 따라 UI 업데이트
    private fun updateUI(user: FirebaseUser?) {
        val isLoggedIn = user != null
        // 로그인 상태면 로그아웃 버튼 표시, 로그인 버튼 숨김
        binding.btnLogout.visibility = if (isLoggedIn) View.VISIBLE else View.GONE
    }

    // ✅ 로그아웃 처리
    private fun logout() {
        auth.signOut() // ✅ Firebase 로그아웃
        GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut().addOnCompleteListener {
            updateUI(null)  // ✅ 로그아웃 후 UI 업데이트
            Toast.makeText(this, "로그아웃되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // ✅ 로그인 결과 처리 (즉시 반영)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_SIGN_IN) {
            val user = auth.currentUser
            updateUI(user) // ✅ 로그인 성공 시 UI 업데이트
        }
    }

    companion object {
        private const val REQUEST_SIGN_IN = 1001
    }

}
