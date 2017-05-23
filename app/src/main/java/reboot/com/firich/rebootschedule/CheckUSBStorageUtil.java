
package reboot.com.firich.rebootschedule;

import android.os.Build;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by brianyeh on 2016/9/20.
 */
public class CheckUSBStorageUtil {
    boolean findStorageFeclogFileFlag = false;
    int     findUSBIndex =1;

    String path = "/mnt/media_rw/usbdisk/1.txt";
    String[] usbList;
    int USBCount =8;
    String FECLogString = "FEC Storage test complete.";

    public CheckUSBStorageUtil(String[] logPath, int usb_count)
    {
        usbList = logPath;
        USBCount = usb_count;
    }

    public CheckUSBStorageUtil(int usbCount)
    {
        USBCount = usbCount;
    }
    public CheckUSBStorageUtil()
    {
    }
    public boolean isFindStorageFECLogFile()
    {
        return findStorageFeclogFileFlag;
    }
    public int getFoundUSBIndex()
    {
        return findUSBIndex;
    }
    public boolean checkUSBStorage(int checkUSBNum)
    {
        // check feclog file exist?
        //String strStorage="/mnt/media_rw";
        String strStorage="/storage/";
        String strfeclog = "1.txt";
        String strUDisk4_4 = "/udisk";
        String strUSBDisk5_1 = "/usbdisk";
        String strDiskType=strUDisk4_4;
        boolean findFeclogFile = false;
        File logFile;

        boolean checkFileOK=false;


        String strVersion = Build.DISPLAY;
        boolean contains_android4 = strVersion.contains("4.4.3 2.0.0-rc2.");
        boolean contains_android5 = strVersion.contains("Edelweiss-T 5.1");
        boolean contains_android5_D = strVersion.contains("Edelweiss-D 5.1");

        if (contains_android5|| contains_android5_D ){
            strDiskType = strUSBDisk5_1;
        }



        //int retry=0;
        String strNum="";
        String strDiskNum="";
        for (int i=1; i <= USBCount; i++) {//search from usbdisk to usbdisk_8
            /*
            strNum = Integer.toString(i);
            if (i == 1){
                strDiskNum = strDiskType +"/" + checkUSBNum + ".txt"; //  /usbdisk/1.txt
            }else{
                if (contains_android5|| contains_android5_D ){
                    strDiskNum = strDiskType + strNum + "/" + checkUSBNum + ".txt"; //  /usbdisk2/2.txt
                }else {
                    strDiskNum = strDiskType + "_" + strNum + "/" + checkUSBNum + ".txt"; //  /udisk_2/2.txt
                }
            }
            path = strStorage + strDiskNum;
            */
            path =usbList[i-1]+"/"+  checkUSBNum + ".txt";
            logFile = new File(path);

           // do {
                if (logFile.exists()) {
                    findFeclogFile = true;
                    findStorageFeclogFileFlag = true;
                    findUSBIndex = i;
                    //check write, read file ok?
                    try {
                        //BufferedWriter for performance, true to set append to file flag
                        BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, false)); //false means: not append.
                        buf.write(FECLogString);
                        buf.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    String readLogFile="";
                    readLogFile = readStorageLog();
                    checkFileOK = readLogFile.contains(FECLogString);

                    break;
                }
            /*
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                */
           //     retry++;
           // } while (retry < 3);
           // retry=0;
    }
    return checkFileOK;
}

    public String readStorageLog() {
        //Find the directory for the SD Card using the API
//*Don't* hardcode "/sdcard"
        // File sdcard = Environment.getExternalStorageDirectory();

//Get the text file
        //  File file = new File(sdcard,"file.txt");
        File logFile;
        logFile = new File(path);

//Read text from file
        StringBuilder text = new StringBuilder();

        String line="";

        try {
            BufferedReader br = new BufferedReader(new FileReader(logFile));

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
        }
        return text.toString();
    }
}

