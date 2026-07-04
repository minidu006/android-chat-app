package com.example.chatchat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.chatchat.R
import com.example.chatchat.databinding.ItemCallLogBinding
import com.example.chatchat.model.CallLogItem

class CallLogAdapter(private val list: List<CallLogItem>) :
    RecyclerView.Adapter<CallLogAdapter.CallLogViewHolder>() {

    inner class CallLogViewHolder(val binding: ItemCallLogBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallLogViewHolder {
        val binding = ItemCallLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CallLogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CallLogViewHolder, position: Int) {
        val item = list[position]
        holder.binding.tvName.text = item.name
        holder.binding.tvDate.text = item.date
        holder.binding.tvType.text = if (item.missed) "Missed" else "Received"
        val color = if (item.missed) R.color.danger else R.color.primary
        holder.binding.tvType.setTextColor(ContextCompat.getColor(holder.itemView.context, color))
    }

    override fun getItemCount(): Int = list.size
}
