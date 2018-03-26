# MyPrintFramework
GP和XP打印机
[![](https://jitpack.io/v/XZWQuickness/PrintFrameworks.svg)](https://jitpack.io/#XZWQuickness/PrintFrameworks)

![MyFramework Library](http://upload.ouliu.net/i/2018031610372928ztm.jpeg)
 
 ## 如何使用它
 
Step 1. 然后在 build.gradle(Module:app) 的 dependencies 添加:

	dependencies {
	      compile 'com.github.XZWQuickness:PrintFrameworks:1.0.4'
	}


Step 2. ## 使用方法
 
 1 连接打印机              PrintUtils.getInstance().initPrintConnect(this)
 
 2 初始化打印机清除缓存     initializePrinter()
 
 3 打印文字倍高倍宽            setFont(Font)
 
 4 打印间距                printAndFeed(int)
 
 5 打印内容                addText(String,Orientation)
 
 6 打印内容                addText(String,String,String)
 
 7 打印条形码              addCode(String,Orientation)
 
 8 打印二维码              addQRCode(String,Orientation)
 
 9 开启钱箱                openCashBox()
 
 10 打印并切纸              addCutPaper
