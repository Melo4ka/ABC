package ru.meldren.abc.exception

open class CommandException(override val message: String? = null) : RuntimeException(message)