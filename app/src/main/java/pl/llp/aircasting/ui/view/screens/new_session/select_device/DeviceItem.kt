package pl.llp.aircasting.ui.view.screens.new_session.select_device

import android.bluetooth.BluetoothDevice
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
open class DeviceItem(
    private val mBluetoothDevice: BluetoothDevice? = null,
    open val name: String = mBluetoothDevice?.name ?: UNKNOWN_DEVICE_NAME,
    open val address: String = mBluetoothDevice?.address ?: "",
    open val id: String = name.split(":", "-").last(),
    open val type: Type = getType(name),
    ) : Parcelable {

    companion object {
        const val UNKNOWN_DEVICE_NAME = "Unknown"
        private const val AIRBEAM1_NAME_REGEX = "airbeam"
        private const val AIRBEAM2_NAME_REGEX = "airbeam2"
        private const val AIRBEAM3_NAME_REGEX = "airbeam3"
        private const val AIRBEAMMINI_NAME_REGEX = "airbeammini"

        private fun getType(name: String?): Type {
            name ?: return Type.OTHER

            return when {
                name.contains(AIRBEAM2_NAME_REGEX, true) ->Type.AIRBEAM2
                name.contains(AIRBEAM3_NAME_REGEX, true) -> Type.AIRBEAM3
                name.contains(AIRBEAMMINI_NAME_REGEX, true) -> Type.AIRBEAMMINI
                name.contains(AIRBEAM1_NAME_REGEX, true) -> Type.AIRBEAM1
                else -> Type.OTHER
            }
        }
    }

    enum class Type(val value: Int) {
        OTHER(-1),
        MIC(0),
        AIRBEAM1(1),
        AIRBEAM2(2),
        AIRBEAM3(3),
        AIRBEAMMINI(4);

        companion object {
            fun fromInt(value: Int) = values().first { it.value == value }
        }
    }

    val bluetoothDevice get() = mBluetoothDevice

    fun isAirBeam(): Boolean {
        return arrayOf(Type.AIRBEAM1, Type.AIRBEAM2, Type.AIRBEAM3, Type.AIRBEAMMINI).contains(type)
    }

    override fun toString(): String {
        return "$mBluetoothDevice"
    }
}
