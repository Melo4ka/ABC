package ru.meldren.abc.processor

interface ArgumentParser<out T : Any> {

    fun parse(arg: String): T
}