package com.bec.printframework

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.bec.printframework.print.utils.PrintUtils
import kotlinx.android.synthetic.main.activity_test.*

class TestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        text.setOnClickListener {
            PrintUtils.getInstance().initializePrinter(this)
                    .setFont(PrintUtils.Font.BIG)
                    .addText("南京亚青体育产业发展有限公司", PrintUtils.Orientation.CENTRE)
                    .setFont(PrintUtils.Font.LITTLE)
                    .addText("时间：2018-03-26 16:17:20", PrintUtils.Orientation.LEFT)
                    .addCode("2018032616172600175295", PrintUtils.Orientation.CENTRE)
                    .addText("收银员：朱国治", PrintUtils.Orientation.LEFT)
                    .addText("商品名称", "数量", "金额", "单价",true)
                    .addText("商品名称", "数量", "金额", false)
                    .addText("", PrintUtils.Orientation.LEFT)
                    .addText("----------------------------------------------", PrintUtils.Orientation.LEFT)
                    .addText("得力0018回形针", "1", "2.00", true)
                    .addText("牛奶", "2", "36.5", false)
                    .printAndFeed(1)
                    .addText("----------------------------------------------", PrintUtils.Orientation.LEFT)
                    .addText("原价合计:2.00", PrintUtils.Orientation.LEFT)
                    .addText("原价合计:2.00", PrintUtils.Orientation.LEFT)
                    .addText("原价合计:2.00", PrintUtils.Orientation.LEFT)
                    .addText("原价合计:2.00", PrintUtils.Orientation.LEFT)
                    .addText("原价合计:2.00", PrintUtils.Orientation.LEFT)
                    .addText("原价合计:2.00", PrintUtils.Orientation.LEFT)
                    .addText("原价合计:2.00", PrintUtils.Orientation.LEFT)
                    .addText("原价合计:2.00", PrintUtils.Orientation.LEFT)
                    .addText("原价合计:2.00", PrintUtils.Orientation.LEFT)
                    .addText("原价合计:2.00", PrintUtils.Orientation.LEFT)
                    .addText("原价合计:2.00", PrintUtils.Orientation.LEFT)
                    .addText("----------------------------------------------", PrintUtils.Orientation.LEFT)
                    .printAndFeed(1)
                    .addText("欢迎光临！\n\n技术支持：江苏银企通支付技术有限公司\n400-86-96019\n", PrintUtils.Orientation.CENTRE)
                    .addCutPaper()
        }

        textView.setOnClickListener {
            PrintUtils.getInstance().exitPrint()
        }


    }


    override fun onDestroy() {
        super.onDestroy()
        PrintUtils.getInstance().exitPrint()
    }
}
