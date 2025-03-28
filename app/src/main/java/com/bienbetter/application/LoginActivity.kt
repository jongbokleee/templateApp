package com.bienbetter.application

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bienbetter.application.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance().reference

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Google 로그인
        binding.btnGoogleSignIn.setOnClickListener { signInWithGoogle() }

        // 일반 로그인
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            loginWithEmail(email, password)
        }

        // 아이디 찾기
        binding.tvFindEmail.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_find_email, null)
            val emailEditText = dialogView.findViewById<EditText>(R.id.etFindEmail)
            val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirm)
            val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()

            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            btnConfirm.setOnClickListener {
                val inputEmail = emailEditText.text.toString().trim()

                if (!Patterns.EMAIL_ADDRESS.matcher(inputEmail).matches()) {
                    Toast.makeText(this, "올바른 이메일 형식이 아닙니다.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (inputEmail.isNotEmpty()) {
                    checkEmailExists(inputEmail)
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
            }

            dialog.show()
        }
        
        // 비밀번호 찾기
        binding.tvFindPassword.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_find_email, null)
            val emailEditText = dialogView.findViewById<EditText>(R.id.etFindEmail)
            val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirm)
            val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()

            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            btnConfirm.setOnClickListener {
                val inputEmail = emailEditText.text.toString().trim()

                if (inputEmail.isEmpty()) {
                    Toast.makeText(this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(inputEmail).matches()) {
                    Toast.makeText(this, "올바른 이메일 형식이 아닙니다.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val usersRef = FirebaseDatabase.getInstance().reference.child("users")
                usersRef.orderByChild("email").equalTo(inputEmail)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                FirebaseAuth.getInstance().sendPasswordResetEmail(inputEmail)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Toast.makeText(this@LoginActivity, "비밀번호 재설정 메일이 전송되었습니다.", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(this@LoginActivity, "이메일 전송 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                dialog.dismiss()
                            } else {
                                Toast.makeText(this@LoginActivity, "등록되지 않은 이메일입니다.", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@LoginActivity, "데이터베이스 오류: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
            }

            dialog.show()
        }

        // 회원가입 이동
        binding.btnJoin.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun checkEmailExists(email: String) {
        val usersRef = FirebaseDatabase.getInstance().reference.child("users")
        usersRef.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        AlertDialog.Builder(this@LoginActivity)
                            .setTitle("확인 결과")
                            .setMessage("해당 이메일은 가입된 계정입니다.")
                            .setPositiveButton("확인", null)
                            .show()
                    } else {
                        Toast.makeText(this@LoginActivity, "등록되지 않은 이메일입니다.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@LoginActivity, "확인 중 오류 발생: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun loginWithEmail(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "이메일과 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        fetchAndSaveFcmToken(it.uid)
                        moveToAddScheduleActivity()
                    }
                } else {
                    Toast.makeText(this, "로그인 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signInWithGoogle() {
        val googleSignInClient = GoogleSignIn.getClient(
            this, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        )

        startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "구글 로그인 실패: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                Log.e("LoginActivity", "Google Sign-in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let { saveUserToDatabase(it) }
                } else {
                    Toast.makeText(this, "Firebase 인증 실패", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserToDatabase(user: FirebaseUser) {
        val userData = mapOf(
            "uid" to user.uid,
            "email" to user.email,
            "displayName" to (user.displayName ?: "익명")
        )

        database.child("users").child(user.uid).setValue(userData)
            .addOnSuccessListener {
                Log.d("LoginActivity", "사용자 정보 저장 완료")
                fetchAndSaveFcmToken(user.uid)
                moveToAddScheduleActivity()
            }
            .addOnFailureListener {
                Toast.makeText(this, "데이터 저장 실패", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchAndSaveFcmToken(userId: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result?.let { token ->
                    database.child("users").child(userId).child("fcmToken").setValue(token)
                        .addOnSuccessListener { Log.d("LoginActivity", "FCM 토큰 저장 완료") }
                        .addOnFailureListener { Log.e("LoginActivity", "FCM 토큰 저장 실패", it) }
                }
            } else {
                Log.w("LoginActivity", "FCM 토큰 가져오기 실패", task.exception)
            }
        }
    }

    private fun moveToAddScheduleActivity() {
        startActivity(Intent(this, AddScheduleActivity::class.java))
        finish()
    }
}