package com.lepu.nordicble

import com.lepu.nordicble.vals.oxyBatArr
import org.junit.Assert
import org.junit.Test

class RunVarsTest {

    @Test
    fun checkRunVars() {
        Assert.assertEquals("80", "${oxyBatArr[100]}")
    }
}