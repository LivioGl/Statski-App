package com.example.statski

import android.icu.util.Calendar
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AthletesViewModel : ViewModel(){

    val athletesMap = mutableMapOf<String, Athlete>()
    val calendar = mutableListOf<Race>()

    private val selected_Athlete = MutableLiveData<Athlete>()
    val selectedAthlete: LiveData<Athlete> get() = selected_Athlete

    fun selectAthlete(athlete: Athlete) {
        selected_Athlete.value = athlete
    }

    // Live data to observe next race
    private val nextRace = MutableLiveData<Pair<List<Race>, Race?>>()

    fun setAthletesMap(map: MutableMap<String, Athlete>){
        athletesMap.clear()
        athletesMap.putAll(map)
    }

    fun setCalendar(races_list : MutableList<Race>){
        calendar.clear()
        calendar.addAll(races_list)
    }
    private fun updateRacesData(calendar: List<Race>){
        val today = Calendar.getInstance().time

    }
}
