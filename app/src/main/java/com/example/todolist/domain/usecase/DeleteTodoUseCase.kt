package com.example.todolist.domain.usecase

import com.example.todolist.domain.repository.TodoRepository

class DeleteTodoUseCase(
    private val repository: TodoRepository
) {
    suspend operator fun invoke(id: Int) = repository.deleteTodo(id)
}
