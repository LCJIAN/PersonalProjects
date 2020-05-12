package com.lcjian.vastplayer.android.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.lcjian.vastplayer.android.service.MediaScannerService;

/**
 * 文件扫描
 */
public class MediaScannerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            context.startService(new Intent(context, MediaScannerService.class));
        } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
            Uri uri = intent.getData();
            if (uri != null && uri.getScheme().equals("file")) {
                scanDirectory(context, uri.getPath());
            }
        }
    }

    /**
     * 扫描文件夹
     */
    private void scanDirectory(Context context, String path) {
        Bundle args = new Bundle();
        args.putString(MediaScannerService.EXTRA_DIRECTORY, path);
        context.startService(new Intent(context, MediaScannerService.class).putExtras(args));
    }
}
