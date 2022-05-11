package com.android.zpl;

public class CommandZPL {
    public static byte[] getWriteRFID(int address, int memory,byte[] data){
       return  ("^RFW,H,"+address+","+data.length+","+memory+"^FD"+UtilityTooth.byteToHex(data)+"^FS\r\n").getBytes();
    }
}
