package com.example.statski

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SlopesViewModel: ViewModel() {
    val _slopesMap_ = MutableLiveData<MutableMap<String, Slope>>()
    val SlopesMap = mutableMapOf<String, Slope>()
    fun setSlopesMap(map: MutableMap<String, Slope>){
        SlopesMap.clear()
        SlopesMap.putAll(map)
    }

}