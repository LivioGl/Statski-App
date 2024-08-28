package com.example.statski

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentTransaction
import com.example.statski.databinding.ActivityAthleteStatsBinding
import com.google.gson.Gson
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
@RequiresApi(Build.VERSION_CODES.O)
class AthleteStats : AppCompatActivity() {
    lateinit var binding : ActivityAthleteStatsBinding
    //val viewModel_instance : AthletesViewModel by viewModels()


    @SuppressLint("SetTextI18n")
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

            val mostRecentPerformance = current_athlete.getMostRecentPerformanceDate()
            if (mostRecentPerformance != null){
                binding.lastRace.text = "Last race: "+mostRecentPerformance.toString()
            }

        }

        val backButton = binding.backButton
        backButton.setOnClickListener{
            finish()
        }
    }


}