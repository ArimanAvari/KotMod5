package com.example.todolist.domain.usecase

import com.example.todolist.domain.repository.TodoRepository

class SetCompletedHighlightUseCase(
    private val repository: TodoRepository
) {
    suspend operator fun invoke(enabled: Boolean) = repository.setCompletedHighlight(enabled)
}
