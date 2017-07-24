package net.aquadc.validation.rule

import android.content.res.Resources
import android.widget.EditText
import net.aquadc.validation.Rule
import net.aquadc.validation.ValidationResult

class LengthRule(
        private val min: Int = 0,
        private val max: Int = Int.MAX_VALUE
) : Rule {

    init {
        if (max < min) throw IllegalArgumentException("range $min..$max would be empty")
        if (min < 0) throw IllegalArgumentException("min must be >= 0, $min given")
    }

    override fun invoke(input: EditText, res: Resources): ValidationResult {
        val len = input.length()
        return when {
            len < min -> ValidationError(len, min, max, "must be at least $min characters, $len supplied")
            len > max -> ValidationError(len, min, max, "must be at most $max characters, $len supplied")
            else -> ValidationResult.Success
        }
    }

    class ValidationError internal constructor(
            val actual: Int, val min: Int, val max: Int, message: CharSequence
    ) : ValidationResult.Error(message)

}

class LengthRuleWithMinMaxMessages(
        private val rule: LengthRule,
        private val tooFewCharsMessage: (min: Int, actual: Int, resources: Resources) -> CharSequence,
        private val tooManyCharsMessage: (max: Int, actual: Int, resources: Resources) -> CharSequence
) : Rule {

    override fun invoke(input: EditText, resources: Resources): ValidationResult {
        val result = rule(input, resources)
        return when (result) {
            is ValidationResult.Success -> ValidationResult.Success
            is LengthRule.ValidationError -> LengthRule.ValidationError(result.actual, result.min, result.max, when {
                result.actual < result.min -> tooFewCharsMessage(result.min, result.actual, resources)
                result.actual > result.max -> tooManyCharsMessage(result.max, result.actual, resources)
                else -> throw IllegalArgumentException("${result.actual} seems to be in ${result.min}..${result.max}, so there's no error")
            })
            else -> throw IllegalArgumentException("LengthRule should return either ValidationResult.Success or LengthRule.ValidationError, got $result")
        }
    }

}