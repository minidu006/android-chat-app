package com.example.chatchat.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.chatchat.R
import com.example.chatchat.databinding.FragmentSignUpBinding
import com.example.chatchat.model.AppUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnSignUp.setOnClickListener { signUp() }

        binding.tvGoSignIn.setOnClickListener {
            findNavController().navigate(R.id.signInFragment)
        }
    }

    private fun signUp() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
            Toast.makeText(requireContext(), "Fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                val user = AppUser(
                    uid = uid,
                    name = name,
                    email = email,
                    phone = phone,
                    bio = "",
                    birthDate = "",
                    imageUrl = ""
                )
                db.collection("users").document(uid).set(user)
                    .addOnSuccessListener {
                        val options = NavOptions.Builder()
                            .setPopUpTo(R.id.welcomeFragment, true)
                            .build()
                        findNavController().navigate(R.id.homeFragment, null, options)
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), it.message ?: "Could not save profile", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), it.message ?: "Sign up failed", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
