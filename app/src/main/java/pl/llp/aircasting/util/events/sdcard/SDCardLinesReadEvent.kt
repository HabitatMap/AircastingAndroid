package pl.llp.aircasting.util.events.sdcard

import pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.SDCardReader

class SDCardLinesReadEvent(val step: SDCardReader.Step, val linesRead: Int)