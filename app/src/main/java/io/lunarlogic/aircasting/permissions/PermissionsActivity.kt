package io.lunarlogic.aircasting.permissions

interface PermissionsActivity {
    fun bluetoothPermissionsGranted(permissionsManager: PermissionsManager) : Boolean
    fun requestBluetoothPermissions(permissionsManager: PermissionsManager)
    fun requestBluetoothEnable()

    fun audioPermissionsGranted(permissionsManager: PermissionsManager) : Boolean
    fun requestAudioPermissions(permissionsManager: PermissionsManager)
}