package com.example.todolist.presentation.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.todolist.domain.model.TodoItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoEditorScreen(
    todo: TodoItem?,
    onBackClick: () -> Unit,
    onSaveClick: (title: String, description: String, isCompleted: Boolean) -> Unit,
    onDeleteClick: (() -> Unit)?
) {
    var title by rememberSaveable(todo?.id) { mutableStateOf(todo?.title.orEmpty()) }
    var description by rememberSaveable(todo?.id) { mutableStateOf(todo?.description.orEmpty()) }
    var isCompleted by rememberSaveable(todo?.id) { mutableStateOf(todo?.isCompleted ?: false) }

    LaunchedEffect(todo?.id) {
        title = todo?.title.orEmpty()
        description = todo?.description.orEmpty()
        isCompleted = todo?.isCompleted ?: false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (todo == null) "Новая задача" else "Редактирование"
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (todo == null) {
                    "Создайте новую задачу"
                } else {
                    "Измените поля и сохраните обновления"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Название") },
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Описание") },
                minLines = 5
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Checkbox(
                    checked = isCompleted,
                    onCheckedChange = { isCompleted = it }
                )
                Text(
                    text = "Задача выполнена",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }

            Button(
                onClick = { onSaveClick(title, description, isCompleted) },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank()
            ) {
                Text(text = "Сохранить")
            }

            onDeleteClick?.let { deleteAction ->
                TextButton(
                    onClick = deleteAction,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Удалить задачу")
                }
            }

            TextButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(text = "Назад")
            }
        }
    }
}
