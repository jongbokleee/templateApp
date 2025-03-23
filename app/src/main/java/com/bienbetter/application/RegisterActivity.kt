package com.bienbetter.application

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
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
    private var isEmailChecked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // 약관, 개인정보 처리방침 링크
        binding.tvTerms.setOnClickListener { openWebPage("https://www.naver.com/") }
        binding.tvPrivacy.setOnClickListener { openWebPage("https://www.google.com/") }
        binding.backButton.setOnClickListener { finish() }

        // 실시간 유효성 검사
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = validateInputs()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        binding.btnCheckEmail.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()

            if (!isEmailValid(email)) {
                binding.tvEmailCheckMessage.setTextColor(getColor(android.R.color.holo_red_dark))
                binding.tvEmailCheckMessage.text = "올바른 이메일 형식이 아닙니다."
                isEmailChecked = false
                return@setOnClickListener
            }

            auth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val exists = task.result?.signInMethods?.isNotEmpty() == true
                        if (exists) {
                            binding.tvEmailCheckMessage.setTextColor(getColor(android.R.color.holo_red_dark))
                            binding.tvEmailCheckMessage.text = "이미 등록된 이메일입니다."
                            isEmailChecked = false
                        } else {
                            binding.tvEmailCheckMessage.setTextColor(getColor(android.R.color.holo_green_dark))
                            binding.tvEmailCheckMessage.text = "사용 가능한 이메일입니다."
                            isEmailChecked = true
                        }
                        validateInputs()
                    } else {
                        Toast.makeText(this, "이메일 확인 중 오류 발생", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                isEmailChecked = false
                validateInputs()
                binding.tvEmailCheckMessage.text = "" // 이메일 바꾸면 메시지 리셋
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.etRegisterPassword.addTextChangedListener(watcher)

        // 회원가입 버튼 클릭
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etRegisterPassword.text.toString().trim()

            if (!isEmailChecked) {
                Toast.makeText(this, "이메일 중복 확인을 먼저 진행해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

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

            if (!binding.cbTerms.isChecked || !binding.cbPrivacy.isChecked) {
                Toast.makeText(this, "약관에 동의해야 회원가입이 가능합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            checkEmailAndRegister(email, password)
        }
    }

    // ✅ 이메일 중복 확인 후 회원가입
    private fun checkEmailAndRegister(email: String, password: String) {
        auth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val exists = task.result?.signInMethods?.isNotEmpty() == true
                    if (exists) {
                        binding.tvEmailCheckMessage.apply {
                            text = "이미 등록된 이메일입니다."
                            setTextColor(getColor(android.R.color.holo_red_dark))
                            visibility = View.VISIBLE
                        }
                    } else {
                        binding.tvEmailCheckMessage.apply {
                            text = "사용 가능한 이메일입니다."
                            setTextColor(getColor(android.R.color.holo_green_dark))
                            visibility = View.VISIBLE
                        }
                        registerWithEmail(email, password)
                    }
                } else {
                    binding.tvEmailCheckMessage.apply {
                        text = "이메일 확인 중 오류가 발생했습니다."
                        setTextColor(getColor(android.R.color.holo_red_dark))
                        visibility = View.VISIBLE
                    }
                }
            }
    }

    // ✅ Firebase 회원가입
    private fun registerWithEmail(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    auth.currentUser?.let { saveUserToDatabase(it) }
                } else {
                    Toast.makeText(this, "회원가입 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // ✅ Firebase 사용자 정보 저장
    private fun saveUserToDatabase(user: FirebaseUser) {
        val userData = mapOf("uid" to user.uid, "email" to user.email)

        database.child("users").child(user.uid).setValue(userData)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "사용자 정보 저장 완료")
                moveToLoginActivity()
            }
            .addOnFailureListener {
                Toast.makeText(this, "데이터 저장 실패", Toast.LENGTH_SHORT).show()
            }
    }

    private fun moveToLoginActivity() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun validateInputs() {
        val email = binding.etEmail.text.toString()
        val password = binding.etRegisterPassword.text.toString()

        val emailValid = isEmailValid(email)
        val passwordValid = isPasswordValid(password)

        binding.etEmail.error = if (!emailValid && email.isNotEmpty()) "이메일 형식이 올바르지 않습니다" else null
        binding.etRegisterPassword.error = if (!passwordValid && password.isNotEmpty()) "영문, 숫자, 특수문자 포함 8자 이상" else null

        binding.btnRegister.isEnabled = emailValid && passwordValid
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

    private fun openWebPage(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}
