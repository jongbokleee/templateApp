package com.bienbetter.application.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.bienbetter.application.MainActivity
import androidx.recyclerview.widget.RecyclerView
import com.bienbetter.application.R
import com.bienbetter.application.model.ScheduleItem

class ScheduleAdapter(private val activity: FragmentActivity, private var scheduleList: List<ScheduleItem>) :
    RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {

    class ScheduleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val hospitalName: TextView = view.findViewById(R.id.tvHospitalName)
        val scheduleDate: TextView = view.findViewById(R.id.tvScheduleDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_schedule, parent, false)
        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val schedule = scheduleList[position]
        holder.hospitalName.text = schedule.hospitalName
        holder.scheduleDate.text = schedule.date

        // ✅ 일정 클릭 시 `CalendarFragment`로 이동하고, 해당 날짜 전달
        holder.itemView.setOnClickListener {
            val intent = Intent(activity, MainActivity::class.java)
            intent.putExtra("selected_date", schedule.date)
            activity.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = scheduleList.size

    fun updateList(newList: List<ScheduleItem>) {
        scheduleList = newList.sortedByDescending { it.date } // ✅ 최신 일정이 위로 정렬
        notifyDataSetChanged()
    }
}
