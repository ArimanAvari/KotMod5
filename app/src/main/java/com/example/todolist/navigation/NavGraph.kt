package com.example.todolist.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.todolist.presentation.ui.screen.TodoEditorScreen
import com.example.todolist.presentation.ui.screen.TodoListScreen
import com.example.todolist.presentation.viewmodel.TodoViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: TodoViewModel
) {
    val todos by viewModel.todos.collectAsState()
    val highlightCompleted by viewModel.highlightCompleted.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.TodoList.route
    ) {
        composable(Screen.TodoList.route) {
            TodoListScreen(
                todos = todos,
                highlightCompleted = highlightCompleted,
                onHighlightChange = viewModel::setHighlightCompleted,
                onAddTodoClick = { navController.navigate(Screen.TodoCreate.route) },
                onTodoClick = { todoId ->
                    navController.navigate(Screen.TodoEdit.createRoute(todoId))
                },
                onToggleTodo = viewModel::toggleTodo
            )
        }

        composable(Screen.TodoCreate.route) {
            TodoEditorScreen(
                todo = null,
                onBackClick = { navController.popBackStack() },
                onSaveClick = { title, description, isCompleted ->
                    viewModel.saveTodo(
                        id = 0,
                        title = title,
                        description = description,
                        isCompleted = isCompleted,
                        onDone = { navController.popBackStack() }
                    )
                },
                onDeleteClick = null
            )
        }

        composable(
            route = Screen.TodoEdit.route,
            arguments = listOf(navArgument("todoId") { type = NavType.IntType })
        ) { backStackEntry ->
            val todoId = backStackEntry.arguments?.getInt("todoId") ?: 0
            val todo by viewModel.observeTodo(todoId).collectAsState(initial = null)

            TodoEditorScreen(
                todo = todo,
                onBackClick = { navController.popBackStack() },
                onSaveClick = { title, description, isCompleted ->
                    viewModel.saveTodo(
                        id = todoId,
                        title = title,
                        description = description,
                        isCompleted = isCompleted,
                        onDone = { navController.popBackStack() }
                    )
                },
                onDeleteClick = {
                    viewModel.deleteTodo(todoId) {
                        navController.popBackStack()
                    }
                }
            )
        }
    }
}

sealed class Screen(val route: String) {
    data object TodoList : Screen("todo_list")
    data object TodoCreate : Screen("todo_create")
    data object TodoEdit : Screen("todo_edit/{todoId}") {
        fun createRoute(todoId: Int): String = "todo_edit/$todoId"
    }
}
