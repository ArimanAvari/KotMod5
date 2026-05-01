package com.example.todolist.domain.repository

import com.example.todolist.domain.model.TodoItem
import kotlinx.coroutines.flow.Flow

interface TodoRepository {
    fun observeTodos(): Flow<List<TodoItem>>
    fun observeTodo(id: Int): Flow<TodoItem?>
    fun observeCompletedHighlight(): Flow<Boolean>
    suspend fun importTodosIfNeeded()
    suspend fun saveTodo(todo: TodoItem)
    suspend fun deleteTodo(id: Int)
    suspend fun toggleTodo(id: Int)
    suspend fun setCompletedHighlight(enabled: Boolean)
}
