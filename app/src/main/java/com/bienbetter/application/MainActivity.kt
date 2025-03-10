package com.bienbetter.application

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bienbetter.application.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private var activeFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // ✅ 프래그먼트 관리 개선 (add / show / hide 방식)
        setupFragments()

        // ✅ Intent를 통해 특정 Fragment로 이동하는 경우 처리
        val navigateTo = intent.getStringExtra("navigateTo")
        if (navigateTo == "HomeFragment") {
            switchFragment(homeFragment)
            binding.bottomNavigation.selectedItemId = R.id.nav_home
        }

        // ✅ 하단 내비게이션 클릭 시 `Fragment` 전환
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> switchFragment(homeFragment)
                R.id.nav_calendar -> switchFragment(calendarFragment)
                R.id.nav_hospital -> switchFragment(hospitalFragment)
                R.id.nav_history -> switchFragment(historyFragment)
                R.id.nav_settings -> switchFragment(settingsFragment)
            }
            true
        }
    }

    // ✅ 프래그먼트 객체 선언 (프래그먼트가 매번 새로 생성되지 않도록)
    private val homeFragment = HomeFragment()
    private val calendarFragment = CalendarFragment()
    private val hospitalFragment = HospitalSearchFragment()
    private val historyFragment = HistoryFragment()
    private val settingsFragment = SettingsFragment()

    // ✅ `Fragment` 초기화
    private fun setupFragments() {
        supportFragmentManager.beginTransaction()
            .add(R.id.fragmentContainer, homeFragment, "HomeFragment")
            .add(R.id.fragmentContainer, calendarFragment, "CalendarFragment").hide(calendarFragment)
            .add(R.id.fragmentContainer, hospitalFragment, "HospitalSearchFragment").hide(hospitalFragment)
            .add(R.id.fragmentContainer, historyFragment, "HistoryFragment").hide(historyFragment)
            .add(R.id.fragmentContainer, settingsFragment, "SettingsFragment").hide(settingsFragment)
            .commit()
        activeFragment = homeFragment
    }

    // ✅ `Fragment` 전환 함수 (기존 Fragment 숨기고 새로운 Fragment 표시)
    private fun switchFragment(targetFragment: Fragment) {
        if (activeFragment != targetFragment) {
            supportFragmentManager.beginTransaction()
                .hide(activeFragment!!)
                .show(targetFragment)
                .commit()
            activeFragment = targetFragment
        }
    }
}
