package com.example.statski

import android.annotation.SuppressLint
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
    private lateinit var currentSlope : Slope
    private lateinit var winnersList: MutableList<Athlete>

    @SuppressLint("DefaultLocale", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNextRacePreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        val isWomenRace = intent.getBooleanExtra("IsWomenRace", false)
        if(!isWomenRace){
            val menRaceJson = intent.getStringExtra("Men Race")
            val whoToWatchMenJson = intent.getStringExtra("Who to watch men")
            val slopeJson = intent.getStringExtra("Men slope")

            if (menRaceJson != null && whoToWatchMenJson != null) {
                val gson = Gson()

                currentSlope = gson.fromJson(slopeJson, Slope::class.java)
                currentRace = gson.fromJson(menRaceJson, Race::class.java)

                val listType = object : TypeToken<MutableList<Athlete>>() {}.type
                winnersList = gson.fromJson(whoToWatchMenJson, listType)
            }

            val athleteWithMostWins = winnersList.maxByOrNull { athlete ->
                athlete.performance_list.count { it.place == currentSlope.location+"_m" && it.position == "1" }
            }

            val athleteWithMostPodiums = winnersList.maxByOrNull { athlete ->
                athlete.performance_list.count { it.place == currentSlope.location+"_m" && (it.position == "1" || it.position == "2" || it.position == "3") }
            }

            // Calculating avg time delay of second and third place from winning athlete
            val secondPlacePerformances = winnersList.flatMap {
                athlete -> athlete.performance_list.filter { it.position == "2" && it.place == currentSlope.location+"_m"}
                .map{it.total_time.replace("+", "").toDouble()}
            }
            val result_second_place = secondPlacePerformances.sum() / secondPlacePerformances.size
            var avgDelaySecondPlace = String.format("+%.2f", result_second_place)


            val thirdPlacePerformances = winnersList.flatMap {
                    athlete -> athlete.performance_list.filter { it.position == "3" && it.place == currentSlope.location+"_m"}
                .map{it.total_time.replace("+", "").toDouble()}
            }
            val result_third_place = thirdPlacePerformances.sum() / thirdPlacePerformances.size
            var avgDelayThirdPlace = String.format("+%.2f", result_third_place)


            // Calculating data about national teams
            val historyPodiumPerformance = winnersList.flatMap {
                    athlete -> athlete.performance_list.filter {
                it.place == currentSlope.location+"_m" && (it.position == "1" || it.position == "2" || it.position == "3")
            }.map{
                athlete.nation
            }
            }
            val nationPodiumsCount = historyPodiumPerformance.groupingBy { it }.eachCount()
            val top3Nations : List<Map.Entry<String, Int>> = nationPodiumsCount.entries
                .sortedByDescending { it.value }
                .take(3)


            // Calculating data and stats to fill TextViews
            if (athleteWithMostWins != null && athleteWithMostPodiums != null) {
                val wins = athleteWithMostWins.performance_list.count{it.place == currentSlope.location+"_m" && it.position == "1"}
                val podiums = athleteWithMostPodiums.performance_list.count{it.place == currentSlope.location+"_m" && (it.position == "1" || it.position == "2" || it.position == "3")}
                if (athleteWithMostWins.name == athleteWithMostPodiums.name){
                    binding.mostWinsAthlete.text = athleteWithMostWins.name+" is the king of "+currentSlope.name+". He has "+podiums+" podiums and "+wins+" victories on the "+currentSlope.name+": definitely the athlete to beat"
                    binding.mostPodiumsAthlete.text = "We expect a great performance from him"
                } else {
                    binding.mostWinsAthlete.text = athleteWithMostWins.name+", won "+wins.toString()+ " times on the "+currentSlope.name
                    binding.mostPodiumsAthlete.text = athleteWithMostPodiums.name+" secured "+podiums.toString()+" podiums on the "+currentSlope.name+". He is always competitive here"
                }
            }

            binding.avgDelaySecond.text =
                "Based on previous editions, the average gap from first place is ${avgDelaySecondPlace}s for second place, while it is ${avgDelayThirdPlace}s for third place"

            binding.bestTeamsList.text = "Based on previous editions, the team which had best performances is "+top3Nations[0].key+", with "+top3Nations[0].value+" podiums. Then also "+top3Nations[1].key+" and "+top3Nations[2].key+" which have "+top3Nations[1].value+ " and "+top3Nations[2].value+" podiums respectively."
        }
        else {
            val womenRaceJson = intent.getStringExtra("Women Race")
            val whoToWatchWomenJson = intent.getStringExtra("Who to watch women")
            val slopeJson = intent.getStringExtra("Women slope")

            if (womenRaceJson != null && whoToWatchWomenJson != null) {
                val gson = Gson()

                currentRace = gson.fromJson(womenRaceJson, Race::class.java)
                currentSlope = gson.fromJson(slopeJson, Slope::class.java)

                val listType = object : TypeToken<MutableList<Athlete>>() {}.type
                winnersList = gson.fromJson(whoToWatchWomenJson, listType)
            }

            val athleteWithMostWins = winnersList.maxByOrNull { athlete ->
                athlete.performance_list.count { it.place == currentSlope.location+"_f" && it.position == "1" }
            }

            val athleteWithMostPodiums = winnersList.maxByOrNull { athlete ->
                athlete.performance_list.count { it.place == currentSlope.location+"_f" && (it.position == "1" || it.position == "2" || it.position == "3") }
            }

            // Calculating avg time delay of second and third place from winning athlete
            val secondPlacePerformances = winnersList.flatMap {
                    athlete -> athlete.performance_list.filter { it.position == "2" && it.place == currentSlope.location+"_f"}
                .map{it.total_time.replace("+", "").toDouble()}
            }
            var result_second_place = secondPlacePerformances.sum() / secondPlacePerformances.size
            var avgDelaySecondPlace = String.format("+%.2f", result_second_place)


            val thirdPlacePerformances = winnersList.flatMap {
                    athlete -> athlete.performance_list.filter { it.position == "3" && it.place == currentSlope.location+"_f"}
                .map{it.total_time.replace("+", "").toDouble()}
            }
            var result_third_place = thirdPlacePerformances.sum() / thirdPlacePerformances.size
            var avgDelayThirdPlace = String.format("+%.2f", result_third_place)


            // Calculating data about national teams
            val historyPodiumPerformance = winnersList.flatMap {
                athlete -> athlete.performance_list.filter {
                    it.place == currentSlope.location+"_f" && (it.position == "1" || it.position == "2" || it.position == "3")
                }.map{
                    athlete.nation
                }
            }
            val nationPodiumsCount = historyPodiumPerformance.groupingBy { it }.eachCount()
            val top3Nations : List<Map.Entry<String, Int>> = nationPodiumsCount.entries
                .sortedByDescending { it.value }
                .take(3)

            // TODO fix in funzione delle ultime modifiche

            // Calculating data and stats to fill TextViews
            if (athleteWithMostWins != null && athleteWithMostPodiums != null) {
                val wins = athleteWithMostWins.performance_list.count{it.place == currentSlope.location+"_f" && it.position == "1"}
                val podiums = athleteWithMostPodiums.performance_list.count{it.place == currentSlope.location+"_f" && (it.position == "1" || it.position == "2" || it.position == "3")}
                if (athleteWithMostWins.name == athleteWithMostPodiums.name){
                    binding.mostWinsAthlete.text = athleteWithMostWins.name+" is the queen of "+currentSlope.name+". She has "+podiums+" podiums and "+wins+" victories on the "+currentSlope.name+": definitely the athlete to beat"
                    binding.mostPodiumsAthlete.text = "We expect a great performance from her"
                } else {
                    binding.mostWinsAthlete.text = athleteWithMostWins.name+", won "+wins.toString()+ " times on the "+currentSlope.name
                    binding.mostPodiumsAthlete.text = athleteWithMostPodiums.name+", who secured "+podiums.toString()+" podiums on the "+currentSlope.name+". She is always competitive here"
                }
            }
            binding.avgDelaySecond.text =
                "Based on previous editions, the average gap from first place is ${avgDelaySecondPlace}s for second place, while it is ${avgDelayThirdPlace}s for third place"

            binding.bestTeamsList.text = "Based on previous editions, the team which had best performances is "+top3Nations[0].key+", with "+top3Nations[0].value+" podiums. Then also "+top3Nations[1].key+" and "+top3Nations[2].key+" which have "+top3Nations[1].value+ " and "+top3Nations[2].value+" podiums respectively."
        }

        binding.mainTitle.text = currentRace.place+" "+currentRace.race_type

        // Handling back button
        val backButton = binding.backButton
        backButton.setOnClickListener {
            finish()
        }

    }
}