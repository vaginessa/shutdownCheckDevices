package reboot.com.firich.rebootschedule;

import android.util.Log;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.StringWriter;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by brianyeh on 2016/7/28.
 */
public class configUtil {

    private boolean bDebugOn = true;
    String strTagUtil = "configUtil.";
/*
    private String SysKingICCard_dev="/dev/ttyUSB4";
    private String LCM_dev="/dev/ttyUSB2";
    private String NFC_dev="/dev/ttyUSB4";
*/
    private String g_configFileName="fec_device_test_config.xml";
    private String g_configFolder = "/storage/emulated/legacy";

    //private String fectest_config_path = "/data/fec_config/fectest_config.xml";
    private String fectest_config_path = g_configFolder + "/" +g_configFileName ;
    class Device{
    /*
    name="RS232Test"
    devicename="/dev/ttyUSB0|/dev/ttyUSB1|/dev/ttyUSB2|/dev/ttyUSB3|/dev/ttyUSB4"
     */
        public String Name;  //"RS232Test"
        public boolean Test; //test="true" ; test="false"
        public String RS232DeviceName; //devicename="/dev/ttyUSB0|/dev/ttyUSB1|/dev/ttyUSB2|/dev/ttyUSB3|/dev/ttyUSB4"
        public String Dev;  //"/dev/ttyUSB5"
    /*
    path1="/data/fec/1.jpg"
    path2="/data/fec/2.jpg"
    path3="/data/fec/Receipt.txt"
    */
        public String Path1;
        public String Path2;
        public String Path3;
        public int BaudRate;
        public int numOfUSB; //for USBStorage test.
        public String Android4_USBList;
        public String Android5_USBList;
    }
    Device DevObject;
    Hashtable<String, Device> hashtableConfig;


    public configUtil()
    {
        hashtableConfig = new Hashtable<String, Device>();
    }
    public configUtil(String configPath)
    {
        hashtableConfig = new Hashtable<String, Device>();
        fectest_config_path = configPath;
    }
    private void dump_trace( String bytTrace)
    {
        if (bDebugOn)
            Log.d(strTagUtil, bytTrace);
    }
    public Device getDevice(String strName)
    {
        Device devObject= new Device();

        boolean IsExist = false;
        IsExist = hashtableConfig.containsKey(strName);
        if (IsExist){
            devObject = hashtableConfig.get(strName);
        }
        return  devObject;
    }
    public void dom4jXMLParser()
    {
        String strBaudRate="";
        StringWriter xmlWriter = new StringWriter();
        SAXReader reader = new SAXReader();
        File file = new File(fectest_config_path);

        try {

            Document document = reader.read(file);
            Element root = document.getRootElement();
            List<Element> childElements = root.elements();
            for (Element child : childElements) {
                //已知属性名情况下
                dump_trace("name: " + child.attributeValue("name"));
                dump_trace("dev: " + child.attributeValue("dev"));
                DevObject = new Device();
                DevObject.Name = child.attributeValue("name");
                DevObject.Test = Boolean.parseBoolean(child.attributeValue("test"));

                if ("ThermalPrinterTest".equals(child.attributeValue("name"))){

                    DevObject.Path1 = child.attributeValue("path1");
                    DevObject.Path2 = child.attributeValue("path2");
                    hashtableConfig.put(child.attributeValue("name"), DevObject);
                }else if ("ThermalPrinterTestD10".equals(child.attributeValue("name"))){

                    DevObject.Path1 = child.attributeValue("path1");
                    DevObject.Path2 = child.attributeValue("path2");
                    DevObject.Path3 = child.attributeValue("path3");
                    hashtableConfig.put(child.attributeValue("name"), DevObject);
                }else if ("RS232Test".equals(child.attributeValue("name"))){

                    DevObject.RS232DeviceName = child.attributeValue("devicename");
                    dump_trace("RS232DeviceName: " + DevObject.RS232DeviceName);
                    hashtableConfig.put(child.attributeValue("name"), DevObject);
                }else if ("USBStorage".equals(child.attributeValue("name"))){

                    DevObject.numOfUSB = Integer.valueOf(child.attributeValue("numOfUSB"));
                    DevObject.Android4_USBList = child.attributeValue("Android4_USBList");
                    DevObject.Android5_USBList = child.attributeValue("Android5_USBList");
                    dump_trace("numOfUSB: " + DevObject.numOfUSB);
                    hashtableConfig.put(child.attributeValue("name"), DevObject);
                }
                else{

                    DevObject.Dev = child.attributeValue("dev");
                    strBaudRate = child.attributeValue("baud_rate");
                    if ( strBaudRate!= null  && !strBaudRate.isEmpty()) {
                        DevObject.BaudRate = Integer.valueOf(strBaudRate);
                    }
                    hashtableConfig.put(child.attributeValue("name"), DevObject);
                }
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
