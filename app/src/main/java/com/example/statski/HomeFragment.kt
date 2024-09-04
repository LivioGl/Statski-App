package com.example.statski

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getString
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.activityViewModels
import com.example.statski.databinding.FragmentHomeBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.gson.Gson
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import kotlin.math.abs

class HomeFragment : Fragment() {

    private lateinit var binding : FragmentHomeBinding
    val viewModel_instance : AthletesViewModel by activityViewModels()
    private var Races_calendar = mutableListOf<Race>()
    private var winnersList_f = mutableListOf<Athlete>()
    private var winnersList_m = mutableListOf<Athlete>()
    private lateinit var alarmScheduler: AndroidAlarmScheduler
    private lateinit var db : FirebaseFirestore
    private lateinit var firebaseAuth : FirebaseAuth
    private lateinit var user : FirebaseUser


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root


    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Races_calendar = viewModel_instance.calendar
        // Get Firebase instance
        db = Firebase.firestore
        firebaseAuth = FirebaseAuth.getInstance()

        // Call functions to get the nearest races from today
        val WomenRace : Race? = FindNearestRaces(Races_calendar).first
        val MenRace : Race? = FindNearestRaces(Races_calendar).second

        val today = LocalDate.now()
        if (today.month >= Month.APRIL && today.month <= Month.SEPTEMBER){
            binding.middleText.text = "Next season is coming! Here there are first races"
        }
        else{
            binding.middleText.text = "Upcoming races:"
        }

        // Get Athletes List from viewModel
        var AthletesList : MutableList<Athlete> = viewModel_instance.athletesMap.values.toMutableList()
        // Fill TextViews with nearest men's race and nearest women's race attributes
        binding.nextWomenRace.text = WomenRace?.race_type
        binding.whenNextRace.text = WomenRace?.date
        binding.whereNextRaceWomen.text = "${WomenRace?.place}, ${WomenRace?.nation}"

        binding.nextMenRace.text = MenRace?.race_type
        binding.whenNextRaceMen.text = MenRace?.date
        binding.whereNextRaceMen.text = "${MenRace?.place}, ${MenRace?.nation}"

        alarmScheduler = AndroidAlarmScheduler(requireContext())

        // Set buttons for notifications
        binding.setNotify.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
                showNotification(WomenRace = true)
            } else {
                Toast.makeText(requireContext(), "Please give notification permission", Toast.LENGTH_SHORT).show()
            }
        }

        binding.notifyButtonMen.setOnClickListener{
            if(ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED){
                showNotification(WomenRace = false)
            } else{
                Toast.makeText(requireContext(), "Please give notification permission", Toast.LENGTH_SHORT).show()
            }

        }

        // OnClickListener for Next Preview Activity
        binding.preview1.setOnClickListener {
            val women_race = Gson().toJson(WomenRace)
            if(WomenRace != null){
                winnersList_f = AthletesList.filter{
                    it.performance_list.filter{
                        it.place == WomenRace.place+"_f" &&( it.cup_points == "100" || it.cup_points == "80" || it.cup_points == "60")
                    }.isNotEmpty()
                }.toMutableList()

                // winnersList.size
                val who_to_watch_women = Gson().toJson(winnersList_f)
                val intent = Intent(activity, NextRacePreview::class.java)
                intent.putExtra("Women Race", women_race)
                intent.putExtra("Who to watch women", who_to_watch_women)
                intent.putExtra("IsWomenRace", true)
                startActivity(intent)
            }

        }
        binding.preview2.setOnClickListener {
            val men_race = Gson().toJson(MenRace)

            if(MenRace != null){
                winnersList_m = AthletesList.filter{
                    it.performance_list.filter{
                        it.place == MenRace.place+"_m" &&( it.cup_points == "100" || it.cup_points == "80" || it.cup_points == "60")
                    }.isNotEmpty()
                }.toMutableList()

                // winnersList.size
                val who_to_watch_men = Gson().toJson(winnersList_m)
                val intent = Intent(activity, NextRacePreview::class.java)
                intent.putExtra("Men Race", men_race)
                intent.putExtra("Who to watch men", who_to_watch_men)
                intent.putExtra("IsWomenRace", false)
                startActivity(intent)

            }
        }
    }

    // FUNCTIONS TO EXTRACT NEAREST RACES FROM CALENDAR
    private fun FindNearestRaces(calendar: MutableList<Race>): Pair<Race?, Race?> {
        val womenRaces = calendar.filter { it.race_type.contains("Women") }
        val menRaces = calendar.filter { it.race_type.contains("Men") }
        // Divide men and women races
        val nearestWomenRace = findNearestRace(womenRaces)
        val nearestMenRace = findNearestRace(menRaces)

        return Pair(nearestWomenRace, nearestMenRace)

    }

    private fun findNearestRace(calendar: List<Race>): Race?{
        val today = LocalDate.now()
        var nearestRace: Race? = null
        var minDaysDiff : Long = Long.MAX_VALUE
        for (race in calendar){
            val raceDate = LocalDate.parse(race.date)
            val daysDiff = abs(daysBetween(today, raceDate))
            if(daysDiff < minDaysDiff){
                minDaysDiff = daysDiff
                nearestRace = race
            }
        }
        return nearestRace
    }

    private fun daysBetween(date1: LocalDate, date2: LocalDate): Long{
        return ChronoUnit.DAYS.between(date1, date2)
    }

    // Notification Manager
    private fun showNotification(WomenRace: Boolean) {
        val race = if(WomenRace) FindNearestRaces(Races_calendar).first else FindNearestRaces(Races_calendar).second
        race?.let{
            val raceDate = LocalDate.parse(it.date).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            // For testing: substitute raceDate with a unix Value
            val alarmItem = AlarmItem(time = raceDate, race = it)
            alarmScheduler.schedule(alarmItem)
            Toast.makeText(requireContext(), "Notification set for ${it.race_type} on ${it.date}", Toast.LENGTH_SHORT).show()
        }

    }



}