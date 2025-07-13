package com.codergm.tasktracker.service

import com.codergm.tasktracker.model.dto.TaskDto
import com.codergm.tasktracker.model.entity.Status
import com.codergm.tasktracker.model.entity.Task

interface TaskService {

    fun addTask(task: Task): TaskDto

    fun getTask(id: Long): Result<TaskDto>

    fun listTasks(): List<TaskDto>

    fun completeTask(id: Long, status: Status): Result<TaskDto>

    fun deleteTask(id: Long): Result<Unit>
}