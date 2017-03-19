package net.aquadc.validation

import android.support.annotation.IdRes
import android.support.design.widget.TextInputLayout
import android.view.View
import android.widget.EditText
import android.widget.TextView

/**
 * Created by miha on 30.08.16
 */
class SingleErrorViewPresenter : AbsFlushingPresenter {

    private val output: TextView

    constructor(root: View, output: TextView) : super(root) {
        this.output = output
    }

    constructor(root: View, @IdRes output: Int) : super(root) {
        this.output = root.findViewById(output) as TextView
    }

    private var hasError: Boolean = false

    override fun setError(et: EditText, message: String) {
        if (!hasError) {
            output.text = message
            hasError = true
            et.addTextChangedListener(Flusher(activeFlushers, et))
        }
    }

    override fun beforeValidation() {
        // clear errors
        super.beforeValidation()
        hasError = false
    }

    override fun clearErrors(et: EditText, til: TextInputLayout?) {
        output.text = ""
    }
}
