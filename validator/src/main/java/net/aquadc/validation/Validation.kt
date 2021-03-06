package net.aquadc.validation

import android.content.res.Resources
import android.support.annotation.StringRes
import android.support.v4.util.ArrayMap
import android.util.Patterns
import android.widget.EditText
import android.widget.TextView
import java.util.*

/**
 * Created by miha on 26.05.16
 */
class Validation(
        private val presenter: Presenter = DefaultPresenter(),
        private val transform: (CharSequence) -> CharSequence = { it }
) {

    private val fieldsAndRules = LinkedHashMap<EditText, Node>()

    fun add(field: EditText) {
        var node: Node? = fieldsAndRules[field]
        if (node == null) {
            node = Node(field)
            fieldsAndRules.put(field, node)
        }
    }

    fun add(field: EditText, rule: Rule, @StringRes errorMessage: Int) {
        add(field, RuleWithOwnText(rule, errorMessage))
    }

    fun add(field: EditText, rule: Rule, errorMessage: String) {
        add(field, RuleWithOwnText(rule, errorMessage))
    }

    fun add(field: EditText, vararg rules: Rule) {
        var node: Node? = fieldsAndRules[field]
        if (node != null) {
            node.addRules(rules)
        } else {
            node = Node(field, *rules)
            fieldsAndRules.put(field, node)
        }
    }

    /**
     * Validates all fields. Sets errors if necessary
     * @return mapping of EditText to values, or null, if invalid
     */
    fun validateAndGet(): Map<EditText, String>? {
        presenter.beforeValidation()

        val values = ArrayMap<EditText, String>(fieldsAndRules.size)
        var error = false
        fieldsAndRules.forEach { (key, value) ->
            if (validate(value) && !error)
                values.put(key, value.field.text.toString())
            else
                error = true
        }
        return if (error) null else values
    }

    /**
     * Validates all fields. Sets errors if necessary
     * @return `true` if everything is valid, `false` otherwise
     */
    fun validate(): Boolean {
        presenter.beforeValidation()

        // not using 'all' and invoking 'validate' first
        // because need to check each field and reflect results on presentation
        return fieldsAndRules.values.fold(true) { acc, it -> validate(it) && acc }
    }

    /**
     * Validates specified field. Sets error if necessary
     * @return `true` if field value is valid, `false` otherwise
     */
    fun validate(et: EditText): Boolean {
        val node = fieldsAndRules[et]
                ?: throw IllegalArgumentException("specified view $et was not registered for validation, got only $fieldsAndRules")

        return validate(node)
    }

    /**
     * Validates all fields without any visual side-effects
     * @return `true` if everything is valid, `false` otherwise
     */
    fun isValid(): Boolean {
        val error = fieldsAndRules.values.any { getError(it) != null }
        return !error
    }

    private fun validate(node: Node): Boolean {
        val error = getError(node)

        return if (error == null) {
            presenter.setValid(node.field)
            true
        } else {
            presenter.setError(node.field, transform(error.message))
            false
        }
    }

    private fun getError(node: Node): ValidationResult.Error<*>? {
        var error: ValidationResult.Error<*>? = null
        val et = node.field
        for (rule in node.rules) {
            if (rule.toString() !== Preset.REQUIRED.toString() && et.text.toString().isEmpty()) {
                continue
            }

            val result = rule(node.field, et.resources)
            when (result) {
                is ValidationResult.Success -> { }
                is ValidationResult.Error -> error = result
            }.also { }
        }

        return error
    }

    /**
     * Validators
     */
    class RuleWithOwnText : Rule {

        private val validationRule: Rule
        @StringRes private val errorMessageRes: Int
        private var errorMessage: String? = null /* lazy */

        constructor(validationRule: Rule, @StringRes errorMessage: Int) {
            this.validationRule = validationRule
            this.errorMessageRes = errorMessage
        }

        constructor(validationRule: Rule, errorMessage: String) {
            this.validationRule = validationRule
            this.errorMessageRes = 0
            this.errorMessage = errorMessage
        }

        override fun invoke(input: EditText, res: Resources): ValidationResult<*> {
            val result = validationRule(input, res)
            return when (result) {
                is ValidationResult.Success -> ValidationResult.Success
                is ValidationResult.Error -> ValidationResult.Error(errorMessage ?: res.getString(errorMessageRes).also { errorMessage = it })
            }
        }

        override fun toString(): String {
            return validationRule.toString()
        }
    }

    enum class Preset : Rule {
        REQUIRED, EMAIL;

        override fun invoke(input: EditText, res: Resources): ValidationResult<*> {
            val text = input.text.toString()
            return when (this) {
                REQUIRED -> if (text.isEmpty()) ValidationResult.Error("validation failed for $name") else ValidationResult.Success
                EMAIL -> if (Patterns.EMAIL_ADDRESS.matcher(text).matches()) ValidationResult.Success else ValidationResult.Error("validation failed for $name")
            }
        }
    }

    inner class EqualTo(private val sample: TextView) : Rule {

        override fun invoke(input: EditText, res: Resources): ValidationResult<*> =
                if (input.text.toString() == sample.text.toString()) ValidationResult.Success
                else ValidationResult.Error("values must be equal")
    }

    private class Node(val field: EditText, vararg rules: Rule) {
        val rules: MutableSet<Rule>

        init {
            this.rules = LinkedHashSet<Rule>()
            this.rules.addAll(Arrays.asList(*rules))
        }

        fun addRule(rule: Rule) {
            this.rules.add(rule)
        }

        fun addRules(rules: Collection<Rule>) {
            this.rules.addAll(rules)
        }

        fun addRules(rules: Array<out Rule>) {
            this.rules.addAll(rules.asList())
        }
    }

    interface Presenter {
        fun setValid(et: EditText)
        fun setError(et: EditText, message: CharSequence)
        fun beforeValidation()
    }
}
