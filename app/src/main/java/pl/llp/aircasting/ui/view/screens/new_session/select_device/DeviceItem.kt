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
        private val AIRBEAM1_NAME_REGEX = "airbeam"
        private val AIRBEAM2_NAME_REGEX = "airbeam2"
        private val AIRBEAM3_NAME_REGEX = "airbeam3"

        private fun getType(name: String?): Type {
            name ?: return Type.OTHER

            if (name.contains(AIRBEAM2_NAME_REGEX, true)) {
                return Type.AIRBEAM2
            }

            if (name.contains(AIRBEAM3_NAME_REGEX, true)) {
                return Type.AIRBEAM3
            }

            if (name.contains(AIRBEAM1_NAME_REGEX, true)) {
                return Type.AIRBEAM1
            }

            return Type.OTHER
        }
    }

    enum class Type(val value: Int) {
        OTHER(-1),
        MIC(0),
        AIRBEAM1(1),
        AIRBEAM2(2),
        AIRBEAM3(3);

        companion object {
            fun fromInt(value: Int) = values().first { it.value == value }
        }
    }

    val bluetoothDevice get() = mBluetoothDevice

    fun isAirBeam(): Boolean {
        return arrayOf(Type.AIRBEAM1, Type.AIRBEAM2, Type.AIRBEAM3).contains(type)
    }

    fun isSyncable(): Boolean {
        return arrayOf(Type.AIRBEAM3).contains(type)
    }
}
