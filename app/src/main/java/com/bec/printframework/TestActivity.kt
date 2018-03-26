package com.bec.printframework

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.bec.printframework.print.utils.PrintUtils
import kotlinx.android.synthetic.main.activity_test.*

class TestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        PrintUtils.getInstance().initPrintConnect(this)
        text.setOnClickListener {
            PrintUtils.getInstance().initializePrinter()
                    .addCode("0123456789012345678922",PrintUtils.Companion.Orientation.CENTRE)
                    .addCutPaper()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        PrintUtils.getInstance().exitPrint()
    }
}
