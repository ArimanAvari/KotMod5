package com.example.todolist.data.model

import com.example.todolist.data.local.TodoEntity
import com.example.todolist.domain.model.TodoItem

fun TodoItemDto.toEntity(): TodoEntity {
    return TodoEntity(
        id = id,
        title = title,
        description = description,
        isCompleted = isCompleted
    )
}

fun TodoEntity.toDomain(): TodoItem {
    return TodoItem(
        id = id,
        title = title,
        description = description,
        isCompleted = isCompleted
    )
}

fun TodoItem.toEntity(): TodoEntity {
    return TodoEntity(
        id = id,
        title = title,
        description = description,
        isCompleted = isCompleted
    )
}
