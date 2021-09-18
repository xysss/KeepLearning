package com.xysss.keeplearning.viewmodel.state

import com.xysss.jetpackmvvm.base.viewmodel.BaseViewModel
import com.xysss.jetpackmvvm.callback.databind.StringObservableField

/**
 * Author:bysd-2
 * Time:2021/9/1716:40
 */
class WelcomeViewModel :BaseViewModel(){

    val welcomeJoinHint= StringObservableField("立即进入")
}