package com.bec.printframework.print.utils.inter

import com.gprinter.aidl.GpService
import net.posprinter.posprinterface.IMyBinder

/**
 * Created by mycomputer on 2018/3/26.
 */

interface IPrintObject{
    fun getIMyBinder(mIMyBinder: IMyBinder)
    fun getGpService(mGpService: GpService)
}
