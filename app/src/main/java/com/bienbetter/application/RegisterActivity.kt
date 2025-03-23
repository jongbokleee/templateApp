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
import com.google.firebase.database.*
import java.util.regex.Pattern

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private var isEmailChecked = false
    private var lastCheckedEmail = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        binding.tvTerms.setOnClickListener { openWebPage("https://www.naver.com/") }
        binding.tvPrivacy.setOnClickListener { openWebPage("https://www.google.com/") }
        binding.backButton.setOnClickListener { finish() }

        // 이메일, 비밀번호 입력 유효성 감시
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.tvEmailCheckMessage.text = ""
                isEmailChecked = false
                validateInputs()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        binding.etEmail.addTextChangedListener(watcher)
        binding.etRegisterPassword.addTextChangedListener(watcher)

        // ✅ 이메일 중복 확인 버튼
        binding.btnCheckEmail.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (!isEmailValid(email)) {
                binding.tvEmailCheckMessage.apply {
                    text = "올바른 이메일 형식이 아닙니다."
                    setTextColor(getColor(android.R.color.holo_red_dark))
                    visibility = View.VISIBLE
                }
                isEmailChecked = false
                return@setOnClickListener
            }

            // Firebase Authentication + Database 확인
            checkEmailDuplication(email)
        }

        // ✅ 회원가입 버튼
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etRegisterPassword.text.toString().trim()

            if (!isEmailChecked || email != lastCheckedEmail) {
                Toast.makeText(this, "이메일 중복 확인을 먼저 진행해주세요.", Toast.LENGTH_SHORT).show()
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

            registerWithEmail(email, password)
        }
    }

    // ✅ 이메일 중복 확인 (Authentication + Database)
    private fun checkEmailDuplication(email: String) {
        auth.fetchSignInMethodsForEmail(email)
            .addOnSuccessListener { authResult ->
                val authExists = authResult.signInMethods?.isNotEmpty() == true

                // Database 확인
                database.child("users").orderByChild("email").equalTo(email)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val dbExists = snapshot.exists()
                            if (authExists || dbExists) {
                                binding.tvEmailCheckMessage.apply {
                                    text = "이미 등록된 이메일입니다."
                                    setTextColor(getColor(android.R.color.holo_red_dark))
                                    visibility = View.VISIBLE
                                }
                                isEmailChecked = false
                            } else {
                                binding.tvEmailCheckMessage.apply {
                                    text = "사용 가능한 이메일입니다."
                                    setTextColor(getColor(android.R.color.holo_green_dark))
                                    visibility = View.VISIBLE
                                }
                                isEmailChecked = true
                                lastCheckedEmail = email
                            }
                            validateInputs()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@RegisterActivity, "이메일 확인 실패", Toast.LENGTH_SHORT).show()
                            Log.e("RegisterActivity", "DB 확인 실패: ${error.message}")
                        }
                    })
            }
            .addOnFailureListener {
                Toast.makeText(this, "이메일 확인 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
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
