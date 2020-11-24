package com.lepu.nordicble.utils

import android.content.Context

const val KEY_LEAD_INFO = "lead_info"

const val KEY_HOST_IP = "host_ip"
const val KEY_HOST_PORT = "host_port"

//ER1
const val KEY_ER1_BLE_NAME = "er1_ble_name"

//O2Max
const val KEY_O2Max_BLE_NAME = "o2max_ble_name"

// 康康血压计
const val KEY_KCA_BLE_NAME = "kca_ble_name"

// if lock scrren
const val KEY_LOCK_SCREEN = "lock_scrren"

data class HostConfig(val ip: String?, val port: Int?)

fun saveConfig(context: Context, key: String, value: String) {
    PreferenceUtils.savePreferences(context, key, value)
}

fun readLockScreen(context: Context) : Boolean {

    return PreferenceUtils.readBoolPreferences(context, KEY_LOCK_SCREEN)
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

fun saveEr1Config(context: Context, name: String) {
    PreferenceUtils.savePreferences(context, KEY_ER1_BLE_NAME, name)
}

fun readEr1Config(context: Context) : String? {
    return PreferenceUtils.readStrPreferences(context, KEY_ER1_BLE_NAME)
}

fun clearEr1Config(context: Context) {
    PreferenceUtils.removeStrPreferences(context, KEY_ER1_BLE_NAME)
}


fun saveO2RingConfig(context: Context, name: String) {
    PreferenceUtils.savePreferences(context, KEY_O2Max_BLE_NAME, name)
}

fun readO2RingConfig(context: Context) : String? {
    return PreferenceUtils.readStrPreferences(context, KEY_O2Max_BLE_NAME)
}

fun clearO2MaxConfig(context: Context) {
    PreferenceUtils.removeStrPreferences(context, KEY_O2Max_BLE_NAME)
}


fun saveKlcConfig(context: Context, name: String) {
    PreferenceUtils.savePreferences(context, KEY_KCA_BLE_NAME, name)
}

fun readKlcConfig(context: Context) : String? {
    return PreferenceUtils.readStrPreferences(context, KEY_KCA_BLE_NAME)
}

fun clearKlcConfig(context: Context) {
    PreferenceUtils.removeStrPreferences(context, KEY_KCA_BLE_NAME)
}

fun clearHostConfig(context: Context) {
    PreferenceUtils.removeStrPreferences(context, KEY_HOST_IP)
    PreferenceUtils.removeStrPreferences(context, KEY_HOST_PORT)
}

fun clearConfig(context: Context) {
    PreferenceUtils.removeAllPreferences(context)
}