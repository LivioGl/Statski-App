package com.example.statski

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentTransaction
import com.example.statski.databinding.ActivityAthleteStatsBinding
import com.google.gson.Gson

class AthleteStats : AppCompatActivity() {
    lateinit var binding : ActivityAthleteStatsBinding
    val viewModel_instance : AthletesViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAthleteStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()


        val bundle : Bundle? = intent.extras
        val athlete_picked = bundle!!.getString("athlete_picked")
        val Gson = Gson()
        val current_athlete : Athlete = Gson.fromJson(athlete_picked, Athlete::class.java)

        if (current_athlete != null){
            binding.title.text = current_athlete.name
            binding.nation.text = current_athlete.nation
            binding.birth.text = current_athlete.birth.toString()
        }

        val backButton = binding.backButton
        backButton.setOnClickListener{
            finish()
        }
    }
}