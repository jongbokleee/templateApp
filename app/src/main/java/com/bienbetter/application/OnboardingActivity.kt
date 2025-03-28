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
import android.net.Uri
import com.google.firebase.database.FirebaseDatabase

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewBinding 초기화
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
            checkForceUpdate()
        } else {
            showNetworkErrorDialog()
        }
    }

    private fun checkForceUpdate() {
        val dbRef = FirebaseDatabase.getInstance().reference.child("config")
        dbRef.get().addOnSuccessListener { snapshot ->
            val latestVersion = snapshot.child("latest_version").getValue(String::class.java) ?: return@addOnSuccessListener
            val forceUpdate = snapshot.child("force_update").getValue(Boolean::class.java) ?: false
            val updateUrl = snapshot.child("update_url").getValue(String::class.java) ?: return@addOnSuccessListener

            val currentVersion = getCurrentAppVersion()

            if (forceUpdate && isVersionLower(currentVersion, latestVersion)) {
                showForceUpdateDialog(updateUrl)
            } else {
                proceedToNextStep()
            }
        }.addOnFailureListener {
            proceedToNextStep()
        }
    }

    private fun getCurrentAppVersion(): String {
        return packageManager.getPackageInfo(packageName, 0).versionName
    }

    private fun isVersionLower(current: String, latest: String): Boolean {
        val currentParts = current.split(".")
        val latestParts = latest.split(".")

        for (i in 0 until maxOf(currentParts.size, latestParts.size)) {
            val cur = currentParts.getOrNull(i)?.toIntOrNull() ?: 0
            val lat = latestParts.getOrNull(i)?.toIntOrNull() ?: 0
            if (cur < lat) return true
            if (cur > lat) return false
        }
        return false
    }

    private fun showForceUpdateDialog(updateUrl: String) {
        AlertDialog.Builder(this)
            .setTitle("업데이트 필요")
            .setMessage("최신 버전으로 업데이트하지 않으면 앱을 사용할 수 없습니다.")
            .setCancelable(false)
            .setPositiveButton("업데이트") { _, _ ->
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl)))
                finish()
            }
            .show()
    }

    private fun proceedToNextStep() {
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 1000)
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
