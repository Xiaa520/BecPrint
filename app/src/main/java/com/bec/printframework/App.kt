package com.bec.printframework

import android.app.Application
import com.bec.printframework.print.utils.PrintUtils

/**
 * Created by mycomputer on 2018/4/12.
 */

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        PrintUtils.getInstance().initPrintConnect(applicationContext)


    }
}
