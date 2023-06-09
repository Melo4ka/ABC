package ru.meldren.abc.annotation

@Target(AnnotationTarget.CLASS)
@Retention
annotation class Command(val name: String, vararg val aliases: String)
