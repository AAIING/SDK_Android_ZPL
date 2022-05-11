package com.android.zpl;

import android.content.Context;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by NO on 2018/12/13.
 */

public class UtilityTooth {

    public static int chackEdtextArea(Context context, EditText editText, int lessNumber, int moreNumber, String str){
        String edstr = editText.getText().toString().trim();
        if (edstr.length()>4){
            ToastMessage(context, str);
            return -1;
        }
        int valueOf = Integer.valueOf(edstr);
        if (valueOf<lessNumber||valueOf>moreNumber) {
            ToastMessage(context, str);
            return -1;
        }else {
            return valueOf;
        }
    }

    private static void ToastMessage(Context context, String str) {
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
    }


    public static String byteToHex(byte[] data){
        final StringBuilder stringBuilder = new StringBuilder(data.length);
        for(byte byteChar : data){
            stringBuilder.append(String.format("%02X", byteChar));
        }
        return stringBuilder.toString();
    }

    public static byte[] hexToByte(String path){
        Pattern p = Pattern.compile("\\s*|\t|\r|\n");
        Matcher ma = p.matcher(path);
        path = ma.replaceAll("");
        int m=path.length()/2;
        if(m*2<path.length()){
            m++;
        }
        String[] strs=new String[m];
        int j=0;
        for(int i=0;i<path.length();i++){
            if(i%2==0){//每隔两个
                strs[j]=""+path.charAt(i);
            }else{
                strs[j]=strs[j]+path.charAt(i);
                j++;
            }
        }
        byte[] by=new byte[strs.length];
        for (int i = 0; i < strs.length; i++) {
            if (strs[i].length()==2) {
                by[i]= Integer.valueOf(strs[i], 16).byteValue();
            }
        }
        return by;
    }
}
