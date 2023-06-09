package ru.meldren.abc.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention
annotation class Priority(val value: UInt)