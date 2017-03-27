package net.aquadc.validation

import android.content.res.Resources
import android.support.annotation.IdRes
import android.support.annotation.StringRes
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.TextView

import java.util.Arrays
import java.util.HashMap
import java.util.LinkedHashMap
import java.util.LinkedHashSet

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
    private var transformer: ((String)->String)? = null

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

    fun setErrorMessageTransformer(transformer: ((String)->String)?) {
        this.transformer = transformer
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
            if (!rule.valid(node.field)) {
                presenter.setError(et, transform(rule.getErrorMessage(et.resources)))
                return false
            } else {
                presenter.setValid(et)
            }
        }
        return true
    }

    /**
     * Validators
     */

    class RuleWithOwnText : Rule {

        private val validationRule: Rule
        @StringRes private val errorMessageRes: Int
        private var errorMessage: String? = null

        constructor(validationRule: Rule, @StringRes errorMessage: Int) {
            this.validationRule = validationRule
            this.errorMessageRes = errorMessage
        }

        constructor(validationRule: Rule, errorMessage: String) {
            this.validationRule = validationRule
            this.errorMessageRes = 0
            this.errorMessage = errorMessage
        }

        override fun valid(input: EditText): Boolean {
            return validationRule.valid(input)
        }

        override fun getErrorMessage(res: Resources): String {
            return errorMessage ?: res.getString(errorMessageRes).also { errorMessage = it }
        }

        override fun toString(): String {
            return validationRule.toString()
        }
    }

    enum class Preset : Rule {
        REQUIRED, EMAIL;

        override fun valid(input: EditText): Boolean {
            val text = input.text.toString()
            when (this) {
                REQUIRED -> return !text.isEmpty()
                EMAIL -> return Patterns.EMAIL_ADDRESS.matcher(text).matches()
            }
        }

        override fun getErrorMessage(res: Resources): String {
            return "validation failed for " + name
        }
    }

    inner class EqualTo(private val sample: TextView) : Rule {

        override fun valid(input: EditText): Boolean {
            return input.text.toString() == sample.text.toString()
        }

        override fun getErrorMessage(res: Resources): String {
            return "values must be equal"
        }
    }

    interface Rule {
        fun valid(input: EditText): Boolean
        fun getErrorMessage(res: Resources): String
    }

    /**
     * Error message transformation
     */
    private fun transform(initial: String): String {
        return transformer?.invoke(initial) ?: initial
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
        fun setError(et: EditText, message: String)
        fun beforeValidation()
    }
}
