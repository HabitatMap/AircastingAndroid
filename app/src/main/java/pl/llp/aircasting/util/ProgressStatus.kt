package pl.llp.aircasting.util

sealed class OperationStatus {
    object InProgress : OperationStatus()
    object Idle : OperationStatus()
}