package com.lepu.nordicble.utils

import android.content.Context

const val KEY_LEAD_INFO = "lead_info"

const val KEY_HOST_IP = "host_ip"
const val KEY_HOST_PORT = "host_port"

//ER1
const val KEY_ER1_BLE_NAME = "ble_name"
const val KEY_ER1_BLE_ADDR = "ble_mac_addr"
const val KEY_ER1_BLE_SN = "ble_sn"

//O2Ring
const val KEY_O2Ring_BLE_NAME = "ble_name"
const val KEY_O2Ring_BLE_ADDR = "ble_mac_addr"
const val KEY_O2Ring_BLE_SN = "ble_sn"

// 康康血压计
const val KEY_KLC_BLE_NAME = "ble_name"
const val KEY_KLC_BLE_ADDR = "ble_mac_addr"
const val KEY_KLC_BLE_SN = "ble_sn"

// if lock scrren
const val KEY_LOCK_SCREEN = "lock_scrren"

data class HostConfig(val ip: String?, val port: Int?)
data class BleConfig(val name: String?, val macAddr: String?, val sn: String?)

fun saveConfig(context: Context, key: String, value: String) {
    PreferenceUtils.savePreferences(context, key, value)
}

fun readLockScreen(context: Context) : Boolean {
    val lock = PreferenceUtils.readBoolPreferences(context, KEY_LOCK_SCREEN)

    return lock
}

fun saveLockScreen(context: Context, lock: Boolean) {
    PreferenceUtils.savePreferences(context, KEY_LOCK_SCREEN, lock)
}

fun readLeadInfo(context: Context) : Int {
    val info = PreferenceUtils.readIntPreferences(context, KEY_LEAD_INFO)

    return if (info == 0) {
        0x02
    } else {
        info
    }
}

fun saveLeadInfo(context: Context, info: Int) {
    PreferenceUtils.savePreferences(context, KEY_LEAD_INFO, info)
}

fun clearLeadInfo(context: Context) {
    saveLeadInfo(context, 0x02)
}

fun saveHostConfig(context: Context, ip: String, port: Int) {
    PreferenceUtils.savePreferences(context, KEY_HOST_IP, ip)
    PreferenceUtils.savePreferences(context, KEY_HOST_PORT, port)
}

fun readHostConfig(context: Context) : HostConfig {

    val ip = PreferenceUtils.readStrPreferences(context, KEY_HOST_IP)
    val port = PreferenceUtils.readIntPreferences(context, KEY_HOST_PORT)

    return HostConfig(ip, port)
}

fun saveEr1Config(context: Context, name: String, macAddr: String, sn: String) {
    PreferenceUtils.savePreferences(context, KEY_ER1_BLE_NAME, name)
    PreferenceUtils.savePreferences(context, KEY_ER1_BLE_ADDR, macAddr)
    PreferenceUtils.savePreferences(context, KEY_ER1_BLE_SN, sn)
}

fun readEr1Config(context: Context) : BleConfig {
    val name = PreferenceUtils.readStrPreferences(context, KEY_ER1_BLE_NAME)
    val macAddr = PreferenceUtils.readStrPreferences(context, KEY_ER1_BLE_ADDR)
    val sn = PreferenceUtils.readStrPreferences(context, KEY_ER1_BLE_SN)

    return BleConfig(name, macAddr, sn)
}

fun clearEr1Config(context: Context) {
    PreferenceUtils.removeStrPreferences(context, KEY_ER1_BLE_NAME)
    PreferenceUtils.removeStrPreferences(context, KEY_ER1_BLE_ADDR)
    PreferenceUtils.removeStrPreferences(context, KEY_ER1_BLE_SN)
}


fun saveO2RingConfig(context: Context, name: String, macAddr: String, sn: String) {
    PreferenceUtils.savePreferences(context, KEY_O2Ring_BLE_NAME, name)
    PreferenceUtils.savePreferences(context, KEY_O2Ring_BLE_ADDR, macAddr)
    PreferenceUtils.savePreferences(context, KEY_O2Ring_BLE_SN, sn)
}

fun readO2RingConfig(context: Context) : BleConfig {
    val name = PreferenceUtils.readStrPreferences(context, KEY_O2Ring_BLE_NAME)
    val macAddr = PreferenceUtils.readStrPreferences(context, KEY_O2Ring_BLE_ADDR)
    val sn = PreferenceUtils.readStrPreferences(context, KEY_O2Ring_BLE_SN)

    return BleConfig(name, macAddr, sn)
}

fun clearO2RingConfig(context: Context) {
    PreferenceUtils.removeStrPreferences(context, KEY_O2Ring_BLE_NAME)
    PreferenceUtils.removeStrPreferences(context, KEY_O2Ring_BLE_ADDR)
    PreferenceUtils.removeStrPreferences(context, KEY_O2Ring_BLE_SN)
}


fun saveKlcConfig(context: Context, name: String, macAddr: String, sn: String) {
    PreferenceUtils.savePreferences(context, KEY_KLC_BLE_NAME, name)
    PreferenceUtils.savePreferences(context, KEY_KLC_BLE_ADDR, macAddr)
    PreferenceUtils.savePreferences(context, KEY_KLC_BLE_SN, sn)
}

fun readKlcConfig(context: Context) : BleConfig {
    val name = PreferenceUtils.readStrPreferences(context, KEY_KLC_BLE_NAME)
    val macAddr = PreferenceUtils.readStrPreferences(context, KEY_KLC_BLE_ADDR)
    val sn = PreferenceUtils.readStrPreferences(context, KEY_KLC_BLE_SN)

    return BleConfig(name, macAddr, sn)
}

fun clearKlcConfig(context: Context) {
    PreferenceUtils.removeStrPreferences(context, KEY_KLC_BLE_NAME)
    PreferenceUtils.removeStrPreferences(context, KEY_KLC_BLE_ADDR)
    PreferenceUtils.removeStrPreferences(context, KEY_KLC_BLE_SN)
}

fun clearHostConfig(context: Context) {
    PreferenceUtils.removeStrPreferences(context, KEY_HOST_IP)
    PreferenceUtils.removeStrPreferences(context, KEY_HOST_PORT)
}

fun clearConfig(context: Context) {
    PreferenceUtils.removeAllPreferences(context)
}