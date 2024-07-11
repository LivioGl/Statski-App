package com.example.statski

import android.os.Parcel
import android.os.Parcelable

data class Race(
    val date : String,
    val place : String,
    val nation: String,
    val race_type : String
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(date)
        parcel.writeString(place)
        parcel.writeString(nation)
        parcel.writeString(race_type)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Race> {
        override fun createFromParcel(parcel: Parcel): Race {
            return Race(parcel)
        }

        override fun newArray(size: Int): Array<Race?> {
            return arrayOfNulls(size)
        }
    }
}