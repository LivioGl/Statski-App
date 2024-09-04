package com.example.statski

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.statski.databinding.ActivityNextRacePreviewBinding
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.statski.databinding.ActivityAthleteStatsBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class NextRacePreview : AppCompatActivity() {

    lateinit var binding : ActivityNextRacePreviewBinding
    private lateinit var currentRace : Race
    private lateinit var winnersList: MutableList<Athlete>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNextRacePreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()


        val isWomenRace = intent.getBooleanExtra("IsWomenRace", false)
        if(!isWomenRace){
            val menRaceJson = intent.getStringExtra("Men Race")
            val whoToWatchMenJson = intent.getStringExtra("Who to watch men")

            if (menRaceJson != null && whoToWatchMenJson != null) {
                val gson = Gson()

                val Race = gson.fromJson(menRaceJson, Race::class.java)
                currentRace = Race

                val listType = object : TypeToken<MutableList<Athlete>>() {}.type
                val List: MutableList<Athlete> = gson.fromJson(whoToWatchMenJson, listType)
                winnersList = List

            }

            val athleteWithMostWins = winnersList.maxByOrNull { it.performance_list.count { perf -> perf.position == "1" } }
            val athleteWithMostPodiums = winnersList.maxByOrNull { it.performance_list.count { perf -> perf.position in listOf("1", "2", "3") } }

            if (athleteWithMostWins != null){
                // Filling text views
                binding.mostWinsAthlete.text = athleteWithMostWins.name
            }
        }
        else{
            val womenRaceJson = intent.getStringExtra("Women Race")
            val whoToWatchWomenJson = intent.getStringExtra("Who to watch women")

            if (womenRaceJson != null && whoToWatchWomenJson != null) {
                val gson = Gson()

                val Race = gson.fromJson(womenRaceJson, Race::class.java)
                currentRace = Race

                val listType = object : TypeToken<MutableList<Athlete>>() {}.type
                val List: MutableList<Athlete> = gson.fromJson(whoToWatchWomenJson, listType)
                winnersList = List

            }
            val athleteWithMostWins = winnersList.maxByOrNull { it.performance_list.count { perf -> perf.position == "1" } }
            val athleteWithMostPodiums = winnersList.maxByOrNull { it.performance_list.count { perf -> perf.position in listOf("1", "2", "3") } }

            if (athleteWithMostWins != null){
                // Filling text views

                binding.mostWinsAthlete.text = athleteWithMostWins.name
            }
        }
        binding.mainTitle.text = currentRace.place+" "+currentRace.race_type


        // Handling back button
        val backButton = binding.backButton
        backButton.setOnClickListener {
            finish()
        }


    }
}