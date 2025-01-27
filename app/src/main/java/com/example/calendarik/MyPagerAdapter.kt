package com.example.calendarik

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyPagerAdapter(private val items: List<PageData>) : RecyclerView.Adapter<MyPagerAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
        val imageView1: ImageView = view.findViewById(R.id.imageView1)
        val textView: TextView = view.findViewById(R.id.textView)
        val textView1: TextView = view.findViewById(R.id.textView1)
        val textView2: TextView = view.findViewById(R.id.textView2)
        val textView3: TextView = view.findViewById(R.id.textView3)
        val imageView2: ImageView = view.findViewById(R.id.imageView2)
        val imageButton1: ImageView = view.findViewById(R.id.imageButton1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.page_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.imageView.setImageResource(item.imageRes)
        holder.imageView1.setImageResource(item.imageRes1)
        holder.textView.text = item.title
        holder.textView1.text = item.welcomeMessage
        holder.textView2.text = item.headerText1
        holder.textView3.text = item.headerText2
        holder.imageView2.setImageResource(item.backgroundImageRes)
        holder.imageButton1.setImageResource(item.slideImageRes)
        holder.imageButton1.setOnClickListener {
            val intent = Intent(holder.itemView.context, Calendarik::class.java)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = items.size

}
