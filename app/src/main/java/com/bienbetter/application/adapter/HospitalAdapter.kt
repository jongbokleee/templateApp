package com.bienbetter.application.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bienbetter.application.R
import com.bienbetter.application.model.Hospital

class HospitalAdapter(private var hospitals: List<Hospital>) :
    RecyclerView.Adapter<HospitalAdapter.HospitalViewHolder>() {

    class HospitalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvHospitalName)
        val address: TextView = view.findViewById(R.id.tvHospitalAddress)
        val phone: TextView = view.findViewById(R.id.tvHospitalPhone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HospitalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_hospital, parent, false)
        return HospitalViewHolder(view)
    }

    override fun onBindViewHolder(holder: HospitalViewHolder, position: Int) {
        val hospital = hospitals[position]
        holder.name.text = hospital.name
        holder.address.text = hospital.address
        holder.phone.text = hospital.phone
    }

    override fun getItemCount(): Int = hospitals.size

    fun updateList(newHospitals: List<Hospital>) {
        hospitals = newHospitals
        notifyDataSetChanged()
    }
}