package com.codergm.tasktracker.service.impl

import com.codergm.tasktracker.exception.TaskException
import com.codergm.tasktracker.model.dto.TaskDto
import com.codergm.tasktracker.model.entity.Status
import com.codergm.tasktracker.model.entity.Task
import com.codergm.tasktracker.model.entity.toDto
import com.codergm.tasktracker.repository.TaskRepository
import com.codergm.tasktracker.service.TaskService
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
class TaskServiceImpl(private val taskRepository: TaskRepository) : TaskService {

    @CacheEvict(value = ["task", "tasks"], allEntries = true)
    override fun addTask(task: Task): TaskDto = taskRepository.save(task).toDto()

    @Cacheable(value = ["task"], key = "#id")
    override fun getTask(id: Long): Result<TaskDto> {
        return taskRepository.findById(id).getOrNull()?.run { Result.success(this.toDto()) }
            ?: Result.failure(TaskException.notFound("task with id $id not found"))
    }

    @Cacheable("tasks")
    override fun listTasks(): List<TaskDto> = taskRepository.findAll().map { it.toDto() }

    @CacheEvict(value = ["task", "tasks"], allEntries = true)
    override fun completeTask(id: Long, status: Status): Result<TaskDto> =
        taskRepository.findById(id).getOrNull()
            ?.apply { this.status = status }
            ?.run {
                Result.success(taskRepository.save(this).toDto())
            } ?: Result.failure(TaskException.notFound("task with id $id not found"))

    @CacheEvict(value = ["task", "tasks"], allEntries = true)
    override fun deleteTask(id: Long): Result<Unit> =
        taskRepository.findById(id).getOrNull()
            ?.run {
                taskRepository.deleteById(id)
                Result.success(Unit)
            } ?: Result.failure(TaskException.notFound("task with id $id not found"))

}