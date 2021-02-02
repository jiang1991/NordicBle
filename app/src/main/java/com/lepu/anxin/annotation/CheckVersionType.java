package com.lepu.anxin.annotation;


import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/***
 // 0: 2.4G   1: 5G   2:双频
 *****/
@Retention(RetentionPolicy.SOURCE)
@StringDef({CheckVersionType.WIRELESS,
        CheckVersionType.ANXINBAO,
        CheckVersionType.ADAPTER
})
public @interface CheckVersionType {

    String  WIRELESS =  "wireless";
    String  ANXINBAO = "anxinbao";
    String  ADAPTER = "adapter";

}
