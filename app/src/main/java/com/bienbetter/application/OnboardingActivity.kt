package com.bienbetter.application

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bienbetter.application.databinding.ActivityOnboardingBinding

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewBinding 초기화
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ SharedPreferences 확인 → 온보딩이 이미 완료되었으면 `MainActivity`로 이동
        val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val isFirstRun = sharedPreferences.getBoolean("isFirstRun", true)

        if (!isFirstRun) {
            navigateToMain()
            return
        }

        // 네트워크 체크 후 이동
        checkNetworkAndProceed()
    }

    // ✅ 네트워크 상태 체크 함수
    private fun Context.isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun checkNetworkAndProceed() {
        if (isNetworkAvailable()) {
            // 1초 후 MainActivity로 이동
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }, 1000)
        } else {
            // 네트워크 오류 팝업 표시
            showNetworkErrorDialog()
        }
    }

    private fun showNetworkErrorDialog() {
        AlertDialog.Builder(this)
            .setTitle("연결 오류")
            .setMessage("인터넷 연결 상태를 확인하거나 다른 네트워크에 연결해 보세요.")
            .setCancelable(false)
            .setPositiveButton("확인") { _, _ ->
                checkNetworkAndProceed() // 다시 시도
            }
            .show()
    }


    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish() // 온보딩 화면 종료
    }
}
