package com.example.todolist.domain.usecase

import com.example.todolist.domain.model.TodoItem
import com.example.todolist.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow

class ObserveTodoUseCase(
    private val repository: TodoRepository
) {
    operator fun invoke(id: Int): Flow<TodoItem?> = repository.observeTodo(id)
}
