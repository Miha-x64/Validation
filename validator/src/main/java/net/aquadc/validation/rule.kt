package net.aquadc.validation

import android.content.res.Resources
import android.widget.EditText

typealias Rule = (input: EditText, res: Resources) -> ValidationResult

sealed class ValidationResult {
    object Success : ValidationResult()

    open class Error(
            open val message: CharSequence
    ) : ValidationResult() // todo: parameterize it, allow Payload
}
