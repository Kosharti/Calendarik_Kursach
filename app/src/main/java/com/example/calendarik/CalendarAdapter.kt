package com.example.calendarik

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class CalendarAdapter(
    private val days: ArrayList<LocalDate>,
    private val onItemClick: (LocalDate) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayOfMonth: TextView = itemView.findViewById(R.id.dayTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.calendar_cell, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val date = days[position]
        if (date != LocalDate.MIN) {
            holder.dayOfMonth.text = date.dayOfMonth.toString()

            holder.itemView.setOnClickListener {
                onItemClick(date)
            }
        } else {
            holder.dayOfMonth.text = ""
        }

    }

    override fun getItemCount(): Int = days.size
}