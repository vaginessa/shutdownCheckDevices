package reboot.com.firich.rebootschedule;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

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

        EditText editTextFECConfig=(EditText)findViewById(R.id.EditTextFECConfig);

        editTextFECConfig.setText("<DeviceTest>\n" +
                "<Device name=\"USBStorage\"\n" +
                "test=\"true\"\n" +
                "numOfUSB=\"4\"\n" +
                "Android5_USBList=\"/storage/usbdisk|/storage/usbdisk2|/storage/usbdisk3|/storage/usbdisk4|/storage/usbdisk5|/storage/usbdisk6|/storage/usbdisk7|/storage/usbdisk8\"\n" +
                "></Device>\n" +
                "\n" +
                "<Device\n" +
                "name=\"RS232Test\"\n" +
                "test=\"true\"\n" +
                "devicename=\"/dev/ttyUSB100|/dev/ttyUSB101|/dev/ttyUSB102\"\n" +
                "></Device>\n" +
                "\n" +
                "<Device \n" +
                "name=\"Ethernet\"\n" +
                "test=\"true\"\n" +
                "dev=\"192.168.8.252\"\n" +
                "></Device>\n" +
                "</DeviceTest> ");

        /*
        Intent RebootIntent = new Intent(this, BackgroundService.class);
        startService(RebootIntent);
        dump_trace("MainActivity:onCreate:startService:RebootIntent");
        */
    }
}
