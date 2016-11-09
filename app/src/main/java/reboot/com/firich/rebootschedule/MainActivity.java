package reboot.com.firich.rebootschedule;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {

    String strTagUtil = "Reboot.";
    private boolean bDebugOn = true;
    private void dump_trace( String bytTrace)
    {
        if (bDebugOn)
            Log.d(strTagUtil, bytTrace);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reboot);

        dump_trace("MainActivity:onCreate");
        Intent RebootIntent = new Intent(this, BackgroundService.class);
        startService(RebootIntent);
        dump_trace("MainActivity:onCreate:startService:RebootIntent");
    }
}
