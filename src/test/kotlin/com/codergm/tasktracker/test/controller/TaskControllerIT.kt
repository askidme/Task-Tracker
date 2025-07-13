package com.codergm.tasktracker.test.controller

import com.codergm.tasktracker.config.TaskTestConfig
import com.codergm.tasktracker.exception.CustomControllerAdviser.ErrorResponse
import com.codergm.tasktracker.model.dto.TaskDto
import com.codergm.tasktracker.model.entity.Priority.*
import com.codergm.tasktracker.model.entity.Status.COMPLETED
import com.codergm.tasktracker.model.entity.Status.PENDING
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus.*
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TaskControllerIT : TaskTestConfig() {

    @Autowired
    lateinit var webClient: WebTestClient

    val uri = "/api/tasks"
    val taskId = 1001L
    val badTaskId: Long = 99999

    @Test
    fun `POST addTask should return 201 OK`() {
        val request = TaskDto(null, "New Task", "desc", null, HIGH, PENDING)

        webClient.post()
            .uri(uri)
            .bodyValue(request)
            .exchange()
            .expectStatus().isCreated
            .expectBody(TaskDto::class.java)
            .value { response ->
                assertThat(response.id).isNotNull()
                assertThat(response.title).isEqualTo("New Task")
            }
    }

    @Test
    fun `GET  getTask should return 200 OK with task`() {

        webClient.get()
            .uri("$uri/$taskId")
            .exchange()
            .expectStatus().isOk
            .expectBody(TaskDto::class.java)
            .value { response ->
                assertThat(response.id).isEqualTo(taskId)
                assertThat(response.status).isEqualTo(PENDING)
                assertThat(response.title).isEqualTo("Test Task 1")
                assertThat(response.description).isEqualTo("Description 1")
            }
    }

    @Test
    fun `GET get non-existent task should return 404`() {

        webClient.get()
            .uri("$uri/$badTaskId")
            .exchange()
            .expectStatus().isNotFound
            .expectBody(ErrorResponse::class.java)
            .value { response ->
                assertThat(response?.error).isEqualTo("TaskException")
                assertThat(response?.code).isEqualTo("TASK_NOT_FOUND")
                assertThat(response?.method).isEqualTo("GET")
                assertThat(response?.message).isEqualTo("task with id $badTaskId not found")
                assertThat(response?.url).isEqualTo("/api/tasks/$badTaskId")
                assertThat(response?.status).isEqualTo("NOT_FOUND")
            }
    }

    @Test
    fun `GET listTasks should return 200 OK with tasks`() {

        webClient.get()
            .uri(uri)
            .exchange()
            .expectStatus().isOk
            .expectBody(Array<TaskDto>::class.java)
            .value { response ->
                assertThat(response).isNotEmpty
                assertThat(response).hasSize(3)
                assertThat(response.first().title).isEqualTo("Test Task 1")
                assertThat(response[1].title).isEqualTo("Test Task 2")
            }
    }

    @Test
    fun `PUT completeTask should return 200 OK with updated status`() {

        webClient.put()
            .uri("$uri/$taskId/COMPLETED")
            .exchange()
            .expectStatus().isOk
            .expectBody(TaskDto::class.java)
            .value { response ->
                assertThat(response.status).isEqualTo(COMPLETED)
            }
    }

    @Test
    fun `PUT complete non-existent task should return 404`() {
        webClient.put()
            .uri("$uri/$badTaskId/COMPLETED")
            .exchange()
            .expectStatus().isNotFound
            .expectBody(ErrorResponse::class.java)
            .value { response ->
                assertThat(response.error).isEqualTo("TaskException")
                assertThat(response.code).isEqualTo("TASK_NOT_FOUND")
                assertThat(response.method).isEqualTo("PUT")
                assertThat(response.message).isEqualTo("task with id $badTaskId not found")
                assertThat(response.url).isEqualTo("/api/tasks/$badTaskId/COMPLETED")
                assertThat(response.status).isEqualTo("NOT_FOUND")
            }
    }

    @Test
    fun `PUT completeTask with incorrect status should return 500`() {

        webClient.put()
            .uri("$uri/$taskId/INCORRECT_TASK_STATUS")
            .exchange()
            .expectStatus().is5xxServerError
            .expectBody(ErrorResponse::class.java)
            .value { response ->
                assertThat(response.error).isEqualTo("MethodArgumentTypeMismatchException")
                assertThat(response.code).isEqualTo("SERVER_ERROR")
                assertThat(response.method).isEqualTo("PUT")
                assertThat(response.url).isEqualTo("/api/tasks/$taskId/INCORRECT_TASK_STATUS")
                assertThat(response.status).isEqualTo("INTERNAL_SERVER_ERROR")
            }
    }

    @Test
    fun `DELETE deleteTask should return 204 OK`() {

        webClient.delete()
            .uri("$uri/$taskId")
            .exchange()
            .expectStatus().isNoContent
    }

    @Test
    fun `DELETE non-existent task should return 404`() {

        webClient.delete()
            .uri("$uri/$badTaskId")
            .exchange()
            .expectStatus().isNotFound
            .expectBody(ErrorResponse::class.java)
            .value { response ->
                assertThat(response.error).isEqualTo("TaskException")
                assertThat(response.code).isEqualTo("TASK_NOT_FOUND")
                assertThat(response.method).isEqualTo("DELETE")
                assertThat(response.message).isEqualTo("task with id $badTaskId not found")
                assertThat(response.url).isEqualTo("/api/tasks/$badTaskId")
                assertThat(response.status).isEqualTo("NOT_FOUND")
            }
    }
}