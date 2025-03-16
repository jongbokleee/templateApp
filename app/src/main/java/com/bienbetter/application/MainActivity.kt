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

        setupFragments()

        // ✅ 일정 클릭 시 이동하는 경우 selected_date 전달
        val selectedDate = intent.getStringExtra("selected_date")
        if (selectedDate != null) {
            switchToCalendarWithDate(selectedDate)
        }

        val navigateTo = intent.getStringExtra("navigateTo")

        if (navigateTo == "HospitalSearchFragment") {
            switchFragment(hospitalFragment)
        }

        if (navigateTo == "HomeFragment") {
            switchFragment(homeFragment)
            binding.bottomNavigation.selectedItemId = R.id.nav_home
        }

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

    private val homeFragment = HomeFragment()
    private val calendarFragment = CalendarFragment()
    private val hospitalFragment = HospitalSearchFragment()
    private val historyFragment = HistoryFragment()
    private val settingsFragment = SettingsFragment()

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

    private fun switchFragment(targetFragment: Fragment) {
        if (activeFragment != targetFragment) {
            supportFragmentManager.beginTransaction()
                .hide(activeFragment!!)
                .show(targetFragment)
                .commit()
            activeFragment = targetFragment
        }
    }

    // ✅ `CalendarFragment`로 이동하면서 선택한 날짜 전달
    private fun switchToCalendarWithDate(selectedDate: String) {
        val bundle = Bundle()
        bundle.putString("selected_date", selectedDate)
        calendarFragment.arguments = bundle

        switchFragment(calendarFragment) // ✅ `CalendarFragment` 활성화
        binding.bottomNavigation.selectedItemId = R.id.nav_calendar // ✅ 하단 네비게이션 변경
    }
}
