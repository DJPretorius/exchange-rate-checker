package com.example.exchangerates.modles.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "currencies")
data class Currency (
    @PrimaryKey(autoGenerate = true)
    val id : Int = 0,
    val symbol : String,
    val description : String
) {

    override fun toString(): String {
        return "$symbol - $description"
    }
}