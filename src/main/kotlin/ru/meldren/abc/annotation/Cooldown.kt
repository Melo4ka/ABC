package ru.meldren.abc.annotation

import java.util.concurrent.TimeUnit

@Target(AnnotationTarget.CLASS)
@Retention
annotation class Cooldown(
    val value: ULong,
    val unit: TimeUnit = TimeUnit.SECONDS
)
