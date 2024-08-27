package com.example.statski

data class Athlete(
    val name: String,
    val nation: String,
    val birth: Int

    ){
    val performance_list = mutableListOf<Performance>()
}
