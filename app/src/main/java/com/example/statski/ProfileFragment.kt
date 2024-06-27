package com.example.statski

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.firebase.ui.auth.AuthUI

class ProfileFragment : Fragment() {

    private lateinit var signOutButton : Button
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        signOutButton = view.findViewById(R.id.sign_out_button)
        signOutButton.setOnClickListener {
            AuthUI.getInstance()
                .signOut(requireContext()!!)
                .addOnCompleteListener {
                    val intent = Intent(requireContext(), WelcomeActivity::class.java)
                    startActivity(intent)
                }
        }
    }

}