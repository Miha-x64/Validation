package net.aquadc.validation.rule

import android.content.res.Resources
import android.widget.EditText
import net.aquadc.validation.Rule
import net.aquadc.validation.ValidationResult

class LengthRule(
        private val min: Int = 0,
        private val max: Int = Int.MAX_VALUE,
        private val countLength: (CharSequence) -> Int = CharacterCounter
) : Rule {

    init {
        if (max < min) throw IllegalArgumentException("range $min..$max would be empty")
        if (min < 0) throw IllegalArgumentException("min must be >= 0, $min given")
    }

    override fun invoke(input: EditText, res: Resources): ValidationResult<ValidationError> {
        val len = countLength(input.text)
        return when {
            len < min -> ValidationResult.Error("must be at least $min characters, $len supplied", ValidationError(len, min, max))
            len > max -> ValidationResult.Error("must be at most $max characters, $len supplied", ValidationError(len, min, max))
            else -> ValidationResult.Success
        }
    }

    class ValidationError internal constructor(
            val actual: Int, val min: Int, val max: Int
    )

    companion object {
        val CharacterCounter = CharSequence::length
        val WordCounter = object : (CharSequence) -> Int {
            private val pattern = Regex("[\\p{L}\'\\-]+")
            override fun invoke(p1: CharSequence): Int = pattern.findAll(p1).count()
        }
    }

}

class LengthRuleWithMinMaxMessages(
        private val rule: LengthRule,
        private val tooFewCharsMessage: (min: Int, actual: Int, resources: Resources) -> CharSequence,
        private val tooManyCharsMessage: (max: Int, actual: Int, resources: Resources) -> CharSequence
) : Rule {

    override fun invoke(input: EditText, resources: Resources): ValidationResult<*> {
        val result = rule(input, resources)
        return when (result) {
            is ValidationResult.Success -> ValidationResult.Success
            is ValidationResult.Error -> ValidationResult.Error(when {
                result.payload.actual < result.payload.min -> tooFewCharsMessage(result.payload.min, result.payload.actual, resources)
                result.payload.actual > result.payload.max -> tooManyCharsMessage(result.payload.max, result.payload.actual, resources)
                else -> throw IllegalArgumentException("${result.payload.actual} seems to be in ${result.payload.min}..${result.payload.max}, so there's no error")
            })
        }
    }

}