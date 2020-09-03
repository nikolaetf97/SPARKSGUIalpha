package com.example.sparks

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.logs_card.view.*

class LogDataAdapter(val context: Context, val logs: MutableList<LogData>) : RecyclerView.Adapter<LogDataAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.logs_card, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = logs.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val data = logs[position]
        holder.setData(data)
    }

    inner class MyViewHolder(itemView:View) : RecyclerView.ViewHolder(itemView) {
        fun setData(data: LogData?) {
            itemView.tv_len.text = context.getString(R.string.info_tv_period) +  data!!.len
            itemView.tv_loc.text = context.getString(R.string.parking_name) + data.loc
            itemView.tv_cost.text = context.getString(R.string.cost) + data.cost
            itemView.tv_date.text = context.getString(R.string.tv_date) + data.date
        }
    }
}