package com.example.statski

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
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
import androidx.fragment.app.activityViewModels
import com.example.statski.databinding.FragmentHomeBinding
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.abs

class HomeFragment : Fragment() {

    private lateinit var binding : FragmentHomeBinding
    val viewModel_instance : AthletesViewModel by activityViewModels()
    private var Races_calendar = mutableListOf<Race>()
    private lateinit var alarmScheduler: AlarmScheduler

    companion object {
        private const val NOTIFICATION_PERMISSION_CODE = 1001
    }

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "AlarmChannel"
            val descriptionText = "Channel for alarm notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("alarm_channel", name, importance).apply {
                description = descriptionText
            }
        }
        val WomenRace : Race? = FindNearestRaces(Races_calendar).first
        val MenRace : Race? = FindNearestRaces(Races_calendar).second
        //val race1 = WomenRace?.let { Race(WomenRace?.date.toString(), it.place, WomenRace.nation, WomenRace.nation) }

        binding.nextWomenRace.text = WomenRace?.race_type
        binding.whenNextRace.text = WomenRace?.date
        binding.whereNextRace.text = "${WomenRace?.place}, ${WomenRace?.nation}"

        binding.setNotify.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
                showNotification(WomenRace = true)
            } else {
                // TODO AGGIUNGI TOAST (Bono sto toast)
            }
        }
    }

    // FUNCTIONS TO EXTRACT NEAREST RACES FROM CALENDAR
    private fun FindNearestRaces(calendar: MutableList<Race>): Pair<Race?, Race?> {
        val womenRaces = calendar.filter { it.race_type.contains("Women") }
        val menRaces = calendar.filter { it.race_type.contains("Men") }

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


//


    private fun showNotification(WomenRace: Boolean){
        val race = if(WomenRace) FindNearestRaces(Races_calendar).first else FindNearestRaces(Races_calendar).second
        race?.let{
            val raceDate = LocalDate.parse(it.date).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val alarmItem = AlarmItem(time = raceDate, race = it)
            alarmScheduler.schedule(alarmItem)
            Toast.makeText(requireContext(), "Notification set for ${it.race_type} on ${it.date}", Toast.LENGTH_SHORT).show()
        }
//
    }
}