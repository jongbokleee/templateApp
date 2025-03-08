package com.bienbetter.application.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bienbetter.application.R
import com.bienbetter.application.model.HomeSection

class HomeSectionAdapter(
    private val sectionList: List<HomeSection>,
    private val onItemClick: (String) -> Unit // Click listener
) : RecyclerView.Adapter<HomeSectionAdapter.SectionViewHolder>() {

    class SectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvSectionTitle)
        val tvContent: TextView = itemView.findViewById(R.id.tvSectionContent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_section, parent, false)
        return SectionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SectionViewHolder, position: Int) {
        val section = sectionList[position]
        holder.tvTitle.text = section.title
        holder.tvContent.text = section.items.joinToString("\n")

        // Set click listener on the section title
        holder.itemView.setOnClickListener {
            onItemClick(section.title) // Pass the section title to HomeActivity
        }
    }

    override fun getItemCount(): Int {
        return sectionList.size
    }
}
