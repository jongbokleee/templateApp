package com.bienbetter.application

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bienbetter.application.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import java.util.regex.Pattern

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

        binding.backButton.setOnClickListener {
            finish()  // 현재 액티비티 종료
        }

        // 실시간 유효성 체크
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateInputs()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        binding.etEmail.addTextChangedListener(watcher)
        binding.etRegisterPassword.addTextChangedListener(watcher)

        // ✅ 회원가입 버튼 클릭 시
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etRegisterPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "이메일과 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isEmailValid(email)) {
                Toast.makeText(this, "올바른 이메일 형식이 아닙니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isPasswordValid(password)) {
                Toast.makeText(this, "비밀번호 조건을 확인하세요.", Toast.LENGTH_SHORT).show()
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

    private fun validateInputs() {
        val email = binding.etEmail.text.toString()
        val password = binding.etRegisterPassword.text.toString()

        val isEmailValid = isEmailValid(email)
        val isPasswordValid = isPasswordValid(password)

        binding.etEmail.error = if (!isEmailValid && email.isNotEmpty()) "이메일 형식이 올바르지 않습니다" else null
        binding.etRegisterPassword.error = if (!isPasswordValid && password.isNotEmpty()) "영문, 숫자, 특수문자 포함 8자 이상" else null

        binding.btnRegister.isEnabled = isEmailValid && isPasswordValid
    }

    private fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isPasswordValid(password: String): Boolean {
        if (password.length < 8) return false

        val hasLetter = Pattern.compile("[a-zA-Z]").matcher(password).find()
        val hasDigit = Pattern.compile("[0-9]").matcher(password).find()
        val hasSpecial = Pattern.compile("[^a-zA-Z0-9]").matcher(password).find()

        return hasLetter && hasDigit && hasSpecial
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
