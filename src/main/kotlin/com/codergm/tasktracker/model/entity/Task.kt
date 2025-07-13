package com.codergm.tasktracker.model.entity

import com.codergm.tasktracker.model.dto.TaskDto
import jakarta.persistence.*
import java.time.LocalDate

enum class Priority { LOW, MEDIUM, HIGH }
enum class Status { PENDING, COMPLETED, REJECTED }

@Entity
@Table(name = "tasks")
data class Task(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "task_id_seq")
    @SequenceGenerator(name = "task_id_seq", sequenceName = "task_id_seq", allocationSize = 1)
    val id: Long? = null,
    val title: String,
    val description: String,
    val dueDate: LocalDate? = null,
    @Enumerated(EnumType.STRING)
    val priority: Priority = Priority.MEDIUM,
    @Enumerated(EnumType.STRING)
    var status: Status = Status.PENDING
)

fun Task.toDto(): TaskDto =
    TaskDto(id = id, title = title, description = description, dueDate = dueDate, priority = priority, status = status)

