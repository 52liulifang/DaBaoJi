package com.example.cc.makepackage.util;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

public class StringUtil {
    //byte转16进制字串
    public static String bytesToHexString(byte[] bytes){
        String result="";
        for (int i=0;i<bytes.length;i++){
            String hexString = Integer.toHexString(bytes[i] & 0xFF);
            if (hexString.length() == 1) {
                hexString = '0' + hexString;
            }
            result += hexString.toUpperCase();
        }
        return result;
    }
    //字串转int数组，返回16个
    public static int[] string2int(String s,int[] array){
        try{
            for (int i=1;i<=44;i++){
                String temp=s.substring(10+(i-1)*2,2*i+10);
                array[i-1]=Integer.valueOf(temp,16);
            }
        }catch (ArrayIndexOutOfBoundsException e){
            e.printStackTrace();
        }
        return array;
    }
    /**
     * 方法描述：判断某一Service是否正在运行
     *
     * @param context     上下文
     * @param serviceName Service的全路径： 包名 + service的类名
     * @return true 表示正在运行，false 表示没有运行
     */
    public static boolean isServiceRunning(Context context, String serviceName) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServiceInfos = am.getRunningServices(200);
        if (runningServiceInfos.size() <= 0) {
            return false;
        }
        for (ActivityManager.RunningServiceInfo serviceInfo : runningServiceInfos) {
            if (serviceInfo.service.getClassName().equals(serviceName)) {
                return true;
            }
        }
        return false;

    }


}
