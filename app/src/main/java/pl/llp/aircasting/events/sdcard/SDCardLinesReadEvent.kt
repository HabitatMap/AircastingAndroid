package pl.llp.aircasting.events.sdcard

import pl.llp.aircasting.sensor.airbeam3.sync.SDCardReader

class SDCardLinesReadEvent(val step: SDCardReader.Step, val linesRead: Int)
