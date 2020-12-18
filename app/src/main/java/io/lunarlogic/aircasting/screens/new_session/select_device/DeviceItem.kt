package io.lunarlogic.aircasting.screens.new_session.select_device

import android.bluetooth.BluetoothDevice

open class DeviceItem(private val mBluetoothDevice: BluetoothDevice? = null) {
    open val name: String
    open val address: String
    open val id: String
    open val type: Type

    companion object {
        private val AIRBEAM2_NAME_REGEX = "airbeam2"
        private val AIRBEAM3_NAME_REGEX = "airbeam3"

        fun isAirBeam3(sensorPackageName: String?): Boolean {
            return getType(sensorPackageName) == Type.AIRBEAM3
        }

        private fun getType(name: String?): Type {
            name ?: return Type.OTHER

            if (name.contains(AIRBEAM2_NAME_REGEX, true)) {
                return Type.AIRBEAM2
            }

            if (name.contains(AIRBEAM3_NAME_REGEX, true)) {
                return Type.AIRBEAM3
            }

            return Type.OTHER
        }
    }

    init {
        name = mBluetoothDevice?.name ?: "Unknown"
        address = mBluetoothDevice?.address ?: ""
        id = name.split(":", "-").last()
        type = getType(name)
    }

    enum class Type(val value: Int) {
        OTHER(-1),
        AIRBEAM2(0),
        AIRBEAM3(1),
        MIC(2);

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
        return arrayOf(Type.AIRBEAM2, Type.AIRBEAM3).contains(type)
    }
}
