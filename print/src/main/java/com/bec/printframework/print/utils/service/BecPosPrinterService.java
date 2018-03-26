package com.bec.printframework.print.utils.service;

import android.app.Notification;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;


import com.bec.printframework.print.R;

import net.posprinter.service.PosprinterService;

/**
 * Created by lzp on 2018/3/8 0008.
 */

public class BecPosPrinterService extends PosprinterService {

    @Override
    public IBinder onBind(Intent intent) {

        Notification.Builder builder = new Notification.Builder(getApplicationContext());

        builder.setShowWhen(false)
                .setAutoCancel(false)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ico))
                .setContentTitle("拜客™新零售")
                .setSmallIcon(R.drawable.ic_printshop)
                .setContentText("XP打印机服务")
                .setWhen(System.currentTimeMillis());

        Notification notification = builder.build();

        notification.defaults = Notification.DEFAULT_SOUND;

        startForeground(110, notification);

        return super.onBind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }
}
