package com.example.android.todolist.network

data class Task(
    val id: String,
    val content: String,
    val completed: Boolean,
    val created: String,
    val due_date: String?
)

