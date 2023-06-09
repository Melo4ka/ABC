package ru.meldren.abc.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention
annotation class Subcommand(val name: String, vararg val aliases: String)