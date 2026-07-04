package com.example.chatchat.adapter

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatchat.databinding.ItemConversationBinding
import com.example.chatchat.model.Conversation
import java.util.Date

class ConversationAdapter(
    private var list: List<Conversation>,
    private val onClick: (Conversation) -> Unit
) : RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder>() {

    inner class ConversationViewHolder(val binding: ItemConversationBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val binding = ItemConversationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ConversationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        val item = list[position]
        holder.binding.tvName.text = item.otherUserName
        holder.binding.tvLastMessage.text = item.lastMessage
        holder.binding.tvTime.text = DateFormat.format("hh:mm a", Date(item.lastMessageTime))
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<Conversation>) {
        list = newList
        notifyDataSetChanged()
    }
}
