package com.rexlite.rexlitebasicnew;

public class CRC16_Modbus {

    protected CRC16_Modbus(){

    }

    public static byte[] GetCRC(byte[] bytes) {

        int CRC = 0x0000ffff;

        int POLYNOMIAL = 0x0000a001;
        int i, j;
        for (i = 0; i < bytes.length; i++) {
            CRC ^= ((int) bytes[i] & 0x000000ff);
            for (j = 0; j < 8; j++) {
                if ((CRC & 0x00000001) != 0) {
                    CRC >>= 1;
                    CRC ^= POLYNOMIAL;
                } else {
                    CRC >>= 1;
                }
            }
        }

        String result = Integer.toHexString(CRC).toUpperCase();
        if (result.length() != 4) {
            StringBuffer sb = new StringBuffer("0000");
            result = sb.replace(4 - result.length(), 4, result).toString();
        }

        //return result.substring(2, 4) + " " + result.substring(0, 2);

        String str=result.substring(2, 4) + result.substring(0, 2);

        if (str == null) {
            return null;
        }
        if (str.length() == 0) {
            return new byte[0];
        }
        byte[] byteArray = new byte[str.length() / 2];
        for (int x = 0; x < byteArray.length; x++){
            String subStr = str.substring(2 * x, 2 * x + 2);
            byteArray[x] = ((byte)Integer.parseInt(subStr, 16));
        }
        return byteArray;

    }

}
