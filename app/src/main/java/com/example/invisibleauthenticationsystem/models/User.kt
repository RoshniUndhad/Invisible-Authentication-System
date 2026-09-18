package com.example.invisibleauthenticationsystem.models

data class User(
    val id: Long = -1,
    val fullName: String,
    val email: String,
    val phone: String,
    val calculatorPin: String,
    val avgDwellTime: Double,
    val avgFlightTime: Double,
    val touchPressure: Double,
    val isBiometricEnabled: Boolean = false
)

