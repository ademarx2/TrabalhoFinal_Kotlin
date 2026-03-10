package com.rameda38.trabalhofinal.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey
    val id: String = "", 
    val description: String = "",
    val value: Double = 0.0,
    val userId: String = ""
) : Serializable
