package reboot.com.firich.rebootschedule;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends Activity {

    String strTagUtil = "Reboot.";
    String mLogFileName="";
    String g_configFileName="fec_device_test_config.xml";
    String g_configFolder = "/storage/emulated/legacy";

    //////////////////////USB Test  //////////////////////////////////////////////////////////
    /* "USB1;/mnt/media_rw/udisk|USB2;/mnt/media_rw/udisk_2|USB3;/mnt/media_rw/udisk_3|USB4;/mnt/media_rw/udisk_4|USB5;/mnt/media_rw/udisk_5|USB6;/mnt/media_rw/udisk_6|USB7;/mnt/media_rw/udisk_7|USB8;/mnt/media_rw/udisk_8"  */
    private String strUSBStorageDeviceName_android4_4 ="/storage/udisk|/storage/udisk_2|/storage/udisk_3|/storage/udisk_4|/storage/udisk_5|/storage/udisk_6|/storage/udisk_7|/storage/udisk_8";
    private String strUSBStorageDeviceName_android5_1 ="/storage/usbdisk|/storage/usbdisk2|/storage/usbdisk3|/storage/usbdisk4|/storage/usbdisk5|/storage/usbdisk6|/storage/usbdisk7|/storage/usbdisk8";

    private String strUSBStorageDeviceName =strUSBStorageDeviceName_android4_4;
    String[] strUSBStorageDeviceList;


    textViewResultUtil ltextViewResultIDs; //1, 2, 3, 4
    int deviceCount = 0;
    private Handler mHandler = null; //Brian
    private class textViewResultUtil{
        int[] textViewResultID= new int[20];
        public void setTextViewResultIDs(int deviceCount){

            //textViewResultID =  new int[deviceCount];
            for (int i=0; i < deviceCount; i++){
                textViewResultID[i] = generateViewId();
            }
        }
        public int getTextViewResultID(int ResultIndex){
            int ResultID=-1;
            if (ResultIndex < 20)
                ResultID = textViewResultID[ResultIndex];
            return ResultID;
        }
        private final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

        /**
         * Generate a value suitable for use in @link setId(int)}.
         * This value will not collide with ID values generated at build time by aapt for R.id.
         *
         * @return a generated ID value
         */
        public int generateViewId() {
            for (;;) {
                final int result = sNextGeneratedId.get();
                // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
                int newValue = result + 1;
                if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
                if (sNextGeneratedId.compareAndSet(result, newValue)) {
                    return result;
                }
            }
        }
    }
    //////////////////////// USB Test ////////////////////////////////////////////////////////


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
        LoadSetConfigFile();

        InitUSBStorageTable(); // Create USB test layout table.




        /*
        Intent RebootIntent = new Intent(this, BackgroundService.class);
        startService(RebootIntent);
        dump_trace("MainActivity:onCreate:startService:RebootIntent");
        */
    }

    /*
     * Load file content to String
     */
    public static String loadFileAsString(String filePath) throws java.io.IOException{
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
    }

    void LoadSetConfigFile()
    {
   /*
        LoadConfigFile:
        If config file exist in /storage/emulated/legacy/fec_device_test_config.xml then
            read this config file
        else
            read default config file res/raw/default_device_config.xml

         */
        dump_trace("MainActivity:LoadConfigFile");

        //File dir = new File ("/storage/emulated/legacy");

        File dir = new File (g_configFolder);
        String strLConfig ="";

        try {
            File fillFilePath = new File(dir, g_configFileName);
            if (fillFilePath.exists()){
                dump_trace("MainActivity:LoadConfigFile:fillFilePath.exists=true");
                strLConfig = loadFileAsString(fillFilePath.getAbsolutePath());
                dump_trace("file:"+ fillFilePath.getAbsolutePath()+"\n"+strLConfig);
            }else{
                dump_trace("MainActivity:LoadConfigFile:fillFilePath.exists=false");
                strLConfig = get_configFile_from_res_raw_xml();
                write_config_xml_to_storage(strLConfig); //write a default config xml file.
                dump_trace("res_raw_xml="+ strLConfig);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        EditText editTextFECConfig=(EditText)findViewById(R.id.EditTextFECConfig);
        editTextFECConfig.setText(strLConfig);
    }
    String get_configFile_from_res_raw_xml()
    {

        InputStream defaultConfigIS = this.getResources().openRawResource(R.raw.default_device_config);

        dump_trace("MainActivity:get_configFile_from_res_raw_xml");
        String strDefaultConfig="";
        strDefaultConfig = convertInputStringToString(defaultConfigIS);
        dump_trace("get_configFile_from_res_raw_xml:default config:\n"+ strDefaultConfig);
        return strDefaultConfig;

    }
    String convertInputStringToString(InputStream inputStream)
    {
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
        InitUSBStorageTable();
    }
    public void Start_Test_click(View view)
    {

        Intent RebootIntent = new Intent(this, BackgroundService.class);
        startService(RebootIntent);
        dump_trace("MainActivity:onCreate:startService:Start_Test_click");

    }


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


        File dir = new File (g_configFolder);
        String strLConfig ="";
        strLConfig = strConfig;

        try {
            File fillFilePath = new File(dir, g_configFileName);
            //secondFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(fillFilePath, false); //false for overwrite not append.
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

    //////////////////////////////////////////  USB Test ///////////////////////////////////
    void Remove_USB_Table_Device_List(TableLayout device_list_table)
    {
        dump_trace("Remove_USB_Table_Device_List:");
        int count = device_list_table.getChildCount();
        dump_trace("Remove_USB_Table_Device_List:row count="+ count);
        for (int i = 0; i < count; i++) {
            View child = device_list_table.getChildAt(i);
            if (child instanceof TableRow) ((ViewGroup) child).removeAllViews();
        }
    }
    private void CreateUSBStorageDeviceTable(String[] strUSBStorageDeviceList, int deviceCount)
    {
        dump_trace("CreateUSBStorageDeviceTable start:");
        Resources resource = (Resources) getBaseContext().getResources();
        ColorStateList colorWhile = (ColorStateList) resource.getColorStateList(R.color.white);
        ColorStateList colorBlack = (ColorStateList) resource.getColorStateList(R.color.black);
        TableLayout device_list_table = (TableLayout) findViewById(R.id.device_list_table);
        //device_list_table.setStretchAllColumns(true);
        TableLayout.LayoutParams row_layout = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        TableRow.LayoutParams view_layout_device = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 5);
        TableRow.LayoutParams view_layout_result = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 5);


        ltextViewResultIDs = new textViewResultUtil();
        ltextViewResultIDs.setTextViewResultIDs(deviceCount);

        Remove_USB_Table_Device_List(device_list_table); //Remove all rows first.
        for (int i=0; i< deviceCount; i++) {
            TableRow tr = new TableRow(MainActivity.this);
            tr.setLayoutParams(row_layout);
            tr.setBackgroundColor(resource.getColor(R.color.green));
            tr.setGravity(Gravity.CENTER_HORIZONTAL);

            // String strUSBStorageDevice = strUSBStorageDeviceList[i];
            String strUSBStorageDevice = "USB "+ String.valueOf(i+1);
            if ((strUSBStorageDevice == null) || (strUSBStorageDevice.length() == 0))
                break;
            TextView device = new TextView(MainActivity.this);
            device.setText(strUSBStorageDevice);
            device.setTextColor(colorBlack);
            device.setBackgroundResource(R.color.white);
            device.setPadding(2, 2, 2, 2);
            device.setTextSize(16);
            view_layout_device.setMargins(2, 2, 2, 2);
            device.setLayoutParams(view_layout_device);

            TextView lookback_result = new TextView(MainActivity.this);
            lookback_result.setId(ltextViewResultIDs.getTextViewResultID(i));
            lookback_result.setText("??");
            lookback_result.setTextColor((ColorStateList) resource.getColorStateList(R.color.red));
            lookback_result.setBackgroundResource(R.color.white);
            lookback_result.setPadding(2, 2, 2, 2);
            lookback_result.setTextSize(16);
            view_layout_result.setMargins(2, 2, 2, 2);
            lookback_result.setLayoutParams(view_layout_result);

            tr.addView(device);
            tr.addView(lookback_result);
            device_list_table.addView(tr);
        }

    }

    public void InitUSBStorageTable()
    {

        String strVersion = Build.DISPLAY;
        boolean contains_android4 = strVersion.contains("4.4.3 2.0.0-rc2.");
        boolean contains_android5 = strVersion.contains("Edelweiss-T 5.1");
        boolean contains_android5_D = strVersion.contains("Edelweiss-D 5.1");

        configUtil.Device devObject;
        configUtil configFile = new configUtil();

        configFile.dom4jXMLParser();
        devObject = configFile.getDevice("USBStorage");
        deviceCount = devObject.numOfUSB;
        if (devObject.numOfUSB ==0) {
            deviceCount =8;
        }

        if (devObject.Android4_USBList != null && !devObject.Android4_USBList.isEmpty()) {
            strUSBStorageDeviceName = devObject.Android4_USBList;
        }
        if (! contains_android4){
            if (devObject.Android5_USBList != null && !devObject.Android5_USBList.isEmpty()) {
                strUSBStorageDeviceName = devObject.Android5_USBList;
            }
        }
        strUSBStorageDeviceList = strUSBStorageDeviceName.split("\\|");
        if ((strUSBStorageDeviceList == null) || (strUSBStorageDeviceList.length == 0)) {
            return ;
        }
        // deviceCount = 8; //usbdisk ~ usbdisk8
        CreateUSBStorageDeviceTable(strUSBStorageDeviceList, deviceCount);
    }
    //////////////////////////////////////////  USB Test ///////////////////////////////////
}
