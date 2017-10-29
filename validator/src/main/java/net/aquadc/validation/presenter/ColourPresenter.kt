package net.aquadc.validation.presenter

import android.support.annotation.ColorInt
import android.widget.EditText
import net.aquadc.validation.Validation

class ColourPresenter(
        @param:ColorInt private val validColour: Int,
        @param:ColorInt private val errorColour: Int
) : Validation.Presenter {

    override fun beforeValidation() {
    }

    override fun setValid(et: EditText) {
        et.setTextColor(validColour)
    }

    override fun setError(et: EditText, message: CharSequence) {
        et.setTextColor(errorColour)
    }

}
