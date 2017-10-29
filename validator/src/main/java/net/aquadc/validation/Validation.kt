package net.aquadc.validation

import android.content.res.Resources
import android.support.annotation.StringRes
import android.util.Patterns
import android.widget.EditText
import android.widget.TextView
import java.util.*

/**
 * Created by miha on 26.05.16
 */
open class Validation {

    /**
     * Invariant
     */
    private val fieldsAndRules = LinkedHashMap<EditText, Node>()
    internal val presenter: Presenter

    /**
     * Public constructors
     */
    constructor() {
        this.presenter = DefaultPresenter()
    }

    constructor(presenter: Presenter) {
        this.presenter = presenter
    }

    /**
     * Variant
     */
    private var transform: (CharSequence) -> CharSequence = { it }

    /**
     * Public API
     */
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

    // todo: fun validate(): Unit? { which is more lightweight }

    fun validateAndGet(): Map<EditText, String>? {
        presenter.beforeValidation()

        val values = HashMap<EditText, String>()
        var error = false
        for ((key, value) in fieldsAndRules) {
            if (validate(value) && !error) {
                values.put(key, value.field.text.toString())
            } else {
                error = true
            }
        }
        return if (error) null else values
    }

    fun setErrorMessageTransform(transform: (CharSequence) -> CharSequence) {
        this.transform = transform
    }

    /**
     * Validation
     */
    private fun validate(node: Node): Boolean {
        for (rule in node.rules) {
            val et = node.field
            // if field is not required & empty
            if (rule.toString() !== Preset.REQUIRED.toString() && et.text.toString().isEmpty()) {
                continue
            }
            val result = rule(node.field, et.resources)
            when (result) {
                is ValidationResult.Success -> presenter.setValid(et)
                is ValidationResult.Error -> {
                    presenter.setError(et, transform(result.message))
                    return false
                }
            }.also {  }
        }
        return true
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
            when (this) {
                REQUIRED -> return if (text.isEmpty()) ValidationResult.Error("validation failed for $name") else ValidationResult.Success
                EMAIL -> return if (Patterns.EMAIL_ADDRESS.matcher(text).matches()) ValidationResult.Success else ValidationResult.Error("validation failed for $name")
            }
        }
    }

    inner class EqualTo(private val sample: TextView) : Rule {

        override fun invoke(input: EditText, res: Resources): ValidationResult<*> =
                if (input.text.toString() == sample.text.toString()) ValidationResult.Success
                else ValidationResult.Error("values must be equal")
    }

    /**
     * Internal
     */

    protected class Node(val field: EditText, vararg rules: Rule) {
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
