package com.example.todolist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.todolist.data.local.TodoDatabase
import com.example.todolist.data.local.TodoJsonDataSource
import com.example.todolist.data.preferences.TodoPreferences
import com.example.todolist.data.repository.TodoRepositoryImpl
import com.example.todolist.domain.usecase.DeleteTodoUseCase
import com.example.todolist.domain.usecase.ImportTodosUseCase
import com.example.todolist.domain.usecase.ObserveCompletedHighlightUseCase
import com.example.todolist.domain.usecase.ObserveTodoUseCase
import com.example.todolist.domain.usecase.ObserveTodosUseCase
import com.example.todolist.domain.usecase.SaveTodoUseCase
import com.example.todolist.domain.usecase.SetCompletedHighlightUseCase
import com.example.todolist.domain.usecase.ToggleTodoUseCase
import com.example.todolist.navigation.NavGraph
import com.example.todolist.presentation.viewmodel.TodoUseCases
import com.example.todolist.presentation.viewmodel.TodoViewModel
import com.example.todolist.presentation.viewmodel.TodoViewModelFactory
import com.example.todolist.ui.theme.TodoListTheme

@Composable
fun TodoAppContent() {
    val context = androidx.compose.ui.platform.LocalContext.current.applicationContext

    val repository = remember {
        val database = TodoDatabase.getInstance(context)
        TodoRepositoryImpl(
            todoDao = database.todoDao(),
            jsonDataSource = TodoJsonDataSource(context),
            preferences = TodoPreferences(context)
        )
    }

    val viewModel: TodoViewModel = viewModel(
        factory = TodoViewModelFactory(
            TodoUseCases(
                observeTodos = ObserveTodosUseCase(repository),
                observeTodo = ObserveTodoUseCase(repository),
                importTodos = ImportTodosUseCase(repository),
                saveTodo = SaveTodoUseCase(repository),
                deleteTodo = DeleteTodoUseCase(repository),
                toggleTodo = ToggleTodoUseCase(repository),
                observeCompletedHighlight = ObserveCompletedHighlightUseCase(repository),
                setCompletedHighlight = SetCompletedHighlightUseCase(repository)
            )
        )
    )

    TodoListTheme {
        val navController = rememberNavController()
        NavGraph(
            navController = navController,
            viewModel = viewModel
        )
    }
}
