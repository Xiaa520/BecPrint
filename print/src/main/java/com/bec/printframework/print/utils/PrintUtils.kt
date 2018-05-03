package com.bec.printframework.print.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.*
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.*
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.widget.Toast
import com.bec.printframework.print.utils.StringUtils.encodeAsBitmap
import com.bec.printframework.print.utils.inter.IPrintObject

import com.bec.printframework.print.utils.service.BecPosPrinterService
import com.bec.printframework.print.utils.view.CustomProgress
import com.gprinter.aidl.GpService
import com.gprinter.command.EscCommand
import com.gprinter.command.GpCom
import com.gprinter.command.GpUtils
import com.gprinter.command.LabelCommand
import com.gprinter.io.GpDevice
import com.gprinter.service.GpPrintService
import net.posprinter.posprinterface.IMyBinder
import net.posprinter.posprinterface.ProcessData
import net.posprinter.posprinterface.UiExecute
import net.posprinter.utils.*
import org.jetbrains.anko.toast
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.util.*

/**
 * Created by mycomputer on 2018/3/22.
 */

class PrintUtils {
    var listXP = ArrayList<ByteArray>()
    var ipAddress = ""
    var esc: EscCommand? = null
    //获取app对象
    var context: Context? = null
    var mGpService: GpService? = null
    //Xp打印机服务
    var binder: IMyBinder? = null


    //重置时间
    var resetTime = 20000

    var connectFailTime = 0L

    //广播是否注册
    var mReceiverTag = false

    /**
     *  NO_CONNECTION 未连接  DISCONNECT 断开连接 PRINT_FAIL 打印失败
     */
    enum class ExceptionState {
        NO_CONNECTION,
        DISCONNECT,
        PRINT_FAIL
    }


    /**
     ** 初始化并连接打印机
     */
    fun initPrintConnect(context: Context): PrintUtils {

        this.context = context
        MACHINE_NUMBER = loadMachineNumber().toString()
        if (connectFailTime == 0L) connectFailTime = System.currentTimeMillis()

        if (MACHINE_NUMBER != null && MACHINE_NUMBER.length > 10) {
            val type = MACHINE_NUMBER.substring(1, 5)
            when (type) {
                "1710" -> {
                    //1710一代机
                    SOFT_TYPE = 1
                    IS_ACTIVITE = true
                    conn = PrinterServiceConnection()
                    val intentG = Intent(context, GpPrintService::class.java)
                    context.bindService(intentG, conn, Context.BIND_AUTO_CREATE)
                    if (!mReceiverTag) registerBroadcast(context)
                }
                "1720" -> {
                    //1720二代机
                    IS_ACTIVITE = true
                    SOFT_TYPE = 3
                    connXPrinter = XPrinterServiceConnection()
                    val intentX = Intent(context, BecPosPrinterService::class.java)
                    context.bindService(intentX, connXPrinter, Context.BIND_AUTO_CREATE)
                }
                else -> {
                    SOFT_TYPE = 2
                    IS_ACTIVITE = false
                }

            }

        } else {
            IS_ACTIVITE = false
        }

        return this
    }


    /**
     **  先初始化打印机，清除缓存
     */
    fun initializePrinter(ativity:Activity): PrintUtils {
        CustomProgress.show(ativity, "打印中...", false, null)
        connectFailTime = System.currentTimeMillis()
        if (SOFT_TYPE == 1) { //一代机 gp
            esc = EscCommand()
            esc?.addInitializePrinter()
        } else if (SOFT_TYPE == 3) { ////二代机 xp
            listXP.clear()
            listXP.add(DataForSendToPrinterPos80.initializePrinter())
        }
        return this
    }


    /**
     **  打印机字体大小
     */
    fun setFont(mFont: Font): PrintUtils {
        if (SOFT_TYPE == 1) { //一代机 gp
            if (mFont == Font.BIG) {
                //大文字
                esc?.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF, EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF)// 设置为倍高倍宽
                printAndFeed(3)
            } else if (mFont == Font.LITTLE) {
                //小文字
                esc?.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF)// 取消倍高倍宽
                printAndFeed(1)
            }


        } else if (SOFT_TYPE == 3) { //二代机 xp
            if (mFont == Font.BIG) {
                //大文字
                listXP.add(byteArrayOf(0x1D, 0x21, 0x11))
                listXP.add(DataForSendToPrinterPos58.printAndFeedLine())
            } else if (mFont == Font.LITTLE) {
                //小文字
                listXP.add(byteArrayOf(0x1D, 0x21, 0x00))
                printAndFeed(1)
            }
        }
        return this
    }

    /**
     **  打印机文字
     *  text 打印内容
     *
     *  Orientation 打印方向
     */
    fun addText(text: String, mOrientation: Orientation): PrintUtils {
        if (SOFT_TYPE == 1) { //一代机 gp
            setOrientation(mOrientation)
            esc?.addText(text)
            esc?.addPrintAndLineFeed()
        } else if (SOFT_TYPE == 3) { //二代机 xp
            setOrientation(mOrientation)
            listXP.add(StringUtils.strTobytes(text))
            listXP.add(DataForSendToPrinterPos80.printAndFeedLine())
        }
        return this
    }

    /**
     **  打印机文字
     *  text 打印内容
     *
     *  Orientation 打印方向
     */
    fun addText(leftText: String, centreText: String, rightText: String, isAndLineFeed: Boolean): PrintUtils {
        if (SOFT_TYPE == 1) { //一代机 gp
            setOrientation(Orientation.LEFT)
            esc?.addText(leftText)
            esc?.addSetHorAndVerMotionUnits(7.toByte(), 0.toByte())
            esc?.addSetAbsolutePrintPosition(10.toShort())
            esc?.addText(centreText)
            esc?.addSetAbsolutePrintPosition(18.toShort())
            esc?.addText(rightText)
            if (isAndLineFeed) esc?.addPrintAndLineFeed()
        } else if (SOFT_TYPE == 3) { //二代机 xp
            setOrientation(Orientation.LEFT)
            listXP.add(StringUtils.strTobytes(leftText))
            listXP.add(DataForSendToPrinterPos80.setHorizontalAndVerticalMoveUnit(4, 4))
            listXP.add(DataForSendToPrinterPos80.setAbsolutePrintPosition(5, 0))
            listXP.add(StringUtils.strTobytes(centreText))
            listXP.add(DataForSendToPrinterPos80.setAbsolutePrintPosition(9, 0))
            listXP.add(StringUtils.strTobytes(rightText))
            if (isAndLineFeed) listXP.add(DataForSendToPrinterPos80.printAndFeedLine())
        }
        return this
    }

    /**
     **  打印机文字
     *  text 打印内容
     *
     *  Orientation 打印方向
     */
    fun addText(text1: String, text2: String, text3: String,text4: String, isAndLineFeed: Boolean): PrintUtils {
        if (SOFT_TYPE == 1) { //一代机 gp
            setOrientation(Orientation.LEFT)
            esc?.addText(text1)
            esc?.addSetHorAndVerMotionUnits(7.toByte(), 0.toByte())
            esc?.addSetAbsolutePrintPosition(8.toShort())

            123456

            esc?.addText(text2)
            esc?.addSetAbsolutePrintPosition(14.toShort())
            esc?.addText(text3)
            esc?.addSetAbsolutePrintPosition(19.toShort())
            esc?.addText(text4)
            if (isAndLineFeed) esc?.addPrintAndLineFeed()
        } else if (SOFT_TYPE == 3) { //二代机 xp
            setOrientation(Orientation.LEFT)
            listXP.add(StringUtils.strTobytes(text1))
            listXP.add(DataForSendToPrinterPos80.setHorizontalAndVerticalMoveUnit(4, 4))
            listXP.add(DataForSendToPrinterPos80.setAbsolutePrintPosition(4, 0))
            listXP.add(StringUtils.strTobytes(text2))
            listXP.add(DataForSendToPrinterPos80.setAbsolutePrintPosition(7, 0))
            listXP.add(StringUtils.strTobytes(text3))
            listXP.add(DataForSendToPrinterPos80.setAbsolutePrintPosition(10, 0))
            listXP.add(StringUtils.strTobytes(text4))
            if (isAndLineFeed) listXP.add(DataForSendToPrinterPos80.printAndFeedLine())
        }
        return this
    }

    /**
     **  打印机条形码
     *  text 打印内容
     */
    fun addCode(text: String, mOrientation: Orientation): PrintUtils {
        if (SOFT_TYPE == 1) { //一代机 gp
            setOrientation(mOrientation)
            esc?.addSetBarcodeHeight(80.toByte()) // 设置条码高度为80点
            esc?.addSetBarcodeWidth(2.toByte()) // 设置条码单元宽度为3
            esc?.addCODE128(esc?.genCodeB(text)) // 打印Code128码
            esc?.addPrintAndLineFeed()
        } else if (SOFT_TYPE == 3) { //二代机 xp
            setOrientation(mOrientation)
            //设置条形码宽度
            listXP.add(DataForSendToPrinterPos80.setBarcodeWidth(3))
            //设置条形码高度
            listXP.add(DataForSendToPrinterPos80.setBarcodeHeight(100))
            listXP.add(DataForSendToPrinterPos80.selectHRICharacterPrintPosition(2))
            //设置条形码内容
            listXP.add(DataForSendToPrinterPos80.printBarcode(5, text))//69
            listXP.add(DataForSendToPrinterPos80.printAndFeedLine())
        }
        return this
    }

    /**
     **  打印机二维码
     *  text 打印内容
     */
    fun addQRCode(text: String, mOrientation: Orientation): PrintUtils {
        if (SOFT_TYPE == 1) { //一代机 gp
            setOrientation(mOrientation)
            esc?.addSelectErrorCorrectionLevelForQRCode(0x31.toByte()) // 设置纠错等级
            esc?.addSelectSizeOfModuleForQRCode(8.toByte())// 设置qrcode模块大小
            esc?.addStoreQRCodeData(text)
            esc?.addPrintQRCode()
            esc?.addPrintAndLineFeed()
        } else if (SOFT_TYPE == 3) { //二代机 xp
            setOrientation(mOrientation)
            listXP.add(DataForSendToPrinterPos80.selectAlignment(1))
            listXP.add(DataForSendToPrinterPos80.printRasterBmp(0, encodeAsBitmap(text), BitmapToByteData.BmpType.Threshold, BitmapToByteData.AlignType.Center, 576))
            listXP.add(DataForSendToPrinterPos80.printAndFeedLine())
        }
        return this
    }


    /**
     **  打印机间距
     */
    fun printAndFeed(n: Int): PrintUtils {
        if (SOFT_TYPE == 1) { //一代机 gp
            esc?.addPrintAndFeedLines(1)
        } else if (SOFT_TYPE == 3) { //二代机 xp
            listXP.add(DataForSendToPrinterPos80.printAndFeedLine())
        }
        return this
    }

    /**
     **  打开钱箱
     */
    fun openCashBox(): PrintUtils {
        if (SOFT_TYPE == 1) { //一代机 gp
            esc?.addInitializePrinter()
            esc?.addGeneratePlus(LabelCommand.FOOT.F2, 255.toByte(), 255.toByte())

            try {
                esc?.addPrintAndFeedLines(8.toByte())
                mGpService?.sendEscCommand(0, Base64.encodeToString(GpUtils.ByteTo_byte(esc?.command), Base64.DEFAULT))
            } catch (e: RemoteException) {
                e.printStackTrace()
            }

        } else if (SOFT_TYPE == 3) { //二代机 xp
            binder?.writeDataByYouself(
                    object : UiExecute {
                        override fun onsucess() {

                        }

                        override fun onfailed() {
                            context?.toast("打开钱箱失败,请用钥匙打开!")
                        }
                    }, ProcessData {

                val list = ArrayList<ByteArray>()

                list.add(DataForSendToPrinterPos80.initializePrinter())

                list.add(DataForSendToPrinterPos80.creatCashboxContorlPulse(0, 255, 255))

                return@ProcessData list
                null
            })

        }
        return this
    }


    /**
     ** 切纸并打印
     */
    fun addCutPaper(): PrintUtils {
        if (!ISCONNECT) {
            resetConnentFail()
        } else
            if (SOFT_TYPE == 1) { //一代机 gp
                try {
                    esc?.addPrintAndFeedLines(4.toByte())
                    esc?.addCutPaper()
                    val escCommand = mGpService?.sendEscCommand(0, Base64.encodeToString(GpUtils.ByteTo_byte(esc?.command), Base64.DEFAULT))
                    val r = GpCom.ERROR_CODE.values()[escCommand!!]
                    if (r != GpCom.ERROR_CODE.SUCCESS) {
                        resetConnentFail()
                    } else
                        CustomProgress.disMiss()
                } catch (e: RemoteException) {
                    e.printStackTrace()


                }
            } else if (SOFT_TYPE == 3) { //二代机 xp
                Log.i("listXP", "外面" + listXP.size)
                listXP.add(DataForSendToPrinterPos80.selectCutPagerModerAndCutPager(66, 1))
                binder?.writeDataByYouself(
                        object : UiExecute {
                            override fun onsucess() {
                                listXP.clear()
                                CustomProgress.disMiss()
                                resetPrintTime()
                                Log.i("listXP", "onsucess" + listXP.size)
                            }

                            override fun onfailed() {
                                if (listXP.size > 0) listXP.removeAt(listXP.size - 1)
                                resetConnentFail()
                                Log.i("listXP", "onfailed" + listXP.size)
                            }
                        }, ProcessData {
                    return@ProcessData listXP
                })

            }
        return this
    }


    /**
     ** 设置打印方向
     */
    fun setOrientation(mOrientation: Orientation): PrintUtils {
        if (SOFT_TYPE == 1) { //一代机 gp
            when (mOrientation) {
                Orientation.LEFT -> { //左边
                    esc?.addSelectJustification(EscCommand.JUSTIFICATION.LEFT)
                }
                Orientation.CENTRE -> { //中间
                    esc?.addSelectJustification(EscCommand.JUSTIFICATION.CENTER)
                }
                Orientation.RIGHT -> { //左边
                    esc?.addSelectJustification(EscCommand.JUSTIFICATION.RIGHT)
                }
            }
        } else if (SOFT_TYPE == 3) { //二代机 xp
            when (mOrientation) {
                Orientation.LEFT -> { //左边
                    //居中显示 0 左边 1中间 2 右边
                    listXP.add(DataForSendToPrinterPos80.selectAlignment(0))
                }
                Orientation.CENTRE -> { //中间
                    //居中显示 0 左边 1中间 2 右边
                    listXP.add(DataForSendToPrinterPos80.selectAlignment(1))

                }
                Orientation.RIGHT -> { //左边
                    //居中显示 0 左边 1中间 2 右边
                    listXP.add(DataForSendToPrinterPos80.selectAlignment(2))
                }
            }
        }
        return this
    }


    /**
     ** 退出打印服务
     */
    fun exitPrint() {
        ISCONNECT = false
        try {
            if (SOFT_TYPE == 1) { //一代机 GP
                if (conn != null) {
                    context?.unbindService(conn)
                    conn = null
                }
                if (printerStatusBroadcastReceiver != null) {
                    context?.unregisterReceiver(printerStatusBroadcastReceiver)
                    mReceiverTag = false
                }
            } else if (SOFT_TYPE == 3) { // 二代机XP
                binder?.disconnectCurrentPort(object : UiExecute {
                    override fun onsucess() {

                    }

                    override fun onfailed() {

                    }
                })
                if (connXPrinter != null) {
                    context?.unbindService(connXPrinter)
                    connXPrinter = null
                }

            }
        } catch (e: Exception) {
        }
    }

    /**
     **  获取打印对象
     */
    fun getPrintObject(mIPrintObject: IPrintObject) {
        this.mIPrintObject = mIPrintObject
    }

    var mIPrintObject: IPrintObject? = null

    /**
     * 字体 大小
     */
    enum class Font {
        BIG,
        LITTLE
    }

    /**
     * 打印方向
     */
    enum class Orientation {
        LEFT,
        CENTRE,
        RIGHT
    }

    companion object {
        var MACHINE_NUMBER = ""
        //1:零售版1代机器，2：加油站版，3：零售版2代机器
        var SOFT_TYPE = 0
        //打印机是否连接
        var ISCONNECT = false
        //机器是否经过授权
        var IS_ACTIVITE = false


        private var instancePrintUtils: PrintUtils? = null
        fun getInstance(): PrintUtils {
            if (instancePrintUtils == null) instancePrintUtils = PrintUtils()
            return instancePrintUtils!!
        }


    }


    private var connXPrinter: XPrinterServiceConnection? = null

    //bindService的参数connection
    private inner class XPrinterServiceConnection : ServiceConnection {

        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {

            binder = iBinder as IMyBinder
            if (mIPrintObject != null) {
                mIPrintObject?.getIMyBinder(binder!!)
            }
            val usbList = ArrayList<String>()

            try {
                usbList.addAll(PosPrinterDev.GetUsbPathNames(context))
            } catch (e: NullPointerException) {
                e.printStackTrace()
                usbList.clear()
                if (System.currentTimeMillis() - connectFailTime > resetTime) {
                    context?.toast("没有找到设备,请重试!")
                }

            }

            if (!usbList.isEmpty()) {

                //打开USB链接，连接USB打印机
                binder!!.connectUsbPort(context, if (usbList[0] == null) "" else usbList[0], object : UiExecute {

                    override fun onsucess() {
                        ISCONNECT = true

                        if (listXP.size > 0) {
                            addCutPaper()
                        }

                    }

                    override fun onfailed() {
                        ISCONNECT = false
                        resetConnentFail()
                    }
                })

            } else {
                resetConnentFail()
            }

        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            binder = null
            resetConnentFail()
        }
    }


    /**
     *
     * 重置连接失败时间
     *
     */
    fun resetPrintTime() {
        connectFailTime = 0
    }


    /**
     *
     * 连接失败 静默连接
     *
     */
    fun resetConnentFail() {
        if (System.currentTimeMillis() - connectFailTime < resetTime) {
            if (context != null) initPrintConnect(context!!)
        } else {
            CustomProgress.disMiss()
            context?.toast("打印机连接失败,请退出重试！")
        }
    }


    private var conn: PrinterServiceConnection? = null

    private inner class PrinterServiceConnection : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mGpService = GpService.Stub.asInterface(service)
            if (mIPrintObject != null) {
                mIPrintObject?.getGpService(mGpService!!)
            }
            try {
                mGpService!!.openPort(0, 2, if (getUsbDeviceList() == null) "" else getUsbDeviceList(), 0)
                ISCONNECT = true

                if (esc != null && !TextUtils.isEmpty(Base64.encodeToString(GpUtils.ByteTo_byte(esc?.command), Base64.DEFAULT))) {
                    addCutPaper()
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
                ISCONNECT = false
                resetConnentFail()
            }

        }

        override fun onServiceDisconnected(name: ComponentName) {
            ISCONNECT = false
            mGpService = null
            resetConnentFail()
        }

    }


    private fun registerBroadcast(context: Context) {
        context.registerReceiver(printerStatusBroadcastReceiver, IntentFilter(GpCom.ACTION_CONNECT_STATUS))
        context.registerReceiver(printerStatusBroadcastReceiver, IntentFilter(GpCom.ACTION_DEVICE_REAL_STATUS))
        context.registerReceiver(printerStatusBroadcastReceiver, IntentFilter(GpCom.ACTION_RECEIPT_RESPONSE))
        mReceiverTag = true
    }

    val MAIN_QUERY_PRINTER_STATUS = 0xfe

    private val printerStatusBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            val action = intent.action

            if (action != null && action == GpCom.ACTION_CONNECT_STATUS) {
                val type = intent.getIntExtra(GpPrintService.CONNECT_STATUS, 0)
                val id = intent.getIntExtra(GpPrintService.PRINTER_ID, 0)
                var str = ""
                when (type) {

                    GpDevice.STATE_CONNECTING -> {//连接
                        str = "连接"
                        ISCONNECT = true
                    }
                    GpDevice.STATE_NONE -> { //无
                        str = "无"
                        resetConnentFail()
                        ISCONNECT = false
                    }
                    GpDevice.STATE_VALID_PRINTER -> {//打印机有效
                        str = "打印机有效"
                        ISCONNECT = true
                    }

                    GpDevice.STATE_INVALID_PRINTER -> { //打印机无效
                        str = "打印机无效"
                        ISCONNECT = false

                    }
                }
                Log.i("registerReceiver", str)

            } else if (action != null && action == GpCom.ACTION_DEVICE_REAL_STATUS) {

                // 业务逻辑的请求码，对应哪里查询做什么操作
                val requestCode = intent.getIntExtra(GpCom.EXTRA_PRINTER_REQUEST_CODE, -1)
                // 判断请求码，是则进行业务操作
                if (requestCode == MAIN_QUERY_PRINTER_STATUS) {

                    val status = intent.getIntExtra(GpCom.EXTRA_PRINTER_REAL_STATUS, 16)
                    var str: String
                    if (status == GpCom.STATE_NO_ERR) {
                        str = "打印机正常"
                    } else {
                        str = "打印机 "
                        if ((status and GpCom.STATE_OFFLINE).toByte() > 0) {
                            str += "脱机"
                        }
                        if ((status and GpCom.STATE_PAPER_ERR).toByte() > 0) {
                            str += "缺纸"
                        }
                        if ((status and GpCom.STATE_COVER_OPEN).toByte() > 0) {
                            str += "打印机开盖"
                        }
                        if ((status and GpCom.STATE_ERR_OCCURS).toByte() > 0) {
                            str += "打印机出错"
                        }
                        if ((status and GpCom.STATE_TIMES_OUT).toByte() > 0) {
                            str += "查询超时"
                        }
                    }
                    Log.i("registerReceiver", str)
                }
            }


        }
    }


    private fun getUsbDeviceList(): String {
        val manager = context?.getSystemService(Context.USB_SERVICE) as UsbManager
        // Get the list of attached devices
        val devices = manager.deviceList
        val deviceIterator = devices.values.iterator()
        val count = devices.size
        if (count > 0) {
            while (deviceIterator.hasNext()) {
                val device = deviceIterator.next()
                val deviceName = device.deviceName
                if (checkUsbDevicePidVid(device)) {
//                    MyLog.i("DeviceAddress:", deviceName)
                    return deviceName ?: ""
                } else {
                    return ""
                }
            }
            return ""
        } else {
            context?.toast("无连接的打印机")
            return ""
        }
    }

    private fun checkUsbDevicePidVid(dev: UsbDevice): Boolean {
        val pid = dev.productId
        val vid = dev.vendorId
        var rel = false
        if (vid == 34918 && pid == 256 || vid == 1137 && pid == 85
                || vid == 6790 && pid == 30084 || vid == 26728 && pid == 256
                || vid == 26728 && pid == 512 || vid == 26728 && pid == 768
                || vid == 26728 && pid == 1024 || vid == 26728 && pid == 1280
                || vid == 26728 && pid == 1536) {
            rel = true
        }
        return rel
    }


    val SECRET_KEY = "7BDAD17CE71BF240B2F80F72DA9734F088E4E846"

    private fun loadMachineNumber(): String? {

        val tempFile = File(Environment.getExternalStorageDirectory().path + "/.BEC/.BEC_DEVICE_ID.sys")

        if (tempFile.exists()) {

            var br: BufferedReader? = null

            var fileReader: FileReader? = null

            var decryStr: String? = null

            try {
                fileReader = FileReader(tempFile)
                br = BufferedReader(FileReader(tempFile))
                decryStr = AesUtils.decrypt(SECRET_KEY, br.readLine())
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    if (fileReader != null) {
                        fileReader.close()
                    }
                    if (br != null) {
                        br.close()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    decryStr = null
                }

            }
            return decryStr
        } else {
            return ""
        }

    }

}

