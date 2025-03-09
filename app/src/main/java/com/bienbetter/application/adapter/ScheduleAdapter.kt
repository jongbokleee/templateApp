package com.bienbetter.application.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bienbetter.application.databinding.ItemScheduleBinding

class ScheduleAdapter(private var schedules: List<String>) :
    RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {

    class ScheduleViewHolder(private val binding: ItemScheduleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(schedule: String) {
            binding.tvScheduleItem.text = schedule
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val binding = ItemScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ScheduleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        holder.bind(schedules[position])
    }

    override fun getItemCount(): Int = schedules.size

    fun updateList(newSchedules: List<String>) {
        schedules = newSchedules
        notifyDataSetChanged()
    }
}
