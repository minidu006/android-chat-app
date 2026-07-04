package com.example.chatchat.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatchat.adapter.ChatAdapter
import com.example.chatchat.data.AppRepository
import com.example.chatchat.databinding.FragmentChatBinding
import com.example.chatchat.model.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var otherUserId: String
    private lateinit var otherUserName: String
    private lateinit var roomId: String
    private lateinit var adapter: ChatAdapter
    private var messagesListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        otherUserId = arguments?.getString("otherUserId").orEmpty()
        otherUserName = arguments?.getString("otherUserName").orEmpty()

        val myUid = auth.currentUser?.uid.orEmpty()
        roomId = AppRepository.buildRoomId(myUid, otherUserId)

        binding.tvChatName.text = otherUserName
        (requireActivity() as AppCompatActivity).supportActionBar?.title = otherUserName

        adapter = ChatAdapter(emptyList(), myUid)
        binding.recyclerMessages.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerMessages.adapter = adapter

        binding.btnSend.setOnClickListener { sendMessage() }

        listenMessages()
    }

    private fun listenMessages() {
        messagesListener = db.collection("chatRooms")
            .document(roomId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { value, _ ->
                val list = mutableListOf<ChatMessage>()
                value?.documents?.forEach { doc ->
                    list.add(
                        ChatMessage(
                            id = doc.id,
                            senderId = doc.getString("senderId") ?: "",
                            text = doc.getString("text") ?: "",
                            timestamp = doc.getLong("timestamp") ?: 0L
                        )
                    )
                }
                adapter.updateData(list)
                if (list.isNotEmpty()) {
                    binding.recyclerMessages.scrollToPosition(list.size - 1)
                }
            }
    }

    private fun sendMessage() {
        val text = binding.etMessage.text.toString().trim()
        val myUid = auth.currentUser?.uid ?: return
        if (text.isEmpty()) return

        db.collection("users").document(myUid).get().addOnSuccessListener { myDoc ->
            val myName = myDoc.getString("name") ?: (auth.currentUser?.email ?: "Me")
            val time = System.currentTimeMillis()

            val ordered = listOf(
                myUid to myName,
                otherUserId to otherUserName
            ).sortedBy { it.first }

            val roomData = mapOf(
                "roomId" to roomId,
                "participantIds" to ordered.map { it.first },
                "user1Id" to ordered[0].first,
                "user1Name" to ordered[0].second,
                "user2Id" to ordered[1].first,
                "user2Name" to ordered[1].second,
                "lastMessage" to text,
                "lastMessageTime" to time
            )

            db.collection("chatRooms").document(roomId)
                .set(roomData, SetOptions.merge())
                .addOnSuccessListener {
                    val msgRef = db.collection("chatRooms")
                        .document(roomId)
                        .collection("messages")
                        .document()

                    val msgData = mapOf(
                        "id" to msgRef.id,
                        "senderId" to myUid,
                        "text" to text,
                        "timestamp" to time
                    )

                    msgRef.set(msgData)
                        .addOnSuccessListener {
                            binding.etMessage.setText("")
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), it.message ?: "Send failed", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), it.message ?: "Could not open room", Toast.LENGTH_SHORT).show()
                }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), it.message ?: "Could not read your profile", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        messagesListener?.remove()
        messagesListener = null
        _binding = null
    }
}
