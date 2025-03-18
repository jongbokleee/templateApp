package com.bienbetter.application

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bienbetter.application.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // 이용약관 클릭 시 이동
        findViewById<TextView>(R.id.tvTerms).setOnClickListener {
            openWebPage("https://www.naver.com/")
        }

        // 개인정보 처리방침 클릭 시 이동
        findViewById<TextView>(R.id.tvPrivacy).setOnClickListener {
            openWebPage("https://www.google.com/")
        }

        // ✅ 회원가입 버튼 클릭 시
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etRegisterPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "이메일과 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 이용약관 및 개인정보 처리방침 동의 확인
            if (!binding.cbTerms.isChecked || !binding.cbPrivacy.isChecked) {
                Toast.makeText(this, "약관에 동의해야 회원가입이 가능합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerWithEmail(email, password)
        }
    }

    // ✅ 1. Firebase 회원가입 처리
    private fun registerWithEmail(email: String, password: String) {
        // firebase Authentication에 비밀번호 저장
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        saveUserToDatabase(user)
                    }
                } else {
                    Toast.makeText(this, "회원가입 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // ✅ 2. Firebase Database에 사용자 정보 저장
    private fun saveUserToDatabase(user: FirebaseUser) {
        val userData = mapOf(
            "uid" to user.uid,
            "email" to user.email
        )

        database.child("users").child(user.uid).setValue(userData)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "사용자 정보 저장 완료")
                moveToLoginActivity()
            }
            .addOnFailureListener {
                Toast.makeText(this, "데이터 저장 실패", Toast.LENGTH_SHORT).show()
            }
    }

    // ✅ 3. 로그인 화면으로 이동
    private fun moveToLoginActivity() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    // 웹페이지 열기 함수
    private fun openWebPage(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}
