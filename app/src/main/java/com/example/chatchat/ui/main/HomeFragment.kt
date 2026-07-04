package com.example.chatchat.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatchat.R
import com.example.chatchat.adapter.ContactsAdapter
import com.example.chatchat.databinding.FragmentHomeBinding
import com.example.chatchat.model.AppUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: ContactsAdapter
    private var contactsListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = ContactsAdapter(emptyList()) { user ->
            findNavController().navigate(
                R.id.chatFragment,
                bundleOf(
                    "otherUserId" to user.uid,
                    "otherUserName" to user.name
                )
            )
        }

        binding.recyclerContacts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerContacts.adapter = adapter

        binding.profileCard.setOnClickListener {
            findNavController().navigate(R.id.profileFragment)
        }

        loadMyProfile()
        listenForContacts()
    }

    private fun loadMyProfile() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get().addOnSuccessListener { doc ->
            binding.tvMyName.text = doc.getString("name") ?: "User"
            binding.tvMyEmail.text = doc.getString("email") ?: ""
        }
    }

    private fun listenForContacts() {
        val myUid = auth.currentUser?.uid ?: return
        contactsListener = db.collection("users")
            .addSnapshotListener { value, _ ->
                val users = mutableListOf<AppUser>()
                value?.documents?.forEach { doc ->
                    val user = doc.toObject(AppUser::class.java)
                    if (user != null && user.uid != myUid) {
                        users.add(user)
                    }
                }
                users.sortBy { it.name.lowercase() }
                adapter.updateData(users)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        contactsListener?.remove()
        contactsListener = null
        _binding = null
    }
}
