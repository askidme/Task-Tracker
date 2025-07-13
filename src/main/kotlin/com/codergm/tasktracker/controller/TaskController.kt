package com.codergm.tasktracker.controller

import com.codergm.tasktracker.model.dto.TaskDto
import com.codergm.tasktracker.model.dto.toEntity
import com.codergm.tasktracker.model.entity.Status
import com.codergm.tasktracker.service.TaskService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/tasks")
class TaskController(private val taskService: TaskService) {

    @PostMapping
    fun addTask(@RequestBody taskDto: TaskDto): ResponseEntity<TaskDto> =
        taskService.addTask(taskDto.toEntity()).run { ResponseEntity.status(HttpStatus.CREATED).body(this) }

    @GetMapping("/{id}")
    fun getTask(@PathVariable id: Long): TaskDto = taskService.getTask(id).fold(
        onSuccess = { it },
        onFailure = { throw it }
    )

    @GetMapping
    fun listTask(): List<TaskDto> = taskService.listTasks()

    @PutMapping("/{id}/{status}")
    fun completeTask(@PathVariable id: Long, @PathVariable status: Status): TaskDto =
        taskService.completeTask(id, status).fold(
            onSuccess = { it },
            onFailure = { throw it }
        )


    @DeleteMapping("/{id}")
    fun deleteTask(@PathVariable id: Long): ResponseEntity<Void> =
        taskService.deleteTask(id).fold(
            onSuccess = { ResponseEntity.noContent().build() },
            onFailure = { throw it }
        )
}