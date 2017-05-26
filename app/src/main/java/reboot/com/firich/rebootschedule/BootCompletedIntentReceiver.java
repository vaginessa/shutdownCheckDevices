package reboot.com.firich.rebootschedule;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by brianyeh on 2016/11/9.
 */
public class BootCompletedIntentReceiver extends BroadcastReceiver {
    String strTagUtil = "Reboot.";
    private boolean bDebugOn = true;
    private void dump_trace( String bytTrace)
    {
        if (bDebugOn)
            Log.d(strTagUtil, bytTrace);
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            dump_trace("BootCompletedIntentReceiver:onReceive");
           // Intent pushIntent = new Intent(context, BackgroundService.class);
           // context.startService(pushIntent);


            Intent testIntent = new Intent(context, MainActivity.class);
            testIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            testIntent.putExtra("FromBootCompleted", true);
            context.startActivity(testIntent);
        }
    }
}
