package com.example.statski
import java.io.Serializable

data class Athlete(
    val name: String,
    val nation: String,
    val birth: Int

    ): Serializable{
    val performance_list = mutableListOf<Performance>()
}
