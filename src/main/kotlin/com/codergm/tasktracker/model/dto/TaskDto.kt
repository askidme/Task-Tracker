package com.codergm.tasktracker.model.dto

import com.codergm.tasktracker.model.entity.Priority
import com.codergm.tasktracker.model.entity.Status
import com.codergm.tasktracker.model.entity.Task
import java.time.LocalDate

data class TaskDto(
    val id: Long?,
    val title: String,
    val description: String,
    val dueDate: LocalDate? = null,
    val priority: Priority,
    var status: Status
)

fun TaskDto.toEntity(): Task =
    Task(id = id, title = title, description = description, dueDate = dueDate, priority = priority, status = status)