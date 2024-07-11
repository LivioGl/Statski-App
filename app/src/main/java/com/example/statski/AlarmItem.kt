package com.example.statski

import java.time.LocalDateTime

// Class to manage data given as parameters to notifications

data class AlarmItem(
    val time: Long,
    val race: Race
)
