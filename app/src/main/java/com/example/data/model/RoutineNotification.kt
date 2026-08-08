package com.example.data.model

data class RoutineNotification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val isRead: Boolean = false,
    val category: String = "Exam Alert" // Exam Alert, System, Import
)
