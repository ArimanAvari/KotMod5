package com.example.todolist.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.domain.model.TodoItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TodoViewModel(
    private val useCases: TodoUseCases
) : ViewModel() {

    val todos: StateFlow<List<TodoItem>> = useCases.observeTodos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val highlightCompleted: StateFlow<Boolean> = useCases.observeCompletedHighlight()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    init {
        viewModelScope.launch {
            useCases.importTodos()
        }
    }

    fun observeTodo(todoId: Int): Flow<TodoItem?> {
        return useCases.observeTodo(todoId)
    }

    fun toggleTodo(id: Int) {
        viewModelScope.launch {
            useCases.toggleTodo(id)
        }
    }

    fun setHighlightCompleted(enabled: Boolean) {
        viewModelScope.launch {
            useCases.setCompletedHighlight(enabled)
        }
    }

    fun saveTodo(
        id: Int,
        title: String,
        description: String,
        isCompleted: Boolean,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            useCases.saveTodo(
                TodoItem(
                    id = id,
                    title = title,
                    description = description,
                    isCompleted = isCompleted
                )
            )
            onDone()
        }
    }

    fun deleteTodo(id: Int, onDone: () -> Unit) {
        viewModelScope.launch {
            useCases.deleteTodo(id)
            onDone()
        }
    }
}
