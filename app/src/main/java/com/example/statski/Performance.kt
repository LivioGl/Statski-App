package com.example.statski
import java.io.Serializable

import java.time.LocalDate

// Class to manage all race data in which an athlete took part

// Lots of the attribute will be used in thesis

data class Performance(
    val position : String,
    val total_time : String,
    val cup_points : String,
    val run1 : String?,
    val run2 : String?,
    val place : String,
    val date : String,
    val category : String
): Serializable