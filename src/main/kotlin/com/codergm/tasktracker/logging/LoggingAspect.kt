package com.codergm.tasktracker.logging

import mu.KotlinLogging
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

private val log = KotlinLogging.logger {}

@Aspect
@Component
@Order(1)
class LoggingAspect {

    @Pointcut("execution( public * com.codergm.tasktracker.service..*(..))")
    fun serviceMethods() {
    }

    @Pointcut("execution( public * com.codergm.tasktracker.controller..*(..))")
    fun controllerMethods() {
    }

    @Around("serviceMethods()")
    fun logService(pjp: ProceedingJoinPoint): Any? {
        val method = (pjp.signature as MethodSignature).method
        val className = method.declaringClass.simpleName
        val methodName = method.name
        val args = pjp.args.joinToString(", ") { it.toString() }

        log.debug { "➡\uFE0F  $className.$methodName($args)" }

        val start = Instant.now()

        return runCatching {
            val result = pjp.proceed()
            val ms = Duration.between(start, Instant.now()).toMillis()
            log.info { "✅ $className.$methodName($args) took ${ms}ms -> $result" }
            result
        }.onFailure { ex ->
            val ms = Duration.between(start, Instant.now()).toMillis()
            log.warn(ex) { "❌ $className.$methodName($args) failed after ${ms}ms" }
            throw ex
        }.getOrThrow()

    }

    @Around("controllerMethods()")
    fun logController(pjp: ProceedingJoinPoint): Any? {
        val method = (pjp.signature as MethodSignature).method
        val className = method.declaringClass.simpleName
        val methodName = method.name
        val args = pjp.args.joinToString(", ") { it.toString() }
        log.info { "➡\uFE0F  $className.$methodName($args)" }
        val result = pjp.proceed()
        log.info { "result: ${result}" }
        return result

    }
}