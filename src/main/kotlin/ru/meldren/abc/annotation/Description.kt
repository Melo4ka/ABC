package ru.meldren.abc.annotation

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention
annotation class Description(val description: String)
