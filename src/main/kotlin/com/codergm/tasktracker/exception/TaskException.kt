package com.codergm.tasktracker.exception

import org.springframework.http.HttpStatus

enum class TaskErrorType { TASK_NOT_FOUND }

class TaskException(val code: TaskErrorType, override val message: String, val status: HttpStatus) : RuntimeException(message){
    companion object {
        fun notFound(message: String): TaskException =
            TaskException(TaskErrorType.TASK_NOT_FOUND, message, HttpStatus.NOT_FOUND)
    }
}


