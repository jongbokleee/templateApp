package com.bienbetter.application

import android.os.Bundle
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TermsOfServiceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms_of_service)

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
        val section12 = findViewById<TextView>(R.id.section12)
        val section13 = findViewById<TextView>(R.id.section13)
        val section14 = findViewById<TextView>(R.id.section14)
        val section15 = findViewById<TextView>(R.id.section15)
        val section16 = findViewById<TextView>(R.id.section16)
        val section17 = findViewById<TextView>(R.id.section17)
        val section18 = findViewById<TextView>(R.id.section18)

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
        val tvGotoSection12 = findViewById<TextView>(R.id.tvGotoSection12)
        val tvGotoSection13 = findViewById<TextView>(R.id.tvGotoSection13)
        val tvGotoSection14 = findViewById<TextView>(R.id.tvGotoSection14)
        val tvGotoSection15 = findViewById<TextView>(R.id.tvGotoSection15)
        val tvGotoSection16 = findViewById<TextView>(R.id.tvGotoSection16)
        val tvGotoSection17 = findViewById<TextView>(R.id.tvGotoSection17)
        val tvGotoSection18 = findViewById<TextView>(R.id.tvGotoSection18)

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
        tvGotoSection12.setOnClickListener { scrollView.post { scrollView.smoothScrollTo(0, section12.top) } }
        tvGotoSection13.setOnClickListener { scrollView.post { scrollView.smoothScrollTo(0, section13.top) } }
        tvGotoSection14.setOnClickListener { scrollView.post { scrollView.smoothScrollTo(0, section14.top) } }
        tvGotoSection15.setOnClickListener { scrollView.post { scrollView.smoothScrollTo(0, section15.top) } }
        tvGotoSection16.setOnClickListener { scrollView.post { scrollView.smoothScrollTo(0, section16.top) } }
        tvGotoSection17.setOnClickListener { scrollView.post { scrollView.smoothScrollTo(0, section17.top) } }
        tvGotoSection18.setOnClickListener { scrollView.post { scrollView.smoothScrollTo(0, section18.top) } }
    }
}
