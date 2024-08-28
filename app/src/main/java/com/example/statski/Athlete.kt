package com.example.statski

import java.time.LocalDate

data class Athlete(
    val name: String,
    val nation: String,
    val birth: Int

    ){
    val performance_list = mutableListOf<Performance>()
    fun getMostRecentPerformanceDate(): LocalDate? {
        return performance_list
            .map { it.getDateAsLocalDate() }
            .maxOrNull()
    }
}
