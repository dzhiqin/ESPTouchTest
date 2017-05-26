package com.example.esptouchtest;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/4/3.
 */

public class WifiHelper {

    private ArrayList<ScanResult> scanResultList;
    private Context context;
    private WifiManager wifiManager;
    public WifiHelper(Context context){
        this.context=context;
        wifiManager=(WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        if(!wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(true);
        }
        //scanResultList=new ArrayList<>();
    }
    public List<ScanResult> getScanResult(){
        wifiManager.startScan();
        List<ScanResult> scanResultsList=wifiManager.getScanResults();
        return scanResultsList;
    }
    public WifiInfo getWifiInfo(){
        return wifiManager.getConnectionInfo();
    }


    private static byte[] intToByte(int i){
        byte[] ipByte=new byte[4];
        ipByte[0]=(byte)(i&0xff);
        ipByte[1]=(byte)((i>>8)&0xff);
        ipByte[2]=(byte)((i>>16)&0xff);
        ipByte[3]=(byte)((i>>24)&0xff);
        return ipByte;
    }
    private static String intToIP(int i){
        return (i&0xff)+"."+((i>>8)&0xff)+"."+((i>>16)&0xff)+"."+((i>>24)&0xff);
    }
    public ArrayList<String> getWifiSSIDList(){
        scanResultList=(ArrayList)getScanResult();
        ArrayList<String> wifiSSIDList=new ArrayList<>();

        for(ScanResult result:scanResultList){
            //Log.v("test",result.toString());
        }
        wifiSSIDList.clear();
        for(int i=0;i<scanResultList.size();i++){
            ScanResult result=scanResultList.get(i);
            if(!result.SSID.isEmpty()){
                String wifiStr=result.SSID;
                if(!wifiSSIDList.contains(wifiStr)){
                    //a new wifi SSID
                    //put the wifistr into wifiSSIDList
                    wifiSSIDList.add(wifiStr);
                }
            }
        }
        //Log.v("test","wifiHashMap:"+wifiHashMap.toString());
        return wifiSSIDList;
    }
}
