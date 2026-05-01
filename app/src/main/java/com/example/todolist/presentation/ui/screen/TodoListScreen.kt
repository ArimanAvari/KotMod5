package com.example.todolist.presentation.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.todolist.domain.model.TodoItem
import com.example.todolist.presentation.ui.component.TodoCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(
    todos: List<TodoItem>,
    highlightCompleted: Boolean,
    onHighlightChange: (Boolean) -> Unit,
    onAddTodoClick: () -> Unit,
    onTodoClick: (Int) -> Unit,
    onToggleTodo: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "TodoList") },
                actions = {
                    Column(
                        modifier = Modifier.padding(end = 12.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Цвет завершенных",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Switch(
                            checked = highlightCompleted,
                            onCheckedChange = onHighlightChange
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTodoClick) {
                Text(text = "+")
            }
        }
    ) { innerPadding ->
        if (todos.isEmpty()) {
            EmptyTodoState(
                modifier = Modifier.padding(innerPadding),
                onAddTodoClick = onAddTodoClick
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = todos,
                    key = { todo -> todo.id }
                ) { todo ->
                    TodoCard(
                        todo = todo,
                        highlightCompleted = highlightCompleted,
                        onTodoClick = { onTodoClick(todo.id) },
                        onCheckedChange = { onToggleTodo(todo.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyTodoState(
    modifier: Modifier = Modifier,
    onAddTodoClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Список задач пока пуст",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Добавьте первую задачу и сохраните её в Room",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onAddTodoClick) {
                Text(text = "Добавить задачу")
            }
        }
    }
}
