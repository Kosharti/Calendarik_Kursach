package com.example.calendarik

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class CalendarAdapter(
    private val days: ArrayList<LocalDate?>,
    private val selectedDate: LocalDate,
    private val notesMap: Map<LocalDate?, List<Note>>,
    private val onItemClick: (LocalDate) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayOfMonth: TextView = itemView.findViewById(R.id.dayTextView)
        val circleIndicator: View = itemView.findViewById(R.id.circleIndicator)
        val ringsContainer: View = itemView.findViewById(R.id.ringsContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.calendar_cell, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val date = days[position]
        if (date == null) {
            holder.dayOfMonth.text = ""
            holder.circleIndicator.visibility = View.GONE
            holder.itemView.isEnabled = false
            holder.dayOfMonth.setTextColor(Color.LTGRAY)
        } else {
            holder.dayOfMonth.text = date.dayOfMonth.toString()
            holder.itemView.isEnabled = true
            holder.dayOfMonth.setTextColor(Color.BLACK)

            if (date == selectedDate) {
                holder.circleIndicator.visibility = View.VISIBLE
            } else {
                holder.circleIndicator.visibility = View.GONE
            }

            holder.itemView.setOnClickListener {
                onItemClick(date)
            }
        }
    }

    override fun getItemCount(): Int {
        return days.size
    }
}