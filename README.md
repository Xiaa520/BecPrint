# MyPrintFramework
GP和XP打印机
[![](https://jitpack.io/v/XZWQuickness/BecPrint.svg)](https://jitpack.io/#XZWQuickness/BecPrint)

![MyFramework Library](http://upload.ouliu.net/i/2018031610372928ztm.jpeg)
 
 ## 如何使用它
 
Step 1. 然后在 build.gradle(Module:app) 的 dependencies 添加:

	dependencies {
	     compile 'com.github.XZWQuickness:BecPrint:1.0.5'
	}


Step 2. ## 使用方法

 
 1 连接打印机              PrintUtils.getInstance().initPrintConnect(this)
 
 2 初始化打印机清除缓存     initializePrinter()
 
 3 打印文字倍高倍宽         setFont(Font)
 
 4 打印间距                printAndFeed(int)
 
 5 打印内容                addText(String,Orientation)
 
 6 打印内容                addText(String,String,String)
 
 7 打印条形码              addCode(String,Orientation)
 
 8 打印二维码              addQRCode(String,Orientation)
 
 9 开启钱箱                openCashBox()
 
 10 打印并切纸             addCutPaper()

 11 退出打印服务           exitPrint()
 
 12 获取打印对象           getPrintObject(mIPrintObject:IPrintObject)
 
 13 打印机是否连接         ISCONNECT
 
 14 机器是否经过授权       IS_ACTIVITE
 
 15 机器设备码            MACHINE_NUMBER
 
 16 1:零售版1代机器，2：加油站版，3：零售版2代机器 SOFT_TYPE
 
 
防止support包冲突
```
    configurations.all {
         resolutionStrategy.eachDependency { DependencyResolveDetails details ->
             def requested = details.requested
            if (requested.group == 'com.android.support') {
                if (!requested.name.startsWith("multidex")) {
                    details.useVersion '26.0.0-alpha1'//此处的版本号可以替换
                }
            }
        }
    }
```
 
