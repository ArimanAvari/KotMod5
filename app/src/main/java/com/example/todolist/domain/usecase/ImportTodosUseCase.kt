package com.example.todolist.domain.usecase

import com.example.todolist.domain.repository.TodoRepository

class ImportTodosUseCase(
    private val repository: TodoRepository
) {
    suspend operator fun invoke() = repository.importTodosIfNeeded()
}
