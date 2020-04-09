package io.lunarlogic.aircasting.devices

class Device(private var mName: String, private var mId: String) {
    val name: String
        get() = mName

    val id: String
        get() = mId
}