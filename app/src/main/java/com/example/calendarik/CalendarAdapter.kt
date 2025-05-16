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
    private val notesMap: Map<LocalDate, List<Note>>,
    private val onItemClick: (LocalDate) -> Unit,
    private val onMonthChange: (LocalDate) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayOfMonth: TextView = itemView.findViewById(R.id.dayTextView)
        val circleIndicator: View = itemView.findViewById(R.id.circleIndicator)
        val ringsContainer: LinearLayout = itemView.findViewById(R.id.ringsContainer)

        fun clearBackground() {
            itemView.setBackgroundColor(Color.TRANSPARENT)
            dayOfMonth.setTextColor(Color.BLACK)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.calendar_cell, parent, false)
        return CalendarViewHolder(view)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val date = days[position]
        holder.clearBackground()

        if (date == null) {
            holder.dayOfMonth.text = ""
            holder.circleIndicator.visibility = View.GONE
            holder.itemView.isEnabled = false
            holder.dayOfMonth.setTextColor(Color.LTGRAY)
        } else {
            holder.dayOfMonth.text = date.dayOfMonth.toString()
            holder.itemView.isEnabled = true

            holder.dayOfMonth.setTextColor(if (date.monthValue == selectedDate.monthValue) Color.BLACK else Color.LTGRAY)
            holder.circleIndicator.visibility = if (date == selectedDate) View.VISIBLE else View.GONE

            // 🟣 Показываем иконки категорий под числом
            holder.ringsContainer.removeAllViews()
            val notesForDay = notesMap[date]
            notesForDay?.forEach { note ->
                val imageView = ImageView(holder.itemView.context)
                imageView.layoutParams = LinearLayout.LayoutParams(16, 16).apply {
                    setMargins(2, 0, 2, 0)
                }
                val icon = when (note.category) {
                    "Brainstorm" -> R.drawable.oval_1
                    "Design" -> R.drawable.oval_2
                    "Workout" -> R.drawable.oval_3
                    else -> R.drawable.oval_1
                }
                imageView.setImageResource(icon)
                holder.ringsContainer.addView(imageView)
            }

            holder.itemView.setOnClickListener {
                if (date.monthValue != selectedDate.monthValue) {
                    onMonthChange(date)
                } else {
                    val previousSelectedPosition = selectedPosition
                    selectedPosition = holder.adapterPosition

                    if (previousSelectedPosition != RecyclerView.NO_POSITION) {
                        notifyItemChanged(previousSelectedPosition)
                    }
                    notifyItemChanged(selectedPosition)

                    onItemClick(date)
                }
            }

            if (position == selectedPosition) {
                holder.itemView.background = holder.itemView.context.getDrawable(R.drawable.rounded_corner_background)
                holder.dayOfMonth.setTextColor(Color.WHITE)
            } else {
                holder.itemView.background = null
                holder.itemView.setBackgroundColor(Color.TRANSPARENT)
            }
        }
    }

    override fun getItemCount(): Int = days.size

    fun setSelectedDate(date: LocalDate) {
        selectedDate = date
        notifyDataSetChanged()
    }
}
