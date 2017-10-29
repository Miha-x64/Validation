package net.aquadc.validation

import android.content.res.Resources
import android.widget.EditText

typealias Rule = (input: EditText, res: Resources) -> ValidationResult<*>

sealed class ValidationResult<out E> {
    object Success : ValidationResult<Nothing>()

    class Error<out E>(
            val message: CharSequence,
            val payload: E
    ) : ValidationResult<E>()

    companion object {
        fun Error(message: CharSequence) = ValidationResult.Error(message, Unit)
    }

}
