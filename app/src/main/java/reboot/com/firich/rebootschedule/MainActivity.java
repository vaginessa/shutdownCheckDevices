package reboot.com.firich.rebootschedule;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.net.wifi.WifiManager;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import firich.com.firichsdk.SerialPort;

public class MainActivity extends Activity {

    String strTagUtil = "Reboot.";
    String mLogFileName="";
    String g_configFileName="fec_device_test_config.xml";
    String g_configFolder = "/storage/emulated/legacy";
    boolean g_USBTestResult = false;
    boolean g_RS232TestResult = false; //
    boolean g_PingTestResult = false;

    // Test USB or Not..
    boolean g_TestUSB = false;
    boolean g_TestRS232 = false;
    boolean g_TestEthernet = false;

    //////////////////////Begin: Migrate Ethernet Test   //////////////////////////////////////////////////////////
    private Handler g_Handler_Ethernet = null; //Brian
    private String devIPaddress="124.108.105.150";
    //////////////////////End: Migrate Ethernet Test   //////////////////////////////////////////////////////////

    ////////////////////// Begin: Migrate RS232 Test  migratge //////////////////////////////////////////////////////////

    private String strRS232DeviceName ="";

    String[] strRS232DeviceList; // /dev/ttyUSB0|/dev/ttyUSB1|/dev/ttyUSB2|/dev/ttyUSB3
    textViewResultUtil_RS232 g_ltextViewResultIDs_RS232; //1, 2, 3, 4
    int g_RS232_deviceCount = 0;

    private String strTestString="testStringtestString";
    private Handler g_Handler_RS232 = null; //Brian
    ////////////////////////////////////////////////////////////////////////////////
    SerialPort sp;
    int intSerialPortHandle = -1;
    private int intBaudRate=9600;

    public SerialPort.SerialPortListener splistener = new SerialPort.SerialPortListener() {
        public void onDataReceive(byte[] btyData){
            Log.d("onDataReceive",new String(btyData));
        }
    };

    private void SleepMiniSecond(SerialPort spThread, int minSecond)
    {
        try {
            spThread.sleep(minSecond);
            dump_trace("SLEEP_MSEC="+ minSecond);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class textViewResultUtil_RS232{
        int[] textViewResultID= new int[20];
        public void setTextViewResultIDs(int deviceCount){

            //textViewResultID =  new int[deviceCount];
            for (int i=0; i < deviceCount; i++){
                textViewResultID[i] = generateViewId();
                dump_trace("textViewResultUtil_RS232:setTextViewResultIDs: textViewResultID="+ textViewResultID[i]);
            }
        }
        public int getTextViewResultID(int ResultIndex){
            int ResultID=-1;
            if (ResultIndex < 20)
                ResultID = textViewResultID[ResultIndex];
            return ResultID;
        }
        private final AtomicInteger sNextGeneratedId = new AtomicInteger(11); //for RS232

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
                int newValue = result + 1; //for RS232
                if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
                if (sNextGeneratedId.compareAndSet(result, newValue)) {
                    return result;
                }
            }
        }

    }

    ////////////////////// End: Migrate RS232 Test  migratge //////////////////////////////////////////////////////////
    //////////////////////USB Test  //////////////////////////////////////////////////////////
    /* "USB1;/mnt/media_rw/udisk|USB2;/mnt/media_rw/udisk_2|USB3;/mnt/media_rw/udisk_3|USB4;/mnt/media_rw/udisk_4|USB5;/mnt/media_rw/udisk_5|USB6;/mnt/media_rw/udisk_6|USB7;/mnt/media_rw/udisk_7|USB8;/mnt/media_rw/udisk_8"  */
    private String strUSBStorageDeviceName_android4_4 ="/storage/udisk|/storage/udisk_2|/storage/udisk_3|/storage/udisk_4|/storage/udisk_5|/storage/udisk_6|/storage/udisk_7|/storage/udisk_8";
    private String strUSBStorageDeviceName_android5_1 ="/storage/usbdisk|/storage/usbdisk2|/storage/usbdisk3|/storage/usbdisk4|/storage/usbdisk5|/storage/usbdisk6|/storage/usbdisk7|/storage/usbdisk8";

    private String strUSBStorageDeviceName =strUSBStorageDeviceName_android4_4;
    String[] strUSBStorageDeviceList;


    textViewResultUtil ltextViewResultIDs; //1, 2, 3, 4
    int deviceCount = 0;
    private Handler g_UITestResultHandler = null; //Brian
    private Handler g_UITestTimesHandler = null; //Brian

    private class textViewResultUtil{
        int[] textViewResultID= new int[20];
        public void setTextViewResultIDs(int deviceCount){

            //textViewResultID =  new int[deviceCount];
            for (int i=0; i < deviceCount; i++){
                textViewResultID[i] = generateViewId();
                dump_trace("setTextViewResultIDs: textViewResultID="+ textViewResultID[i]);
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

        this.g_UITestResultHandler = new Handler(); //Brian:
        this.g_UITestTimesHandler = new Handler();

        ////////////////////// Migrate Ethernet Test   //////////////////////////////////////////////////////////
        this.g_Handler_Ethernet = new Handler(); //Brian:

        ////////////////////// Migrate RS232 Test  migratge //////////////////////////////////////////////////////////
        this.g_Handler_RS232 = new Handler(); //Brian:

        //mLogFileName = get_log_file_name();
        dump_trace("MainActivity:onCreate:1");
        LoadSetConfigFile();

        InitUSBStorageTable(); // Create USB test layout table.
        InitRS232TestTable();   // Create RS232 test layout table
        InitEthernetTestTable();



        /*
        Intent RebootIntent = new Intent(this, BackgroundService.class);
        startService(RebootIntent);
        dump_trace("MainActivity:onCreate:startService:RebootIntent");
        */
    }

    boolean  g_bFromBootCompleted = false;
    @Override
    protected void onStart() {
        super.onStart();

        dump_trace("onStart:start");
        setTitle(" SN:" + Build.SERIAL);

        Intent intent = getIntent();
        g_bFromBootCompleted = intent.getBooleanExtra("FromBootCompleted", false);
        dump_trace("onStart:bFromBootCompleted"+ g_bFromBootCompleted);
        if (g_bFromBootCompleted)
        {
            //start 3min delay to test
            startUSBStorage_Test();
        }

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

        boolean b_ist_1st_time_run = get_flag_is_1st_time_run();
        if (b_ist_1st_time_run)
        {
            dump_trace("MainActivity:LoadConfigFile:b_ist_1st_time_run.=true");
            strLConfig = get_configFile_from_res_raw_xml();
            write_config_xml_to_storage(strLConfig); //write a default config xml file.
            dump_trace("res_raw_xml="+ strLConfig);

            Write_flag_1st_time_run_to_Load_res_raw_xml(false); //After 1st time run, set flag to false.
        }
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

    private void Write_flag_1st_time_run_to_Load_res_raw_xml(boolean Is1stTime)
    {
        SharedPreferences spref = getPreferences(MODE_PRIVATE);

        //由 SharedPreferences 中取出 Editor 物件，透過 Editor 物件將資料存入
        SharedPreferences.Editor editor = spref.edit();
        //清除 SharedPreferences 檔案中所有資料
        editor.clear();
        //儲存 boolean 型態的資料
        editor.putBoolean("KEY_FLAG_1ST_TIME_RUN", Is1stTime);
        //TODO: 可以將 log 檔名存起來.... ---------------
        //將目前對 SharedPreferences 的異動寫入檔案中
        //如果沒有呼叫 commit()，則異動的資料不會生效
        editor.commit();
    }
    private boolean get_flag_is_1st_time_run()
    {
        dump_trace("get_flag_is_1st_time_run:");
        SharedPreferences spref = getPreferences(MODE_PRIVATE);
        //回傳 KEY_STRING 是否在在 SharedPreferences 檔案中
        boolean exists = spref.contains("KEY_FLAG_1ST_TIME_RUN");
        if (exists){
            dump_trace("get_flag_is_1st_time_run:KEY_FLAG_1ST_TIME_RUN:exist..");
            //透過 KEY_BOOL key 取出 boolean 型態的資料，若資料不存在則回傳 true
            boolean bIs1stTimeRun = spref.getBoolean("KEY_FLAG_1ST_TIME_RUN", false);
            return bIs1stTimeRun;
        }else{
            dump_trace("get_flag_is_1st_time_run:KEY_FLAG_1ST_TIME_RUN:NOT exist!!so it is 1st time run..");
            return true;
        }
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
        InitRS232TestTable();   // Create RS232 test layout table
        InitEthernetTestTable();
    }
    public void Start_Test_click(View view)
    {
        dump_trace("MainActivity:onCreate:startService:Start_Test_click");

        //boolean testShutdown = false;
        Write_TestTimes_to_InternalStorage(0);
        /*
        if (testShutdown) {
            Intent RebootIntent = new Intent(this, BackgroundService.class);
            startService(RebootIntent);
        }
        */

        startUSBStorage_Test();

        //writeTestTimesToFile(this.getApplicationContext(), intTestTimes);


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
        g_TestUSB = devObject.Test;
        dump_trace("InitUSBStorageTable:g_TestUSB="+g_TestUSB);
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


    public void startUSBStorage_Test()
    {
        dump_trace("startUSBStorage_Test:Begin");

        USBStorageTestThread USBStorageTestThreadP = new USBStorageTestThread(strUSBStorageDeviceList, ltextViewResultIDs, deviceCount);
        USBStorageTestThreadP.start();
    }

    private class USBStorageTestThread extends Thread {
        String[] lstrUSBPath;
        textViewResultUtil ltextViewResultIDs;
        int ldeviceCount;
        USBStorageTestThread(String[] strUSBPath, textViewResultUtil textViewResultIDs, int deviceCount) {
            lstrUSBPath = strUSBPath;
            ltextViewResultIDs = textViewResultIDs;
            ldeviceCount = deviceCount;
        }

        public void run() {

            int TestTimes=0;
            TestTimes = get_TestTimes();
            UIUpdateTestTimes(TestTimes);
            try {
                //if (g_bFromBootCompleted) {
                 Thread.sleep(1 * 60 * 1000); // delay 3 min test.


                //Thread.sleep(3*60); // delay 3 min test.
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


            //Intent intent = getIntent();
            boolean testPASS = false;
            boolean[] testResult = new boolean[ldeviceCount];

            if (g_TestUSB) {
                for (int i = 1; i <= ldeviceCount; i++) {
                    testResult[i - 1] = USBStorage_Test(lstrUSBPath, ltextViewResultIDs.getTextViewResultID(i - 1), i, ldeviceCount);
                }

                int i = 0;
                testPASS = testResult[i];
                do {
                    testPASS = (testPASS && testResult[i]);
                    dump_trace("USB test result:" + testPASS);
                    i++;
                } while (i < ldeviceCount);
                g_USBTestResult = testPASS;
                dump_trace("USB Test total result:g_USBTestResult=" + g_USBTestResult);
                //write_Log_to_storage("USB Test total result="+ g_USBTestResult);
            }else{
                dump_trace("Do not test USB!");
                g_USBTestResult = true; //For final test result judgement.
            }

            /*
             if test PASS,
                write test times to storage
                shutdown
             else
                test FAIL stop ....test ....停在錯誤的畫面不要 shutdown.
             */
            //*** For Test shutdown
            //g_USBTestResult = true;
            //*** For Test shutdown

            //Write test result into global variable.
            /*
            if (g_USBTestResult) {

                dump_trace("Test Result:PASS");
                TestTimes = get_TestTimes();
                TestTimes++;
                Write_TestTimes_to_InternalStorage(TestTimes);
                UIUpdateTestTimes(TestTimes);
                dump_trace("Test Times:=" + TestTimes);
                //TODO: shutdown...
                shutdown_now();

            }else{
                dump_trace("Test Result USB:FAIL");
                // test FAIL, stop ....test ....停在錯誤的畫面不要 shutdown.
            }
            */

            startRS232_Loopback_Test();

/*
            if (testPASS){
                setResult(1, intent);
            }else{
                setResult(0, intent);
            }
            */

            //finish();
        }
    }

    private void shutdown_now()
    {
        dump_trace("shutdown_now:start");
        Intent i = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
        i.putExtra("android.intent.extra.KEY_CONFIRM",false);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        MainActivity.this.startActivity(i);
    }

    private void Write_TestTimes_to_InternalStorage(int nTestTimes)
    {
        SharedPreferences spref = getPreferences(MODE_PRIVATE);

        //由 SharedPreferences 中取出 Editor 物件，透過 Editor 物件將資料存入
        SharedPreferences.Editor editor = spref.edit();
        //清除 SharedPreferences 檔案中所有資料
        editor.clear();
        //儲存 boolean 型態的資料
        editor.putInt("KEY_TEST_TIMES", nTestTimes);
        //TODO: 可以將 log 檔名存起來.... ---------------
        //將目前對 SharedPreferences 的異動寫入檔案中
        //如果沒有呼叫 commit()，則異動的資料不會生效
        editor.commit();
    }
    private int get_TestTimes()
    {
        SharedPreferences spref = getPreferences(MODE_PRIVATE);
        //回傳 KEY_STRING 是否在在 SharedPreferences 檔案中
        boolean exists = spref.contains("KEY_TEST_TIMES");
        if (exists){
            //透過 KEY_BOOL key 取出 boolean 型態的資料，若資料不存在則回傳 true
            int nTestTimes = spref.getInt("KEY_TEST_TIMES", 0);
            return nTestTimes;
        }else{
            return 0;
        }
    }

    String g_TestTimesfilename = "Test-times";
    static private  int intTestTimes=0;
    void writeTestTimesToFile(Context context, String data)
    {
        //      String string = "Hello world!";
        FileOutputStream outputStream;

        try {
            outputStream = context.openFileOutput(g_TestTimesfilename, Context.MODE_PRIVATE);
            outputStream.write(data.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String readTestTimesFromFile() {

        String ret = "";

        try {
            InputStream inputStream = openFileInput(g_TestTimesfilename);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    private boolean USBStorage_Test(String[] strUSBPaths, int testDeviceTextViewID, int USBIndex, int deviceCount) {

        boolean testResult=false;
        int intDataReceivedLength=1;
        //TODO:

        CheckUSBStorageUtil CheckUSBStorageUtilVar = new CheckUSBStorageUtil(strUSBPaths, deviceCount);
        testResult = CheckUSBStorageUtilVar.checkUSBStorage(USBIndex);
        PostUIUpdateLog("", testResult, testDeviceTextViewID);
        try {
            Thread.sleep(600);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return testResult;

    }

    private void PostUIUpdateLog(final String msg, final  boolean testPASS, final int testDeviceTextViewID)
    {
        this.g_UITestResultHandler.post(new Runnable()
        {
            public void run()
            {
                //Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                final TextView textViewResultDeviceID = (TextView) findViewById(testDeviceTextViewID);
                dump_trace("PostUIUpdateLog:testPASS="+testPASS);
                if (testPASS)
                    textViewResultDeviceID.setText("PASS");
                else
                    textViewResultDeviceID.setText("FAIL");

            }
        });
    }

    private void UIUpdateTestTimes(final  int testTimes)
    {

        this.g_UITestTimesHandler.post(new Runnable()
        {
            public void run()
            {
                String result="";
                int nTestTimes=0;
                nTestTimes =testTimes;
                //Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                TextView textViewResultDeviceID = (TextView) findViewById(R.id.textViewTestTimesNum);
                dump_trace("UIUpdateTestTimes:testTimes="+nTestTimes);
                result = "" + nTestTimes;
                textViewResultDeviceID.setText(result);

            }
        });
    }
    //////////////////////////////////////////  USB Test ///////////////////////////////////

    ////////////////////// Begin: Migrate RS232 Test  migratge //////////////////////////////////////////////////////////
    private void CreateRS232DeviceTable(String[] strRS232DeviceList, int deviceCount)
    {
        Resources resource = (Resources) getBaseContext().getResources();
        ColorStateList colorWhile = (ColorStateList) resource.getColorStateList(R.color.white);
        ColorStateList colorBlack = (ColorStateList) resource.getColorStateList(R.color.black);
        TableLayout device_list_table_RS232 = (TableLayout) findViewById(R.id.device_list_table_RS232);
        //device_list_table_RS232.setStretchAllColumns(true);
        TableLayout.LayoutParams row_layout = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        TableRow.LayoutParams view_layout_device = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 5);
        TableRow.LayoutParams view_layout_result = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 5);


        g_ltextViewResultIDs_RS232 = new textViewResultUtil_RS232();
        g_ltextViewResultIDs_RS232.setTextViewResultIDs(deviceCount);

        Remove_USB_Table_Device_List(device_list_table_RS232); //Remove all rows first.

        for (int i=0; i< deviceCount; i++) {
            TableRow tr = new TableRow(MainActivity.this);
            tr.setLayoutParams(row_layout);
            tr.setBackgroundColor(resource.getColor(R.color.green));
            tr.setGravity(Gravity.CENTER_HORIZONTAL);

            String strRS232Device = strRS232DeviceList[i];
            if ((strRS232Device == null) || (strRS232Device.length() == 0))
                break;
            TextView device = new TextView(MainActivity.this);
            device.setText(strRS232Device);
            device.setTextColor(colorBlack);
            device.setBackgroundResource(R.color.white);
            device.setPadding(2, 2, 2, 2);
            device.setTextSize(16);
            view_layout_device.setMargins(2, 2, 2, 2);
            device.setLayoutParams(view_layout_device);

            TextView lookback_result = new TextView(MainActivity.this);
            lookback_result.setId(g_ltextViewResultIDs_RS232.getTextViewResultID(i));
            lookback_result.setText("??");
            lookback_result.setTextColor((ColorStateList) resource.getColorStateList(R.color.red));
            lookback_result.setBackgroundResource(R.color.white);
            lookback_result.setPadding(2, 2, 2, 2);
            lookback_result.setTextSize(16);
            view_layout_result.setMargins(2, 2, 2, 2);
            lookback_result.setLayoutParams(view_layout_result);

            tr.addView(device);
            tr.addView(lookback_result);
            device_list_table_RS232.addView(tr);
        }

    }
////////////////////// End: Migrate RS232 Test  migratge //////////////////////////////////////////////////////////
////////////////////// Begin: Migrate RS232 Test  migratge //////////////////////////////////////////////////////////
public void InitRS232TestTable()
{



    configUtil.Device devObject;
    configUtil configFile = new configUtil();

    configFile.dom4jXMLParser();
    //strSmartCardttyUSBPath
    devObject = configFile.getDevice("RS232Test");
    if (devObject.RS232DeviceName != null && !devObject.RS232DeviceName.isEmpty()) {
        strRS232DeviceName = devObject.RS232DeviceName;
    }

    int numberOfSeparator =0;
    for( int i=0; i<strRS232DeviceName.length(); i++ ) {
        if( strRS232DeviceName.charAt(i) == '|' ) {
            numberOfSeparator++;
        }
    }
    g_RS232_deviceCount = numberOfSeparator+1;
    strRS232DeviceList = strRS232DeviceName.split("\\|");
    if ((strRS232DeviceList == null) || (strRS232DeviceList.length == 0)) {
        return ;
    }
    g_TestRS232 = devObject.Test;
    dump_trace("InitRS232TestTable:g_TestRS232=" + g_TestRS232);
    CreateRS232DeviceTable(strRS232DeviceList, g_RS232_deviceCount);
}
////////////////////// End: Migrate RS232 Test  migratge //////////////////////////////////////////////////////////

    ////////////////////// Begin: Migrate RS232 Test  migratge //////////////////////////////////////////////////////////
    private class DeviceLoopbackTestThread extends Thread {
        String[] lstrttyUSBPath;
        textViewResultUtil_RS232 ltextViewResultIDs_RS232;
        int ldeviceCount;
        DeviceLoopbackTestThread(String[] strttyUSBPath, textViewResultUtil_RS232 textViewResultIDs, int deviceCount) {
            lstrttyUSBPath = strttyUSBPath;
            ltextViewResultIDs_RS232 = textViewResultIDs;
            ldeviceCount = deviceCount;
        }

        public void run() {
            Intent intent = getIntent();
            boolean testPASS = false;
            /*
            for (int i=0; i< ldeviceCount; i++) {
                testPASS = RS232_Test(lstrttyUSBPath[i], ltextViewResultIDs_RS232.getTextViewResultID(i));
            }
            */


            if (g_TestRS232) {
                /*
                int i=0;
                testPASS = RS232_Test(lstrttyUSBPath[i], ltextViewResultIDs_RS232.getTextViewResultID(i));
                do {
                    testPASS = (testPASS && RS232_Test(lstrttyUSBPath[i], ltextViewResultIDs_RS232.getTextViewResultID(i)));
                    dump_trace("USB test result:i=" + i + "=" + testPASS);
                    i++;
                } while (i < ldeviceCount);
                */
                g_RS232TestResult = true;
                for (int i=0; i< ldeviceCount; i++) {
                    testPASS = RS232_Test(lstrttyUSBPath[i], ltextViewResultIDs_RS232.getTextViewResultID(i));
                    if (!testPASS){
                        g_RS232TestResult = false; // if one of ttyUSBs fail, then RS232 test is fail.
                    }
                    dump_trace("RS232 test result:i=" + i + "=" + testPASS);

                }
                //g_RS232TestResult = testPASS;
            }else{
                dump_trace("test = false so not test RS232.");
                g_RS232TestResult = true; //For final result judgement.
            }

            Test_Ethernet();

            /*
            if (testPASS){
                setResult(1, intent);
            }else{
                setResult(0, intent);
            }

            try {
                Thread.sleep(1200);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            finish();
            */
        }
    }

    private void PostUIUpdateLog_RS232(final String msg, final  int intDataReceivedLength, final int testDeviceTextViewID)
    {
        this.g_Handler_RS232.post(new Runnable()
        {
            public void run()
            {
                //Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                final TextView textViewResultDeviceID = (TextView) findViewById(testDeviceTextViewID);
                dump_trace("PostUIUpdateLog_RS232:intDataReceivedLength="+intDataReceivedLength);
                if (intDataReceivedLength >0)
                    textViewResultDeviceID.setText("PASS");
                else
                    textViewResultDeviceID.setText("FAIL");

            }
        });
    }

    private boolean RS232_Test(String strttyUSBPath, int testDeviceTextViewID) {
        int intReturnCode = -1;
        // Open serial port
        sp = new SerialPort();

        dump_trace("RS232.test.start");
        dump_trace("strttyUSBPath:"+strttyUSBPath);

        intSerialPortHandle = sp.open(strttyUSBPath,intBaudRate);

        dump_trace("open NFC port: intSerialPortHandle="+intSerialPortHandle);

        byte[] btyVersion_msg_received = new byte[256];
        Arrays.fill( btyVersion_msg_received, (byte) 0 );
        int intDataReceivedLength=0;
        String strTestResult="";

        intReturnCode = sp.write(intSerialPortHandle,strTestString.getBytes());
        dump_trace("write: intReturnCode="+intReturnCode);
        sp.setListener(splistener);
        //SleepMiniSecond(sp, 1000);

        intDataReceivedLength = sp.getDataReceivedLength();
        int nRetry=0;
        while (intDataReceivedLength == 0)
        {
            SleepMiniSecond(sp, 1000);
            intDataReceivedLength = sp.getDataReceivedLength();
            nRetry++;
            if (nRetry == 2)
                break;
        }
        if ( intDataReceivedLength>= 0) {
            btyVersion_msg_received = Arrays.copyOf(sp.getBytDataReceived(),intDataReceivedLength);
        }

        PostUIUpdateLog_RS232("", intDataReceivedLength, testDeviceTextViewID);


        sp.close(intSerialPortHandle);
        sp = null;
        boolean testResult = false;
        testResult = (intDataReceivedLength > 0 )? true: false;
        return testResult;

    }

    public void startRS232_Loopback_Test()
    {
        dump_trace("startRS232_Loopback_Test: start");
        DeviceLoopbackTestThread DeviceLoopbackTestThreadP = new DeviceLoopbackTestThread(strRS232DeviceList, g_ltextViewResultIDs_RS232, g_RS232_deviceCount);
        DeviceLoopbackTestThreadP.start();
    }

////////////////////// End: Migrate RS232 Test  migratge //////////////////////////////////////////////////////////

    //////////////////////Begin: Migrate Ethernet Test   //////////////////////////////////////////////////////////
    TextView textViewPingPASS;
    public void InitEthernetTestTable()
    {



        configUtil.Device devObject;
        configUtil configFile = new configUtil();

        configFile.dom4jXMLParser();
        devObject = configFile.getDevice("Ethernet");
        g_TestEthernet = devObject.Test;
        dump_trace("InitEthernetTestTable: g_TestEthernet="+g_TestEthernet);
        if (devObject.Dev != null && !devObject.Dev.isEmpty()) {
            devIPaddress = devObject.Dev;
        }
        TextView editTextDevIPaddress = (TextView)findViewById(R.id.textViewDestIPaddress);
        editTextDevIPaddress.setText(devIPaddress);

        textViewPingPASS =  (TextView)findViewById(R.id.textViewPingResult);
        // Test_Ethernet();
        PostUIUpdateLog_Ethernet(textViewPingPASS,"??");
    }



    private void Test_Ethernet()
    {

        dump_trace("Start Test Ethernet:");
        PostUIUpdateLog_Ethernet(textViewPingPASS,"Testing...");
        // PingTestThread PingTestThreadP = new PingTestThread();
        //.PingTestThreadP.start();
        PingWithWIFICheckedThread PingWithWIFICheckedThreadP = new PingWithWIFICheckedThread(this);
        PingWithWIFICheckedThreadP.start();
    }

    /*
    private class PingTestThread extends Thread {
        PingTestThread() {
        }

        public void run() {
            // compute primes larger than minPrime
            boolean pingPASS = false;
            int retryTimes=0;
            do {
                pingPASS = executeCommand();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                retryTimes++;
            } while (!pingPASS && (retryTimes <5));
            String strResult="FAIL!";
            g_PingTestResult = pingPASS;
            if (pingPASS){
                strResult = "PASS";
            }
            PostUIUpdateLog_Ethernet(textViewPingPASS,strResult);



        }
    }
    */

    private void PostUIUpdateLog_Ethernet(final TextView textViewMsg, final String PostUIMsg)
    {
        this.g_Handler_Ethernet.post(new Runnable()
        {
            public void run()
            {
                dump_trace("PostUIMsg="+PostUIMsg);

                //textViewPingPASS.setBackgroundResource(R.color.red);
                //if (testPASS){
                    //textViewPingPASS.setBackgroundResource(R.color.green);
                //}
                textViewMsg.setText("Ping Test Result:      "+PostUIMsg);

            }
        });
    }

    private boolean executeCommand(){
        System.out.println("executeCommand");
        Runtime runtime = Runtime.getRuntime();
        try
        {
            String exStr = "/system/bin/ping -c 1 "+ devIPaddress;
            dump_trace("Exec cmd:" + exStr);
            Process  mIpAddrProcess = runtime.exec(exStr);
            int mExitValue = mIpAddrProcess.waitFor();
            System.out.println(" mExitValue "+mExitValue);
            if(mExitValue==0){
                return true;
            }else{
                return false;
            }
        }
        catch (InterruptedException ignore)
        {
            ignore.printStackTrace();
            System.out.println(" Exception:"+ignore);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.out.println(" Exception:"+e);
        }
        return false;
    }


    private class PingWithWIFICheckedThread extends Thread {
        Context context;
        WifiManager wimanager;
        PingWithWIFICheckedThread(Context contextL) {
            context = contextL;
            wimanager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        }
        private boolean enableWifi(Context context, boolean paramBoolean)
        {
            boolean OperateOK = false;
            wimanager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (wimanager != null) {
                OperateOK = wimanager.setWifiEnabled(paramBoolean);
            }
            return OperateOK;
        }
        boolean isWifiEnable()
        {
            boolean isWifiEnable = false;
            isWifiEnable = wimanager.isWifiEnabled();
            return isWifiEnable;
        }
        private String getMacAddress(Context context)
        {
            wimanager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            String macAddress = wimanager.getConnectionInfo().getMacAddress();
            return macAddress;
        }
        public void run() {

            if (g_TestEthernet) {
                // compute primes larger than minPrime
                boolean wifiEnableOK;
                boolean OperateWIFIOK = false;
                int retryTimes = 0;
                boolean initialWIFIOnOffStatus = false;
                wifiEnableOK = isWifiEnable();
                initialWIFIOnOffStatus = wifiEnableOK;
                if (wifiEnableOK) {
                    //disable wifi
                    do {
                        // bConnectOK = connecD10PrinterFunc();
                        if (!OperateWIFIOK) {
                            OperateWIFIOK = enableWifi(context, false);
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        wifiEnableOK = isWifiEnable();
                        retryTimes++;
                    } while (wifiEnableOK && (retryTimes < 5));
                }

                boolean pingPASS = false;
                retryTimes = 0;
                do {
                    pingPASS = executeCommand();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    retryTimes++;
                } while (!pingPASS && (retryTimes < 5));
                g_PingTestResult = pingPASS;
                String strResult = "FAIL!";
                if (pingPASS) {
                    strResult = "PASS";
                }
                PostUIUpdateLog_Ethernet(textViewPingPASS, strResult);

                //Restore WIFI on\off status
                OperateWIFIOK = false;
                if (initialWIFIOnOffStatus) {
                    //enable wifi
                    do {
                        // bConnectOK = connecD10PrinterFunc();
                        if (!OperateWIFIOK) {
                            OperateWIFIOK = enableWifi(context, true);
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        wifiEnableOK = isWifiEnable();
                        retryTimes++;
                    } while (!wifiEnableOK && (retryTimes < 5));
                }

            }else{
                dump_trace("Do not test Ethernet.");
                g_PingTestResult = true;
            }
            dump_trace("Final Test Result: g_USBTestResult="+ g_USBTestResult+", g_RS232TestResult="+ g_RS232TestResult
                    +",g_PingTestResult="+ g_PingTestResult);

            //g_USBTestResult = g_RS232TestResult = g_PingTestResult = true; //for test only..

            if (g_USBTestResult && g_RS232TestResult && g_PingTestResult) {
                dump_trace("Test Result, g_USBTestResult,g_RS232TestResult, g_PingTestResult :PASS");
                int TestTimes=0;
                TestTimes = get_TestTimes();
                TestTimes++;
                Write_TestTimes_to_InternalStorage(TestTimes);
                UIUpdateTestTimes(TestTimes);
                dump_trace("Test Times:=" + TestTimes);
                //TODO: shutdown...
                shutdown_now();
            }else{
                dump_trace("Final Test Result :FAIL");
            }

        }
    }

//////////////////////End: Migrate Ethernet Test   //////////////////////////////////////////////////////////


}
