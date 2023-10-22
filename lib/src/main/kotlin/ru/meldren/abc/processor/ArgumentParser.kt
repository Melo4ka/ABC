package ru.meldren.abc.processor

import ru.meldren.abc.exception.processing.ArgumentParseException
import kotlin.jvm.Throws

interface ArgumentParser<out T : Any> {

    @Throws(ArgumentParseException::class)
    fun parse(arg: String): T
}