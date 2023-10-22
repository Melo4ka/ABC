package ru.meldren.abc.processor.validator

import ru.meldren.abc.annotation.Range
import ru.meldren.abc.exception.invocation.ArgumentValidationException
import ru.meldren.abc.processor.ArgumentValidator
import ru.meldren.abc.util.checkOrThrow
import java.util.function.Function

class NumberRangeValidator<S : Any>(
    private val failMessage: Function<Range, String>
) : ArgumentValidator<S, Number, Range> {

    override fun validate(sender: S, arg: Number, annotation: Range) {
        val doubleValue = arg.toDouble()
        checkOrThrow(
            doubleValue > annotation.min && doubleValue < annotation.max ||
                    annotation.inclusiveMin && doubleValue == annotation.min ||
                    annotation.inclusiveMax && doubleValue == annotation.max
        ) { ArgumentValidationException(failMessage.apply(annotation)) }
    }
}