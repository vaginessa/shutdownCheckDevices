package reboot.com.firich.rebootschedule;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
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

        mLogFileName = get_log_file_name();
        dump_trace("MainActivity:onCreate:1");

        EditText editTextFECConfig=(EditText)findViewById(R.id.EditTextFECConfig);

        InputStream defaultConfigIS = this.getResources().openRawResource(R.raw.default_device_config);

        dump_trace("MainActivity:onCreate:2");
        String strDefaultConfig="";
        strDefaultConfig = convertInputStringToString(defaultConfigIS);
        dump_trace("default config:"+ strDefaultConfig);
        editTextFECConfig.setText(strDefaultConfig);



        /*
        Intent RebootIntent = new Intent(this, BackgroundService.class);
        startService(RebootIntent);
        dump_trace("MainActivity:onCreate:startService:RebootIntent");
        */
    }

    String convertInputStringToString(InputStream inputStream) {
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        String strConfig="";
        try {
            int result = bis.read();
            while (result != -1) {
                buf.write((byte) result);
                result = bis.read();
            }
            strConfig = buf.toString("UTF-8");

        } catch (Exception e) {
            dump_trace(e.toString());
        }
// StandardCharsets.UTF_8.name() > JDK 7
        return strConfig;
    }

    public void Set_Device_Test_Config_click(View view)
    {
        EditText editTextFECConfig=(EditText)findViewById(R.id.EditTextFECConfig);
        write_config_xml_to_storage(editTextFECConfig.getText().toString());
    }
    public void Start_Test_click(View view)
    {

        Intent RebootIntent = new Intent(this, BackgroundService.class);
        startService(RebootIntent);
        dump_trace("MainActivity:onCreate:startService:Start_Test_click");

    }

    String mLogFileName="";

    String get_log_file_name()
    {
        //Initialize your Date however you like it.
        Date newdate = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
        String date = "FEC_ShutdownTestDevice_Log_"+ Build.SERIAL +"_" + format.format(newdate)+".txt";;
        return date;
    }

    public void write_config_xml_to_storage(String strConfig)
    {
        dump_trace("MainActivity:write_config_xml_to_storage");
        dump_trace("Config file:\n"+strConfig );

        String configFileName="fec_device_test_config.xml";
        File dir = new File ("/storage/emulated/legacy");
        String strLConfig ="";
        strLConfig = strConfig;

        try {
            File fillFilePath = new File(dir, configFileName);
            //secondFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(fillFilePath, true);
            fos.write(strLConfig.getBytes());
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void write_Log_to_storage(String log)
    {
       // String LogFileName="BatteryLog.txt";
        if (mLogFileName.isEmpty()){
            return;
        }
        File dir = new File ("/storage/emulated/legacy");
        String LogString ="";
        LogString = log;
        String strVersion = Build.DISPLAY;
        boolean contains_androidHTC = strVersion.contains("MRA58K");
        if (contains_androidHTC){
            /*
            Android added new permission model for Android 6.0 (Marshmallow).

http://www.captechconsulting.com/blogs/runtime-permissions-best-practices-and-how-to-gracefully-handle-permission-removal

             */
            return;
            /*
            java.io.File file = new java.io.File( Environment.getExternalStorageDirectory().getAbsolutePath()+"/"
                    + mLogFileName);

            try {
                //File secondFile = new File(dir, mLogFileName);
                //file.createNewFile();
                FileOutputStream fos = new FileOutputStream(file, true);
                fos.write(LogString.getBytes());
                fos.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
            */

        }
        try {
            File secondFile = new File(dir, mLogFileName);
            //secondFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(secondFile, true);
            fos.write(LogString.getBytes());
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
