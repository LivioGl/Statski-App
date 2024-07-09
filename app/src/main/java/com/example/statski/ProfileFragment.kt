package com.example.statski

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.example.statski.databinding.FragmentProfileBinding
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest

class ProfileFragment : Fragment() {

    private lateinit var signOutButton : Button
    private lateinit var binding : FragmentProfileBinding
    private lateinit var user : FirebaseUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        user = FirebaseAuth.getInstance().currentUser?: throw IllegalStateException("User not logged")
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.signOutButton.setOnClickListener {
            AuthUI.getInstance()
                .signOut(requireContext()!!)
                .addOnCompleteListener {
                    val intent = Intent(requireContext(), WelcomeActivity::class.java)
                    startActivity(intent)
                }
        }
        binding.userName.setText("Hi, "+ user.displayName)
        binding.editName.setText(user.displayName?: "")
        binding.editEmail.setText(user.email?: "")
        binding.buttonSubmit.setOnClickListener{
            UpdateMyProfile(view)


        }


    }

    private fun UpdateMyProfile(view: View?){
        // Get inputs from textViews
        val newName = binding.editName.text.toString().trim()
        val newEmail = binding.editEmail.text.toString().trim()
        val password = binding.editPassword.text.toString().trim()
        // Input Checks: No empty field
        if(newName.isEmpty() || newEmail.isEmpty() || password.isEmpty()){
            Toast.makeText(requireContext(), "There are empty fields", Toast.LENGTH_SHORT).show()
            return
        }
        // Input Checks: name validity
        if(newName.split(" ").size < 2){
            Toast.makeText(requireContext(), "Enter a valid name", Toast.LENGTH_SHORT).show()
            return
        }
        // Input Checks: email validity
        if(!(Patterns.EMAIL_ADDRESS.matcher(newEmail).matches())){
            Toast.makeText(requireContext(), "Enter a valid email", Toast.LENGTH_SHORT).show()
        }
        // Input Checks: password matching
        val credential = EmailAuthProvider.getCredential(user.email!!, password)
        user.reauthenticate(credential)
            .addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    if (newName != user.displayName) {
                        val update = UserProfileChangeRequest.Builder()
                            .setDisplayName(newName)
                            .build()
                        user.updateProfile(update)
                            .addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    Toast.makeText(requireContext(), "Profile Updated", Toast.LENGTH_SHORT).show()
                                    binding.editPassword.setText("")
                                } else {
                                    Toast.makeText(requireContext(), "An error occurred", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }

                    // Update email if different from the previous
                    if (newEmail != user.email) {
                        user.updateEmail(newEmail)
                            .addOnCompleteListener { emailTask ->
                                if (emailTask.isSuccessful) {
                                    Toast.makeText(requireContext(), "Email Updated", Toast.LENGTH_SHORT).show()
                                    binding.editPassword.setText("")

                                } else {
                                    Toast.makeText(requireContext(), "An error occurred", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                } else {
                    Toast.makeText(requireContext(), "An error occurred", Toast.LENGTH_SHORT).show()
                }
            }
    }

}