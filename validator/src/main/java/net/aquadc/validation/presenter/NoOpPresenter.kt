package net.aquadc.validation.presenter

import android.widget.EditText
import net.aquadc.validation.Validation

object NoOpPresenter : Validation.Presenter {

    override fun beforeValidation() {
    }

    override fun setValid(et: EditText) {
    }

    override fun setError(et: EditText, message: CharSequence) {
    }

}
