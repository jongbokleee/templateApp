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
        // Section TextView
        val section1 = findViewById<TextView>(R.id.section1)
        val section2 = findViewById<TextView>(R.id.section2)
        val section3 = findViewById<TextView>(R.id.section3)
        val section4 = findViewById<TextView>(R.id.section4)
        val section5 = findViewById<TextView>(R.id.section5)
        val section6 = findViewById<TextView>(R.id.section6)
        val section7 = findViewById<TextView>(R.id.section7)
        val section8 = findViewById<TextView>(R.id.section8)
        val section9 = findViewById<TextView>(R.id.section9)
        val section10 = findViewById<TextView>(R.id.section10)
        val section11 = findViewById<TextView>(R.id.section11)

        // Goto 목차 TextView
        val tvGotoSection1 = findViewById<TextView>(R.id.tvGotoSection1)
        val tvGotoSection2 = findViewById<TextView>(R.id.tvGotoSection2)
        val tvGotoSection3 = findViewById<TextView>(R.id.tvGotoSection3)
        val tvGotoSection4 = findViewById<TextView>(R.id.tvGotoSection4)
        val tvGotoSection5 = findViewById<TextView>(R.id.tvGotoSection5)
        val tvGotoSection6 = findViewById<TextView>(R.id.tvGotoSection6)
        val tvGotoSection7 = findViewById<TextView>(R.id.tvGotoSection7)
        val tvGotoSection8 = findViewById<TextView>(R.id.tvGotoSection8)
        val tvGotoSection9 = findViewById<TextView>(R.id.tvGotoSection9)
        val tvGotoSection10 = findViewById<TextView>(R.id.tvGotoSection10)
        val tvGotoSection11 = findViewById<TextView>(R.id.tvGotoSection11)

        tvGotoSection1.setOnClickListener { scrollView.post { scrollView.smoothScrollTo(0, section1.top) } }
        tvGotoSection2.setOnClickListener { scrollView.post { scrollView.smoothScrollTo(0, section2.top) } }
        tvGotoSection3.setOnClickListener { scrollView.post { scrollView.smoothScrollTo(0, section3.top) } }
        tvGotoSection4.setOnClickListener { scrollView.post { scrollView.smoothScrollTo(0, section4.top) } }
        tvGotoSection5.setOnClickListener { scrollView.post { scrollView.smoothScrollTo(0, section5.top) } }
        tvGotoSection6.setOnClickListener { scrollView.post { scrollView.smoothScrollTo(0, section6.top) } }
        tvGotoSection7.setOnClickListener { scrollView.post { scrollView.smoothScrollTo(0, section7.top) } }
        tvGotoSection8.setOnClickListener { scrollView.post { scrollView.smoothScrollTo(0, section8.top) } }
        tvGotoSection9.setOnClickListener { scrollView.post { scrollView.smoothScrollTo(0, section9.top) } }
        tvGotoSection10.setOnClickListener { scrollView.post { scrollView.smoothScrollTo(0, section10.top) } }
        tvGotoSection11.setOnClickListener { scrollView.post { scrollView.smoothScrollTo(0, section11.top) } }
    }
}
