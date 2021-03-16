package io.lunarlogic.aircasting.events.sdcard

import io.lunarlogic.aircasting.sensor.airbeam3.sync.SDCardReader

class SDCardLinesReadEvent(val step: SDCardReader.Step, val linesRead: Int)
