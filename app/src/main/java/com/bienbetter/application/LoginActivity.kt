package com.bienbetter.application

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bienbetter.application.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance().reference // ✅ Firebase Database 초기화

    companion object {
        private const val RC_SIGN_IN = 9001 // 구글 로그인 요청 코드
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ ViewBinding 초기화
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ FirebaseAuth 초기화
        auth = FirebaseAuth.getInstance()

        // ✅ 로그인 상태 확인 → 이미 로그인된 경우 바로 이동
        if (auth.currentUser != null) {
            moveToAddScheduleActivity()
        }

        // ✅ 1 로그인 버튼 클릭 시 Google 로그인 실행
        binding.btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }
    }

    // ✅ 2 구글 로그인 요청
    private fun signInWithGoogle() {
        val googleSignInClient = GoogleSignIn.getClient(
            this, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // 🔹 Firebase Console에서 생성한 웹 클라이언트 ID 필요
                .requestEmail()
                .build()
        )

        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    // ✅ 3 로그인 결과 처리
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

    // ✅ 4 Firebase 인증 처리
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {



                    val user = auth.currentUser
                    if (user != null) {
                        saveUserToDatabase(user) // ✅ 사용자 정보를 Realtime Database에 저장
                    }
                } else {
                    Toast.makeText(this, "Firebase 인증 실패", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // ✅ 5 Firebase Database에 사용자 정보 저장 (선택)
    private fun saveUserToDatabase(user: FirebaseUser) {
        val userData = mapOf(
            "uid" to user.uid,
            "email" to user.email,
            "displayName" to (user.displayName ?: "익명")
        )

        database.child("users").child(user.uid).setValue(userData)
            .addOnSuccessListener {
                Log.d("LoginActivity", "사용자 정보 저장 완료")
                moveToAddScheduleActivity() // ✅ DB 저장 후 이동
            }
            .addOnFailureListener {
                Toast.makeText(this, "데이터 저장 실패", Toast.LENGTH_SHORT).show()
            }

        val database = FirebaseDatabase.getInstance().reference
        database.child("users").child(user.uid).setValue(userData)
    }

    // ✅ 일정 추가 화면으로 이동
    private fun moveToAddScheduleActivity() {
        startActivity(Intent(this, AddScheduleActivity::class.java))
        finish() // 🔹 로그인 화면 종료
    }


}
