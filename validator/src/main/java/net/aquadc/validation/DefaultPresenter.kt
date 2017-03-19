package net.aquadc.validation

import android.support.design.widget.TextInputLayout
import android.view.View
import android.widget.EditText

/**
 * Created by miha on 30.08.16
 */
class DefaultPresenter(root: View) : AbsFlushingPresenter(root) {

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
            et.addTextChangedListener(Flusher(activeFlushers, et, til))
        } else {
            et.error = message
            et.addTextChangedListener(Flusher(activeFlushers, et))
        }
    }

    override fun clearErrors(et: EditText, til: TextInputLayout?) {
        if (til != null)
            til.error = null
        else
            et.error = null
    }
}
