package com.example.statski

data class Athlete(
    val name: String,
    val nation: String,

    ){
    val performance_list = mutableListOf<Performance>()
}
