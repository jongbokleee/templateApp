package com.bienbetter.application

import android.os.Bundle
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PrivacyPolicyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)

        val scrollView = findViewById<ScrollView>(R.id.scrollView)
        val section1 = findViewById<TextView>(R.id.section1)
        val section2 = findViewById<TextView>(R.id.section2)

        val tvGotoSection1 = findViewById<TextView>(R.id.tvGotoSection1)
        val tvGotoSection2 = findViewById<TextView>(R.id.tvGotoSection2)

        tvGotoSection1.setOnClickListener {
            scrollView.post {
                scrollView.smoothScrollTo(0, section1.top)
            }
        }

        tvGotoSection2.setOnClickListener {
            scrollView.post {
                scrollView.smoothScrollTo(0, section2.top)
            }
        }
    }
}
