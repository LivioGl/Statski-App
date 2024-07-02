package com.example.statski

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AthletesViewModel : ViewModel(){
    val _athletesMap_ = MutableLiveData<MutableMap<String, Athlete>>()
    val _slopesMap_ = MutableLiveData<MutableMap<String, Slope>>()
    val athletesMap = mutableMapOf<String, Athlete>()
    val SlopesMap = mutableMapOf<String, Slope>()

    fun setSlopesMap(map: MutableMap<String, Slope>){
        SlopesMap.clear()
        SlopesMap.putAll(map)
    }

    fun setAthletesMap(map: MutableMap<String, Athlete>){
        athletesMap.clear()
        athletesMap.putAll(map)
    }

}
