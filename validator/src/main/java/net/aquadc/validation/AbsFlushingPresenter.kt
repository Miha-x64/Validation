package net.aquadc.validation

import android.support.design.widget.TextInputLayout
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import java.util.*

/**
 * Created by miha on 30.08.16
 */
abstract class AbsFlushingPresenter : Validation.Presenter {

    protected val activeFlushers: MutableList<Flusher> = ArrayList()

    override fun beforeValidation() {
        // remove old error message flushers
        val iterator = activeFlushers.iterator()
        while (iterator.hasNext()) {
            val fl = iterator.next()
            fl.clearErrors()   // remove error message from TextInputLayout
            fl.run()           // remove TextWatcher from TextView
            iterator.remove()
        }
    }

    protected abstract fun clearErrors(et: EditText, til: TextInputLayout?)

    protected open fun createFlusher(et: EditText, til: TextInputLayout?) = Flusher(et, til)

    protected open inner class Flusher constructor(
            protected val et: EditText,
            protected val til: TextInputLayout?
    ) : TextWatcher, Runnable {

        init {

            activeFlushers.add(this)
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable) {
            clearErrors()

            et.postDelayed(this, 0)
            activeFlushers.remove(this)
        }

        fun clearErrors() {
            this@AbsFlushingPresenter.clearErrors(et, til)
        }

        override fun run() {
            et.removeTextChangedListener(this)
        }
    }

}
