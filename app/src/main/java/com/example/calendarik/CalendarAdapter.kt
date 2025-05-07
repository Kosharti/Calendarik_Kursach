package com.example.calendarik

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDate

class CalendarAdapter(
    private val days: ArrayList<LocalDate?>,
    private var selectedDate: LocalDate,
    private val notesForDate: Map<LocalDate, List<Note>>,
    private val onItemClick: (LocalDate) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayOfMonth: TextView = itemView.findViewById(R.id.dayTextView)
        val circleIndicator: View = itemView.findViewById(R.id.circleIndicator)
        val ringsContainer: LinearLayout = itemView.findViewById(R.id.ringsContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.calendar_cell, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val date = days[position]

        holder.dayOfMonth.text = date?.dayOfMonth?.toString() ?: ""

        if (date != null) {
            if (date.month != selectedDate.month) {
                holder.dayOfMonth.setTextColor(Color.GRAY)
            } else {
                holder.dayOfMonth.setTextColor(Color.BLACK)
            }

            if (date == selectedDate) {
                holder.dayOfMonth.setTextColor(Color.WHITE)

                val shape = GradientDrawable()
                shape.shape = GradientDrawable.RECTANGLE
                shape.cornerRadius = 25f
                shape.setColor(ContextCompat.getColor(holder.itemView.context, R.color.purple_500))
                holder.circleIndicator.background = shape
                holder.circleIndicator.visibility = View.VISIBLE
            } else {
                holder.circleIndicator.visibility = View.GONE
            }

            holder.itemView.setOnClickListener {
                onItemClick(date)
                setSelectedDate(date)
                notifyDataSetChanged()
            }

            displayCategoryRings(holder, date)

        } else {
            holder.circleIndicator.visibility = View.GONE
        }
    }
    fun setSelectedDate(date: LocalDate) {
        selectedDate = date
        notifyDataSetChanged()
    }

    private fun displayCategoryRings(holder: ViewHolder, date: LocalDate) {
        holder.ringsContainer.removeAllViews()

        val notes = notesForDate[date] ?: emptyList()

        notes.forEach { note ->
            val ring = ImageView(holder.itemView.context)
            val ringColor = when (note.category) {
                "Brainstorm" -> R.drawable.oval_1
                "Design" -> R.drawable.oval_2
                "Workout" -> R.drawable.oval_3
                else -> R.drawable.oval_1
            }
            ring.setImageResource(ringColor)

            val params = LinearLayout.LayoutParams(
                dpToPx(10, holder.itemView.context),
                dpToPx(10, holder.itemView.context)
            )

            ring.layoutParams = params
            holder.ringsContainer.addView(ring)
        }
    }

    override fun getItemCount(): Int = days.size

    private fun dpToPx(dp: Int, context: Context): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }
}