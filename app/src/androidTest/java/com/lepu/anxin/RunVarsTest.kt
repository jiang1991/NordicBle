package com.lepu.anxin

import com.lepu.anxin.vals.oxyBatArr
import org.junit.Assert
import org.junit.Test

class RunVarsTest {

    @Test
    fun checkRunVars() {
        Assert.assertEquals("80", "${oxyBatArr[100]}")
    }
}