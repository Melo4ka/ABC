package ru.meldren.abc.processor.validator

import ru.meldren.abc.annotation.Range
import ru.meldren.abc.exception.processing.ArgumentValidationException
import ru.meldren.abc.util.checkOrThrow

class NumberRangeValidator(val failMessage: (Range) -> String) : ArgumentValidator<Number, Range> {

    override fun validate(arg: Number, annotation: Range) {
        val doubleValue = arg.toDouble()
        checkOrThrow(
            doubleValue > annotation.min && doubleValue < annotation.max ||
                    annotation.inclusiveMin && doubleValue == annotation.min ||
                    annotation.inclusiveMax && doubleValue == annotation.max
        ) { ArgumentValidationException(failMessage(annotation)) }
    }
}