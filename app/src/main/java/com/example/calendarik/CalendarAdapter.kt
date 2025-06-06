package com.example.calendarik

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDate
import java.util.*

class CalendarAdapter(
    private val days: ArrayList<LocalDate?>,
    private var selectedDate: LocalDate,
    private var notesMap: Map<LocalDate, List<Note>>,
    private val onItemClick: (LocalDate) -> Unit,
    private val onMonthChange: (LocalDate) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayOfMonth: TextView = itemView.findViewById(R.id.dayTextView)

        val ringsContainer: LinearLayout = itemView.findViewById(R.id.ringsContainer)

        fun clearBackground() {
            dayOfMonth.background = null
            dayOfMonth.setTextColor(Color.BLACK)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.calendar_cell, parent, false)
        val holder = CalendarViewHolder(view)

        holder.dayOfMonth.setOnClickListener {
            val position = holder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val date = days[position]
                if (date != null) {
                    if (date.monthValue != selectedDate.monthValue) {
                        onMonthChange(date)
                    } else {
                        selectedPosition = position
                        notifyDataSetChanged()
                        onItemClick(date)
                    }
                }
            }
        }

        return holder
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val date = days[position]
        holder.clearBackground()

        if (date == null) {
            holder.dayOfMonth.text = ""
            holder.itemView.isEnabled = false
            holder.dayOfMonth.setTextColor(Color.LTGRAY)
        } else {
            holder.dayOfMonth.text = date.dayOfMonth.toString()
            holder.itemView.isEnabled = true

            holder.dayOfMonth.setTextColor(
                if (date.monthValue == selectedDate.monthValue) Color.BLACK else Color.LTGRAY
            )

            holder.ringsContainer.removeAllViews()
            val notesForDay = notesMap[date]
            if (!notesForDay.isNullOrEmpty()) {
                notesForDay.forEach { note ->
                    val imageView = ImageView(holder.itemView.context).apply {
                        layoutParams = LinearLayout.LayoutParams(16, 16).apply {
                            setMargins(2, 0, 2, 0)
                        }
                        setImageResource(
                            when (note.category.trim().lowercase()) {
                                "brainstorm" -> R.drawable.oval_1
                                "design" -> R.drawable.oval_2
                                "workout" -> R.drawable.oval_3
                                else -> R.drawable.oval_1
                            }
                        )
                    }
                    holder.ringsContainer.addView(imageView)
                }
            }

            if (position == selectedPosition) {
                holder.dayOfMonth.background = holder.itemView.context.getDrawable(R.drawable.rounded_corner_background)
                holder.dayOfMonth.setTextColor(Color.WHITE)
            } else {
                holder.dayOfMonth.background = null
                holder.dayOfMonth.setTextColor(
                    if (date.monthValue == selectedDate.monthValue) Color.BLACK else Color.LTGRAY
                )
            }
        }
    }

    override fun getItemCount(): Int = days.size

    fun setSelectedDate(date: LocalDate) {
        selectedDate = date
        notifyDataSetChanged()
    }

    fun updateDays(newDays: ArrayList<LocalDate?>, newSelectedDate: LocalDate) {
        days.clear()
        days.addAll(newDays)
        selectedDate = newSelectedDate
        notifyDataSetChanged()
    }

    fun updateNotesMap(newNotesMap: Map<LocalDate, List<Note>>) {
        this.notesMap = newNotesMap
        notifyDataSetChanged()
    }
}
