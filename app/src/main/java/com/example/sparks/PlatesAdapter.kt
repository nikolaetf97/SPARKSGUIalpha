package com.example.sparks

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlatesAdapter(private val data: ArrayList<PlatesData>,
                    private val clickListener: (PlatesData) -> Unit)
    : RecyclerView.Adapter<PlatesAdapter.ViewHolder>(){

    inner class ViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
        val nameTextView = itemView.findViewById<TextView>(R.id.content)!!
        val descriptionTextView = itemView.findViewById<TextView>(R.id.itemDescription)!!

        fun bind(pdata : PlatesData, clickListener: (PlatesData) -> Unit)
        {
            itemView.setOnClickListener {
                clickListener(pdata)
                pdata.pos=adapterPosition
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val plateView = inflater.inflate(R.layout.plates_item, parent, false)
        return ViewHolder(plateView)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(data[position], clickListener)
        val plate: PlatesData = data[position]

        val nameTextView = viewHolder.nameTextView
        val descriptionTextView = viewHolder.descriptionTextView

        nameTextView.text = plate.name
        descriptionTextView.text = plate.description
    }
}