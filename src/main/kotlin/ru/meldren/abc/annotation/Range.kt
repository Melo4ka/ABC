package ru.meldren.abc.annotation

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention
annotation class Range(
    val min: Double = Double.MIN_VALUE,
    val max: Double = Double.MAX_VALUE,
    val inclusiveMin: Boolean = true,
    val inclusiveMax: Boolean = true
)

fun Range.asString(
    delimiter: String = ", ",
    numberFormat: (Double) -> String = { value -> value.toString().removeSuffix(".0") }
): String {
    val minSymbol = if (inclusiveMin) '[' else '('
    val maxSymbol = if (inclusiveMax) ']' else ')'
    return "$minSymbol${numberFormat(min)}$delimiter${numberFormat(max)}$maxSymbol"
}