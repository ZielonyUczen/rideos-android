package com.rideos.android.data.vesc

/** VESC communication command IDs used by the RideOS MVP. */
enum class VescCommand(val id: Int) {
    FW_VERSION(0),
    GET_VALUES(4),
    GET_VALUES_SELECTIVE(50)
}
