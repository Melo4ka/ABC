package ru.meldren.abc.annotation

import ru.meldren.abc.processor.ArgumentParser
import kotlin.reflect.KClass

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention
annotation class Parser(val parserClass: KClass<out ArgumentParser<*>>)
