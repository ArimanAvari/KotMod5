package com.example.todolist.data.repository

import com.example.todolist.data.local.TodoDao
import com.example.todolist.data.local.TodoJsonDataSource
import com.example.todolist.data.model.toDomain
import com.example.todolist.data.model.toEntity
import com.example.todolist.data.preferences.TodoPreferences
import com.example.todolist.domain.model.TodoItem
import com.example.todolist.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TodoRepositoryImpl(
    private val todoDao: TodoDao,
    private val jsonDataSource: TodoJsonDataSource,
    private val preferences: TodoPreferences
) : TodoRepository {

    override fun observeTodos(): Flow<List<TodoItem>> {
        return todoDao.observeTodos().map { todos ->
            todos.map { it.toDomain() }
        }
    }

    override fun observeTodo(id: Int): Flow<TodoItem?> {
        return todoDao.observeTodoById(id).map { todo ->
            todo?.toDomain()
        }
    }

    override fun observeCompletedHighlight(): Flow<Boolean> {
        return preferences.highlightCompletedFlow
    }

    override suspend fun importTodosIfNeeded() {
        if (preferences.isImportDone()) return

        if (todoDao.getTodoCount() == 0) {
            val todos = jsonDataSource.readTodos().map { it.toEntity() }
            todoDao.insertTodos(todos)
        }

        preferences.markImportDone()
    }

    override suspend fun saveTodo(todo: TodoItem) {
        val normalizedTodo = todo.copy(
            title = todo.title.trim(),
            description = todo.description.trim()
        )

        if (normalizedTodo.id == 0) {
            todoDao.insertTodo(normalizedTodo.toEntity())
        } else {
            todoDao.updateTodo(normalizedTodo.toEntity())
        }
    }

    override suspend fun deleteTodo(id: Int) {
        todoDao.deleteTodoById(id)
    }

    override suspend fun toggleTodo(id: Int) {
        val currentTodo = todoDao.getTodoById(id) ?: return
        todoDao.updateTodoCompletion(id, !currentTodo.isCompleted)
    }

    override suspend fun setCompletedHighlight(enabled: Boolean) {
        preferences.setHighlightCompleted(enabled)
    }
}
