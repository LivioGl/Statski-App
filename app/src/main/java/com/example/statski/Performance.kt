package com.example.statski

import java.time.LocalDate

data class Performance(
    val position : String,
    val total_time : String,
    val cup_points : String,
    val run1 : String?,
    val run2 : String?,
    val place : String,
    val date : String,
    val category : String
)