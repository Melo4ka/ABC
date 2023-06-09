package ru.meldren.abc.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention
annotation class Syntax(val syntax: String)