package com.codergm.tasktracker.test.cache

import com.codergm.tasktracker.model.entity.Priority.LOW
import com.codergm.tasktracker.model.entity.Status
import com.codergm.tasktracker.model.entity.Status.COMPLETED
import com.codergm.tasktracker.model.entity.Status.PENDING
import com.codergm.tasktracker.model.entity.Task
import com.codergm.tasktracker.repository.TaskRepository
import com.codergm.tasktracker.service.TaskService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.CacheManager
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.util.*

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TaskServiceCachingIT {
    @MockitoBean
    lateinit var taskRepository: TaskRepository

    @Autowired
    lateinit var taskService: TaskService

    @Autowired
    lateinit var cacheManager: CacheManager

    /** ----------  Parameterised “read-through” cache ---------- */
    @ParameterizedTest
    @CsvSource("1,Buy Milk", "2,Write Code")
    fun `getTask caches the first lookup`(id: Long, title: String) {
        // Arrange
        val entity = Task(id, title, "desc", null, LOW, PENDING)
        whenever(taskRepository.findById(id)).thenReturn(Optional.of(entity))

        // Act
        val first  = taskService.getTask(id).getOrThrow()   // repo call #1
        val second = taskService.getTask(id).getOrThrow()   // should be cached

        // Assert
        verify(taskRepository, times(1)).findById(id)
        assertThat(first).isEqualTo(second)
    }

    /** ----------  Parameterised eviction via completeTask ---------- */
    @ParameterizedTest
    @CsvSource(
        "PENDING,COMPLETED",
        "PENDING,REJECTED"
    )
    fun `completeTask evicts single and list caches`(fromStr: String, toStr: String) {
        val from = Status.valueOf(fromStr)
        val to   = Status.valueOf(toStr)
        val id   = 99L
        val entity = Task(id, "Cached!", "desc", null, LOW, PENDING)
        val saved  = entity.copy(status = to)

        whenever(taskRepository.findById(id)).thenReturn(Optional.of(entity))
        whenever(taskRepository.save(any<Task>())).thenReturn(saved)
        whenever(taskRepository.findAll()).thenReturn(listOf(entity), listOf(saved))

        // warm task + tasks caches
        taskService.getTask(id)
        taskService.listTasks()

        // act – mutate
        taskService.completeTask(id, to)

        // caches should be cold now
        taskService.getTask(id)
        taskService.listTasks()

        // we expect repository look-ups twice each
        verify(taskRepository, times(3)).findById(id)
        verify(taskRepository, times(2)).findAll()
    }

    @Test
    fun `getTask should be cached into 'task' cache after first call`() {

        //Arrange

        val taskId = getTestTask().id!!
        whenever(taskRepository.findById(taskId)).thenReturn(Optional.of(getTestTask()))

        //Act
        // First call → hits repository
        val result1 = taskService.getTask(taskId)
        // Second call → should return cached result
        val result2 = taskService.getTask(taskId)

        //Assert
        // Should only hit repository once
        verify(taskRepository, times(1)).findById(taskId)
        assertThat(result1).isEqualTo(result2)
    }

    @Test
    fun `completeTask should evict 'task' cache`() {
        //Arrange
        val taskId = getTestTask().id!!
        val savedTask = getTestTask().copy(status = COMPLETED)

        //Act
        whenever(taskRepository.findById(taskId)).thenReturn(Optional.of(getTestTask()))
        whenever(taskRepository.save(any<Task>())).thenReturn(savedTask)

        taskService.getTask(taskId)
        taskService.getTask(taskId)
        taskService.completeTask(taskId, COMPLETED)
        taskService.getTask(taskId)
        taskService.getTask(taskId)

        //Assert
        verify(taskRepository, times(3)).findById(taskId)
        val cache = cacheManager.getCache("tasks")
        assertThat(cache?.get(taskId)).isNull()
    }

    @Test
    fun `listTask should be cached into 'tasks' cache after first call`() {
        //Arrange
        whenever(taskRepository.findAll()).thenReturn(listOf(getTestTask()))

        //Act
        // First call → hits repository
        val firstResult = taskService.listTasks()
        // Second call → should return cached result
        val secondResult = taskService.listTasks()

        //Assert
        verify(taskRepository, times(1)).findAll()
        assertThat(firstResult.first()).isEqualTo(secondResult.first())
    }

    @Test
    fun `completeTask should evict 'tasks' cache`() {
        //Arrange
        val task = getTestTask()
        val taskId = task.id!!
        val savedTask = task.copy(status = COMPLETED)

        //Act
        whenever(taskRepository.findAll()).thenReturn(listOf(task))
        whenever(taskRepository.save(any<Task>())).thenReturn(savedTask)

        taskService.listTasks()
        taskService.listTasks()
        taskService.completeTask(taskId, COMPLETED)
        taskService.listTasks()
        taskService.listTasks()

        //Assert
        verify(taskRepository, times(2)).findAll()
        val cache = cacheManager.getCache("tasks")
        assertThat(cache?.get(taskId)).isNull()
    }

    private fun getTestTask() = Task(1, "Cached Task", "desc", null, LOW, PENDING)
}