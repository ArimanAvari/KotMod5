package com.example.todolist.presentation.viewmodel

import com.example.todolist.domain.usecase.DeleteTodoUseCase
import com.example.todolist.domain.usecase.ImportTodosUseCase
import com.example.todolist.domain.usecase.ObserveCompletedHighlightUseCase
import com.example.todolist.domain.usecase.ObserveTodoUseCase
import com.example.todolist.domain.usecase.ObserveTodosUseCase
import com.example.todolist.domain.usecase.SaveTodoUseCase
import com.example.todolist.domain.usecase.SetCompletedHighlightUseCase
import com.example.todolist.domain.usecase.ToggleTodoUseCase

data class TodoUseCases(
    val observeTodos: ObserveTodosUseCase,
    val observeTodo: ObserveTodoUseCase,
    val importTodos: ImportTodosUseCase,
    val saveTodo: SaveTodoUseCase,
    val deleteTodo: DeleteTodoUseCase,
    val toggleTodo: ToggleTodoUseCase,
    val observeCompletedHighlight: ObserveCompletedHighlightUseCase,
    val setCompletedHighlight: SetCompletedHighlightUseCase
)
