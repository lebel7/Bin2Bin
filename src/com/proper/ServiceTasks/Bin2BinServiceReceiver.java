package com.proper.ServiceTasks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.android.barcode.Bin2BinService;

/**
 * Created by Lebel on 02/04/2014.
 */
public class Bin2BinServiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, Bin2BinService.class);
        context.startService(i);
    }
}
