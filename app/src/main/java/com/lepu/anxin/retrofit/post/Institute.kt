package com.lepu.anxin.retrofit.post

import com.google.gson.Gson

class Institute(var id: String) {

    override fun toString(): String {
        return Gson().toJson(this)
    }
}