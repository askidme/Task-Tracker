package com.codergm.tasktracker.test.service

import com.codergm.tasktracker.config.TaskTestConfig
import com.codergm.tasktracker.exception.TaskException
import com.codergm.tasktracker.model.entity.Priority
import com.codergm.tasktracker.model.entity.Status
import com.codergm.tasktracker.model.entity.Task
import com.codergm.tasktracker.service.TaskService
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import java.time.LocalDate


@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class TaskServiceImplTests : TaskTestConfig() {

    @Autowired
    private lateinit var taskService: TaskService

    val taskId = 1001L
    val badTaskId: Long = 99999

    @Test
    fun `test addTask`() {
        // Arrange
        val taskTitle = "Test Task"

        val task = Task(
            title = taskTitle,
            description = "Test Description",
            dueDate = LocalDate.now(),
            priority = Priority.HIGH,
            status = Status.PENDING
        )

        // Act
        val saved = taskService.addTask(task)

        // Assert
        val retrieved = taskService.listTasks().filter { it.title == taskTitle }.first()

        assertThat(retrieved.title).isEqualTo(saved.title)

    }

    @Test
    fun `test listTasks`() {

        //Act
        val all = taskService.listTasks()

        //Assert
        assertThat(all).hasSize(3)
        assertThat(all).extracting("title").contains("Test Task 1", "Test Task 2", "Test Task 3")
    }

    @Test
    fun `test getTask success`() {

        //Act
        val result = taskService.getTask(taskId)

        //Assert
        assertThat(result.isSuccess).isTrue()
        val task = result.getOrNull()
        assertThat(task?.id).isEqualTo(taskId)
    }

    @Test
    fun `test getTask failure`() {

        //Act
        val result = taskService.getTask(badTaskId)

        //Assert
        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull()
        assertThat(exception).isInstanceOf(TaskException::class.java)
    }

    @Test
    fun `test completeTask success`() {

        //Act
        val updated = taskService.completeTask(taskId, Status.COMPLETED)

        //Assert
        assertThat(updated.isSuccess).isTrue
        assertThat(updated.getOrNull()?.status).isEqualTo(Status.COMPLETED)
    }

    @Test
    fun `test completeTask failure`() {

        //Act
        val result = taskService.completeTask(badTaskId, Status.COMPLETED)

        //Assert
        assertThat(result.isFailure).isTrue
        assertThat(result.exceptionOrNull()).isInstanceOf(TaskException::class.java)
    }

    @Test
    fun `test deleteTask success`() {

        //Act
        val result = taskService.deleteTask(taskId)

        //Assert
        assertThat(result.isSuccess).isTrue
    }

    @Test
    fun `test deleteTask failure`() {

        //Act
        val result = taskService.deleteTask(badTaskId)

        //Assert
        assertThat(result.isFailure).isTrue
        assertThat(result.exceptionOrNull()).isInstanceOf(TaskException::class.java)
    }
}