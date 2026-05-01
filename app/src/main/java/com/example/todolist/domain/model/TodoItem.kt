package com.example.todolist.domain.model

data class TodoItem(
    val id: Int = 0,
    val title: String,
    val description: String,
    val isCompleted: Boolean
)
