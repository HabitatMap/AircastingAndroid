package pl.llp.aircasting.util.databinding

import androidx.databinding.InverseMethod

object Converter {
    @JvmStatic
    fun doubleToString(value: Double?) =
        value?.toString() ?: ""

    @InverseMethod("doubleToString")
    @JvmStatic
    fun stringToDouble(value: String) = value.toDoubleOrNull()
}