package pl.llp.aircasting.util.events

/**
 * Posted by [pl.llp.aircasting.util.helpers.sensor.services.AirBeamSyncService] right before
 * the V2 manual-sync flow opens the system Wi-Fi picker. The active syncing UI surfaces an
 * educational dialog and replies with [V2WifiPickerEducationConfirmedEvent] once the user
 * acknowledges, unblocking the orchestrator.
 */
class V2WifiPickerEducationRequestedEvent

class V2WifiPickerEducationConfirmedEvent
