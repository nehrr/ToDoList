package com.example.android.todolist.network

data class Task(
    val id: String,
    val content: String,
    val completed: Boolean,
    val created: String,
    val due: TaskDate?,
    val due_date: String? = due?.date // hack
)

data class TaskDate(
    val date: String?
)

