package com.example.sparks

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.logs_card.view.*

class LogDataAdapter(val context: Context, val logs: List<LogData>) : RecyclerView.Adapter<LogDataAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.logs_card, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = logs.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val data = logs[position]
        holder.setData(data, position)
    }


    inner class MyViewHolder(itemView:View) : RecyclerView.ViewHolder(itemView) {
        fun setData(data: LogData?, position: Int) {
            itemView.tv_period.text = data!!.date
            itemView.tv_location.text = data!!.loc
            itemView.cost.text = data!!.cost
        }

    }
}