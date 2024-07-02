package com.example.statski

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AthletesViewModel : ViewModel(){
    val _athletesMap_ = MutableLiveData<MutableMap<String, Athlete>>()
    val athletesMap = mutableMapOf<String, Athlete>()


    // val filteredAthletes: LiveData<List<Athlete>> get() = _filteredAthletes

    fun setAthletesMap(map: MutableMap<String, Athlete>){
        athletesMap.clear()
        athletesMap.putAll(map)
    }

}
