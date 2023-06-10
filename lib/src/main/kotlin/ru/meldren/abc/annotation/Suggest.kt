package ru.meldren.abc.annotation

import ru.meldren.abc.processor.SuggestionProvider
import kotlin.reflect.KClass

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention
annotation class Suggest(val providerClass: KClass<out SuggestionProvider<*>>)