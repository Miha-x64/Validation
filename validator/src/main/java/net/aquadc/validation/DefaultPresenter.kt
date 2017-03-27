package net.aquadc.validation

import android.support.design.widget.TextInputLayout
import android.widget.EditText

/**
 * Created by miha on 30.08.16
 */
open class DefaultPresenter : AbsFlushingPresenter() {

    override fun setValid(et: EditText) {
        // no-op
    }

    override fun setError(et: EditText, message: String) {
        var til: TextInputLayout? = null
        run {
            var parent = et.parent
            if (parent is TextInputLayout) {
                til = parent
            } else {
                parent = parent.parent
                if (parent is TextInputLayout) {
                    til = parent
                }
            }
        }

        if (til != null) {
            til!!.error = message
            et.addTextChangedListener(createFlusher(et, til))
        } else {
            et.error = message
            et.addTextChangedListener(createFlusher(et, null))
        }
    }

    override fun clearErrors(et: EditText, til: TextInputLayout?) {
        if (til != null)
            til.error = null
        else
            et.error = null
    }
}
