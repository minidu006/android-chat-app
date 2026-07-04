package com.example.chatchat.ui.profile

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.chatchat.databinding.FragmentEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private var selectedImageUri: Uri? = null
    private var existingImageUrl: String = ""

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                selectedImageUri = uri
                binding.ivProfile.setImageURI(uri)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loadCurrentProfile()

        binding.btnPickImage.setOnClickListener {
            imagePicker.launch("image/*")
        }

        binding.btnUpdateProfile.setOnClickListener {
            updateProfile()
        }
    }

    private fun loadCurrentProfile() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get().addOnSuccessListener { doc ->
            binding.etName.setText(doc.getString("name") ?: "")
            binding.etPhone.setText(doc.getString("phone") ?: "")
            binding.etBirthDate.setText(doc.getString("birthDate") ?: "")
            binding.etBio.setText(doc.getString("bio") ?: "")
            existingImageUrl = doc.getString("imageUrl") ?: ""
        }
    }

    private fun updateProfile() {
        val uid = auth.currentUser?.uid ?: return
        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val birthDate = binding.etBirthDate.text.toString().trim()
        val bio = binding.etBio.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Name is required", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = selectedImageUri
        if (uri != null) {
            val ref = storage.reference.child("profile_images/$uid.jpg")
            ref.putFile(uri)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { downloadUri ->
                        saveProfile(name, phone, birthDate, bio, downloadUri.toString())
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), it.message ?: "Image upload failed", Toast.LENGTH_SHORT).show()
                }
        } else {
            saveProfile(name, phone, birthDate, bio, existingImageUrl)
        }
    }

    private fun saveProfile(
        name: String,
        phone: String,
        birthDate: String,
        bio: String,
        imageUrl: String
    ) {
        val uid = auth.currentUser?.uid ?: return
        val email = auth.currentUser?.email ?: ""
        val map = mapOf(
            "uid" to uid,
            "name" to name,
            "email" to email,
            "phone" to phone,
            "birthDate" to birthDate,
            "bio" to bio,
            "imageUrl" to imageUrl
        )

        db.collection("users").document(uid)
            .set(map, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), it.message ?: "Update failed", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
