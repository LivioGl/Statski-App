package com.example.statski

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Locale

data class Athlete(
    val name: String,
    val nation: String,
    val birth: Int

    ){
    val performance_list = mutableListOf<Performance>()

    // Get most recent race
    fun getMostRecentPerformanceDate(): LocalDate? {
        return performance_list
            .map { it.getDateAsLocalDate() }
            .maxOrNull()
    }

    // Get last 5 races
    fun GetLastFiveRaces(performance: List<Performance>): List<Performance>{
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH)

        val sortedPerformance = performance.sortedByDescending { performance ->
            dateFormat.parse(performance.date)
        }
        return sortedPerformance.take(5)
    }

    // Get last 5 races, based on category
    fun filterPerformanceByCategory(category: String): List<Performance> {
        return when (category){
            "Downhill" -> performance_list.filter{it.category == "Men's Downhill" || it.category == "Women's Downhill"}
            "Super G" -> performance_list.filter{it.category == "Men's Super G" || it.category == "Women's Super G"}
            "Giant Slalom" -> performance_list.filter{it.category == "Men's Giant Slalom" || it.category == "Women's Giant Slalom"}
            "Slalom" -> performance_list.filter{it.category == "Men's Slalom" || it.category == "Women's Slalom"}
            "Alpine Combined"-> performance_list.filter{it.category == "Men's Alpine combined" || it.category == "Women's Alpine combined"}

            else -> {
                emptyList<Performance>()
            }
        }


    }



    // Get number of victories
    fun CountVictories(): Int{
        return performance_list.count{it.position == "1"}
    }

    // Get number of podiums
    fun CountPodiums(): Int{
        return performance_list.count{it.position == "1" || it.position == "2" || it.position == "3"}
    }

    fun CountTop5(): Int{
        return performance_list.count{it.position == "1" || it.position == "2" || it.position == "3" || it.position == "4" || it.position == "5"}
    }

    fun CountTop10(): Int{
        return performance_list.count{it.position == "1" || it.position == "2" || it.position == "3" || it.position == "4" || it.position == "5"
                || it.position == "6" || it.position == "7" || it.position == "8" || it.position == "9" || it.position == "10"
        }
    }

    // Get total amount of points
    fun SumPoints(): Int{
        var total_points : Int = 0
        for (element in performance_list){
            var p = element.cup_points.toIntOrNull()
            if (p != null) {
                total_points+=p
            }
        }
        return total_points
    }


}
