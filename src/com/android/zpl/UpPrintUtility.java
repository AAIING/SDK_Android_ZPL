package com.android.zpl;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import ZPL.ZPLPrinterHelper;

class UpPrintUtility {

    private void upPrint(final Context context){
        final ZPLPrinterHelper zplPrinterHelper=ZPLPrinterHelper.getZPL(context);
        new Thread(){
            @Override
            public void run() {
                super.run();
                try{
                    InputStream afis = ((Activity)context).getResources().getAssets().open("id2ie2V1.0.47_Beta2.img");
                    byte[] data = InputStreamToByte(afis);
                    zplPrinterHelper.WriteData(UpPrintUtility.inToUpPrint());
                    zplPrinterHelper.WriteData(UpPrintUtility.getTop(data));
                    byte[] readData = zplPrinterHelper.ReadData(2);
                    if (readData==null||readData.length!=1||readData[0]!=0){
                        Log.d("Print", "readData one error timeout");
                        return;
                    }
                    List<byte[]> bytesToList = UpPrintUtility.addBytesToList(UpPrintUtility.getBody(data));
                    Log.d("Print", "总包数 "+bytesToList.size());
                    for (int i = 0; i < bytesToList.size(); i++) {
                        Log.d("Print", "当前包数 "+(i+1));
                        zplPrinterHelper.WriteData(bytesToList.get(i));
                        //需要加延时，不加打印机会死
                        sleep(100);
                    }
                    byte[] readDataEnd = zplPrinterHelper.ReadData(8);
                    if (readDataEnd==null||readDataEnd.length!=1||readDataEnd[0]!=0){
                        Log.d("Print", "readDataEnd error timeout");
                        return;
                    }
                    zplPrinterHelper.WriteData(UpPrintUtility.resetPrint());
                    Log.d("Print", "发送完成");
                }catch (Exception e){
                    Log.d("Print", "升级失败 "+e.getMessage().toString());
                }

            }
        }.start();
    }


    public static List<byte[]> addBytesToList(byte[] dataBytes) {
        // 纯图片数据数组
        List<byte[]> bytesList = new ArrayList<byte[]>();
        // 每包数据量
        int separatedCount = 1024; // 4KB
//	        int separatedCount = 10;
        // 分包数量
        int bytesCount = dataBytes.length / separatedCount;
        // 将每包依次发出
        int i = 0;
        for (; i < bytesCount; i++) {
            // 新建数组, 每次只发separatedCount个数据
            byte[] perBytes = new byte[separatedCount];
            // 每次只取包的 第i个 到 第(i * separatedCount)个
            System.arraycopy(dataBytes, i * separatedCount, perBytes, 0, separatedCount);
//	            Log.e(TAG, "dataBytes count = " + i * separatedCount + " - " + ((i + 1) * separatedCount - 1));
            bytesList.add(perBytes);
        }
        // 前面的数据包发送完毕，计算最后一个数据包的起始点
        int lastBytesPosition = i * separatedCount;
        // 分包后剩余的数据量长度
        int leftLength = dataBytes.length - lastBytesPosition;
        if (leftLength > 0) {
            byte[] lastBytes = new byte[leftLength];
            // 把dataBytes的最后一包数据取出到lastBytes中
            System.arraycopy(dataBytes, lastBytesPosition, lastBytes, 0, leftLength);
            bytesList.add(lastBytes);
        }
        return bytesList;
    }

    public static byte[] getTop(byte[] data){
        byte[] topData = new byte[76];
        System.arraycopy(data,0,topData,0,topData.length);
        return topData;
    }

    public static byte[] getBody(byte[] data){
        byte[] bodyData = new byte[data.length-76];
        System.arraycopy(data,76,bodyData,0,bodyData.length);
        return bodyData;
    }

    public static byte[] inToUpPrint(){
        return new byte[]{0x1B,0x1C,0x26,0x20,0x56,0x31,0x20,0x64,0x6F,0x20,0x22,0x64,0x66,0x75,0x22,0x0d,0x0a};
    }

    public static byte[] resetPrint(){
        return new byte[]{0x1B,0x1C,0x26,0x20,0x56,0x31,0x20,0x64,0x6F,0x20,0x22,0x72,0x65,0x73,0x65,0x74,0x5f,0x70,0x72,0x69,0x6E,0x74,0x65,0x72,0x22,0x0d,0x0a};
    }

    private byte[] InputStreamToByte(InputStream is) throws IOException {
        ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
        int ch;
        while ((ch = is.read()) != -1) {
            bytestream.write(ch);
        }
        byte imgdata[] = bytestream.toByteArray();
        bytestream.close();
        return imgdata;
    }
}
