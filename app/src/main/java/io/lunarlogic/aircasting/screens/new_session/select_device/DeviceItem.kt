package io.lunarlogic.aircasting.screens.new_session.select_device

import android.bluetooth.BluetoothDevice
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
open class DeviceItem(private val mBluetoothDevice: BluetoothDevice? = null) : Parcelable {
    open val name: String
    open val address: String
    open val id: String
    open val type: Type

    companion object {
        private val AIRBEAM1_NAME_REGEX = "airbeam"
        private val AIRBEAM2_NAME_REGEX = "airbeam2"
        private val AIRBEAM3_NAME_REGEX = "airbeam3"
    }

    init {
        name = mBluetoothDevice?.name ?: "Unknown"
        address = mBluetoothDevice?.address ?: ""
        id = name.split(":", "-").last()
        type = getType(name)
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

    fun displayName(): String {
        if (isAirBeam()) {
            return name.split(":", "-").first()
        }

        return name
    }

    fun isAirBeam(): Boolean {
        return arrayOf(Type.AIRBEAM1, Type.AIRBEAM2, Type.AIRBEAM3).contains(type)
    }

    fun isSyncable(): Boolean {
        return arrayOf(Type.AIRBEAM3).contains(type)
    }

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
