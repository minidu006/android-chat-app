package com.example.chatchat.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatchat.R
import com.example.chatchat.adapter.ConversationAdapter
import com.example.chatchat.databinding.FragmentMessagesBinding
import com.example.chatchat.model.Conversation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class MessagesFragment : Fragment() {

    private var _binding: FragmentMessagesBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: ConversationAdapter
    private var fullList = listOf<Conversation>()
    private var conversationsListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = ConversationAdapter(emptyList()) { conversation ->
            findNavController().navigate(
                R.id.chatFragment,
                bundleOf(
                    "otherUserId" to conversation.otherUserId,
                    "otherUserName" to conversation.otherUserName
                )
            )
        }

        binding.recyclerConversations.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerConversations.adapter = adapter

        binding.etSearch.doAfterTextChanged { editable ->
            val q = editable.toString().trim().lowercase()
            if (q.isEmpty()) {
                adapter.updateData(fullList)
            } else {
                adapter.updateData(
                    fullList.filter {
                        it.otherUserName.lowercase().contains(q) ||
                            it.lastMessage.lowercase().contains(q)
                    }
                )
            }
        }

        listenForConversations()
    }

    private fun listenForConversations() {
        val uid = auth.currentUser?.uid ?: return
        conversationsListener = db.collection("chatRooms")
            .whereArrayContains("participantIds", uid)
            .addSnapshotListener { value, _ ->
                val list = mutableListOf<Conversation>()
                value?.documents?.forEach { doc ->
                    val user1Id = doc.getString("user1Id") ?: ""
                    val user2Id = doc.getString("user2Id") ?: ""
                    val user1Name = doc.getString("user1Name") ?: ""
                    val user2Name = doc.getString("user2Name") ?: ""
                    val otherUserId = if (uid == user1Id) user2Id else user1Id
                    val otherUserName = if (uid == user1Id) user2Name else user1Name
                    list.add(
                        Conversation(
                            roomId = doc.id,
                            otherUserId = otherUserId,
                            otherUserName = otherUserName,
                            lastMessage = doc.getString("lastMessage") ?: "",
                            lastMessageTime = doc.getLong("lastMessageTime") ?: 0L
                        )
                    )
                }
                fullList = list.sortedByDescending { it.lastMessageTime }
                adapter.updateData(fullList)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        conversationsListener?.remove()
        conversationsListener = null
        _binding = null
    }
}
