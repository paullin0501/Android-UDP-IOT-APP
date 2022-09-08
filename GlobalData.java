package com.rexlite.rexlitebasicnew;

import java.util.Arrays;

public class GlobalData {
    byte Get_Device_IDSN[]={(byte)0xff,0x13,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,0x19,(byte)0x82,0x00,0x08,(byte)0xfe};
    byte MaxScene_APMode[]={0x0b,0x10,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,0x19,0x05,(byte)0xf4};


    public static byte[] MaxScene_APMode(String SSID ,String password) {

        byte APMode_CMD[]={0x0b,0x10,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,0x19,0x05,(byte)0xf4};
        byte Temp0_CMD[]={0x41};
        byte SSID_TEMP[]=SSID.getBytes();
        byte PASS_Temp[]=password.getBytes();
        byte Temp1_CMD[]={0x0d,0x0a};

        byte Get_length[]= {(byte) (Temp0_CMD.length+SSID_TEMP.length+Temp1_CMD.length+PASS_Temp.length+Temp1_CMD.length)};

        byte ALL[]=ConcatAll(APMode_CMD,Get_length,Temp0_CMD,SSID_TEMP,Temp1_CMD,PASS_Temp,Temp1_CMD);

        return ALL;

    }

    public static byte [] MaxScene(byte SN[] ,String Cmd) {

        byte MaxSceneCMD[]={0x0b,0x06};

        if(Cmd.equals("flash")) {
            byte[] DATATemp = {0x19, 0x05, 0x65, 0x00, 0x00};
            byte[] MaxScene_Temp = ConcatAll(MaxSceneCMD, SN, DATATemp);
            byte[] CRCTemp = GetCRC(MaxScene_Temp);
            byte[] SendCMD = ConcatAll(MaxScene_Temp, CRCTemp);
            return SendCMD;
        }else if(Cmd.equals("scene1")){
            byte[] DATATemp = {0x19, 0x05, 0x70, 0x00, 0x00};
            byte[] MaxScene_Temp = ConcatAll(MaxSceneCMD, SN, DATATemp);
            byte[] CRCTemp = GetCRC(MaxScene_Temp);
            byte[] SendCMD = ConcatAll(MaxScene_Temp, CRCTemp);
            return SendCMD;
        }
        else if(Cmd.equals("scene2")){
            byte[] DATATemp = {0x19, 0x05, 0x72, 0x00, 0x00};
            byte[] MaxScene_Temp = ConcatAll(MaxSceneCMD, SN, DATATemp);
            byte[] CRCTemp = GetCRC(MaxScene_Temp);
            byte[] SendCMD = ConcatAll(MaxScene_Temp, CRCTemp);
            return SendCMD;
        }
        else if(Cmd.equals("scene3")){
            byte[] DATATemp = {0x19, 0x05, 0x74, 0x00, 0x00};
            byte[] MaxScene_Temp = ConcatAll(MaxSceneCMD, SN, DATATemp);
            byte[] CRCTemp = GetCRC(MaxScene_Temp);
            byte[] SendCMD = ConcatAll(MaxScene_Temp, CRCTemp);
            return SendCMD;
        }
        else if(Cmd.equals("scene4")){
            byte[] DATATemp = {0x19, 0x05, 0x76, 0x00, 0x00};
            byte[] MaxScene_Temp = ConcatAll(MaxSceneCMD, SN, DATATemp);
            byte[] CRCTemp = GetCRC(MaxScene_Temp);
            byte[] SendCMD = ConcatAll(MaxScene_Temp, CRCTemp);
            return SendCMD;
        }
        else if(Cmd.equals("scene5")){
            byte[] DATATemp = {0x19, 0x05, 0x78, 0x00, 0x00};
            byte[] MaxScene_Temp = ConcatAll(MaxSceneCMD, SN, DATATemp);
            byte[] CRCTemp = GetCRC(MaxScene_Temp);
            byte[] SendCMD = ConcatAll(MaxScene_Temp, CRCTemp);
            return SendCMD;
        }
        else if(Cmd.equals("scene6")){
            byte[] DATATemp = {0x19, 0x05, 0x7a, 0x00, 0x00};
            byte[] MaxScene_Temp = ConcatAll(MaxSceneCMD, SN, DATATemp);
            byte[] CRCTemp = GetCRC(MaxScene_Temp);
            byte[] SendCMD = ConcatAll(MaxScene_Temp, CRCTemp);
            return SendCMD;
        }
        else if(Cmd.equals("power")){
            byte[] DATATemp = {0x19, 0x05, (byte)0x86, 0x00, 0x00};
            byte[] MaxScene_Temp = ConcatAll(MaxSceneCMD, SN, DATATemp);
            byte[] CRCTemp = GetCRC(MaxScene_Temp);
            byte[] SendCMD = ConcatAll(MaxScene_Temp, CRCTemp);
            return SendCMD;
        }
        else if(Cmd.equals("up")){
            byte[] DATATemp = {0x19, 0x05, (byte)0x88, 0x00, 0x00};
            byte[] MaxScene_Temp = ConcatAll(MaxSceneCMD, SN, DATATemp);
            byte[] CRCTemp = GetCRC(MaxScene_Temp);
            byte[] SendCMD = ConcatAll(MaxScene_Temp, CRCTemp);
            return SendCMD;
        }
        else if(Cmd.equals("down")){
            byte[] DATATemp = {0x19, 0x05, (byte)0x8a, 0x00, 0x00};
            byte[] MaxScene_Temp = ConcatAll(MaxSceneCMD, SN, DATATemp);
            byte[] CRCTemp = GetCRC(MaxScene_Temp);
            byte[] SendCMD = ConcatAll(MaxScene_Temp, CRCTemp);
            return SendCMD;
        }
        return "ERROR".getBytes();
    }


    public static byte [] MaxAIR(String Cmd , byte SN[] , byte Var1, byte Var2) {

        byte MaxSceneCMD[]={0x13,0x06};

        if(Cmd.equals("power")) {
            byte[] DATATemp = {0x19, 0x05, 0x70, 0x00, Var2};
            byte[] MaxScene_Temp = ConcatAll(MaxSceneCMD, SN, DATATemp);
            byte[] CRCTemp = GetCRC(MaxScene_Temp);
            byte[] SendCMD = ConcatAll(MaxScene_Temp, CRCTemp);
            return SendCMD;
        }else if(Cmd.equals("cool")){
            byte[] DATATemp = {0x19, 0x05, 0x71, 0x00, 0x00};
            byte[] MaxScene_Temp = ConcatAll(MaxSceneCMD, SN, DATATemp);
            byte[] CRCTemp = GetCRC(MaxScene_Temp);
            byte[] SendCMD = ConcatAll(MaxScene_Temp, CRCTemp);
            return SendCMD;
        }
        else if(Cmd.equals("warm")){
            byte[] DATATemp = {0x19, 0x05, 0x73, 0x00, 0x00};
            byte[] MaxScene_Temp = ConcatAll(MaxSceneCMD, SN, DATATemp);
            byte[] CRCTemp = GetCRC(MaxScene_Temp);
            byte[] SendCMD = ConcatAll(MaxScene_Temp, CRCTemp);
            return SendCMD;
        }
        else if(Cmd.equals("wet")){
            byte[] DATATemp = {0x19, 0x05, 0x75, 0x00, 0x00};
            byte[] MaxScene_Temp = ConcatAll(MaxSceneCMD, SN, DATATemp);
            byte[] CRCTemp = GetCRC(MaxScene_Temp);
            byte[] SendCMD = ConcatAll(MaxScene_Temp, CRCTemp);
            return SendCMD;
        }
        else if(Cmd.equals("fan")){
            byte[] DATATemp = {0x19, 0x05, 0x77, 0x00, 0x00};
            byte[] MaxScene_Temp = ConcatAll(MaxSceneCMD, SN, DATATemp);
            byte[] CRCTemp = GetCRC(MaxScene_Temp);
            byte[] SendCMD = ConcatAll(MaxScene_Temp, CRCTemp);
            return SendCMD;
        }
        else if(Cmd.equals("temp")){
            byte[] DATATemp = {0x19, 0x05, 0x78, Var1, 0x00};
            byte[] MaxScene_Temp = ConcatAll(MaxSceneCMD, SN, DATATemp);
            byte[] CRCTemp = GetCRC(MaxScene_Temp);
            byte[] SendCMD = ConcatAll(MaxScene_Temp, CRCTemp);
            return SendCMD;
        }
        else if(Cmd.equals("fanlevel")){
            byte[] DATATemp = {0x19, 0x05, 0x79, Var1, 0x00};
            byte[] MaxScene_Temp = ConcatAll(MaxSceneCMD, SN, DATATemp);
            byte[] CRCTemp = GetCRC(MaxScene_Temp);
            byte[] SendCMD = ConcatAll(MaxScene_Temp, CRCTemp);
            return SendCMD;
        }
        else if(Cmd.equals("timing")){
            byte[] DATATemp = {0x19, 0x05, 0x7a, Var1, Var2};
            byte[] MaxScene_Temp = ConcatAll(MaxSceneCMD, SN, DATATemp);
            byte[] CRCTemp = GetCRC(MaxScene_Temp);
            byte[] SendCMD = ConcatAll(MaxScene_Temp, CRCTemp);
            return SendCMD;
        }
        else if(Cmd.equals("auto")){
            byte[] DATATemp = {0x19, 0x05, 0x7b, 0x00, 0x00};
            byte[] MaxScene_Temp = ConcatAll(MaxSceneCMD, SN, DATATemp);
            byte[] CRCTemp = GetCRC(MaxScene_Temp);
            byte[] SendCMD = ConcatAll(MaxScene_Temp, CRCTemp);
            return SendCMD;
        }
        else if(Cmd.equals("sleep")){
            byte[] DATATemp = {0x19, 0x05, 0x7c, Var1, 0x00};
            byte[] MaxScene_Temp = ConcatAll(MaxSceneCMD, SN, DATATemp);
            byte[] CRCTemp = GetCRC(MaxScene_Temp);
            byte[] SendCMD = ConcatAll(MaxScene_Temp, CRCTemp);
            return SendCMD;
        }
        return "ERROR".getBytes();
    }


    public static byte [] Max_Panel(int Max_Ch_Num , byte SN[] , int Key ,byte Var) {
        if(Max_Ch_Num==1){
            byte MaxCHCmd[]={0x14,0x06};

            if(Key==1){
                byte[] MaxData={0x19,0x05,(byte)0xa2,0x00,0x00};
                byte[] CRCTemp=ConcatAll(MaxCHCmd,SN,MaxData);
                byte[] CRCNum=GetCRC(CRCTemp);
                byte[] SendCMD= ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            }else if(Key==2){
                byte[] MaxData={0x19,0x05,(byte)0xa3,0x00,0x00};
                byte[] CRCTemp=ConcatAll(MaxCHCmd,SN,MaxData);
                byte[] CRCNum=GetCRC(CRCTemp);
                byte[] SendCMD= ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            }
        }else if(Max_Ch_Num==2){
            byte MaxCHCmd[]={0x16,0x06};

            if(Key==1){
                byte[] MaxData={0x19,0x05,(byte)0xa2,0x00,0x00};
                byte[] CRCTemp=ConcatAll(MaxCHCmd,SN,MaxData);
                byte[] CRCNum=GetCRC(CRCTemp);
                byte[] SendCMD= ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            }else if(Key==2){
                byte[] MaxData={0x19,0x05,(byte)0xa3,0x00,0x00};
                byte[] CRCTemp=ConcatAll(MaxCHCmd,SN,MaxData);
                byte[] CRCNum=GetCRC(CRCTemp);
                byte[] SendCMD= ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            }else if(Key==3){
                byte[] MaxData={0x19,0x05,(byte)0xa4,0x00,0x00};
                byte[] CRCTemp=ConcatAll(MaxCHCmd,SN,MaxData);
                byte[] CRCNum=GetCRC(CRCTemp);
                byte[] SendCMD= ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            }else if(Key==4){
                byte[] MaxData={0x19,0x05,(byte)0xa5,0x00,0x00};
                byte[] CRCTemp=ConcatAll(MaxCHCmd,SN,MaxData);
                byte[] CRCNum=GetCRC(CRCTemp);
                byte[] SendCMD= ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            }

        }else if(Max_Ch_Num==3){
            byte MaxCHCmd[]={0x18,0x06};

            if(Key==1){
                byte[] MaxData={0x19,0x05,(byte)0xa2,0x00,0x00};
                byte[] CRCTemp=ConcatAll(MaxCHCmd,SN,MaxData);
                byte[] CRCNum=GetCRC(CRCTemp);
                byte[] SendCMD= ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            }else if(Key==2){
                byte[] MaxData={0x19,0x05,(byte)0xa3,0x00,0x00};
                byte[] CRCTemp=ConcatAll(MaxCHCmd,SN,MaxData);
                byte[] CRCNum=GetCRC(CRCTemp);
                byte[] SendCMD= ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            }else if(Key==3){
                byte[] MaxData={0x19,0x05,(byte)0xa4,0x00,0x00};
                byte[] CRCTemp=ConcatAll(MaxCHCmd,SN,MaxData);
                byte[] CRCNum=GetCRC(CRCTemp);
                byte[] SendCMD= ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            }else if(Key==4){
                byte[] MaxData={0x19,0x05,(byte)0xa5,0x00,0x00};
                byte[] CRCTemp=ConcatAll(MaxCHCmd,SN,MaxData);
                byte[] CRCNum=GetCRC(CRCTemp);
                byte[] SendCMD= ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            }
            else if(Key==5){
                byte[] MaxData={0x19,0x05,(byte)0xa6,0x00,0x00};
                byte[] CRCTemp=ConcatAll(MaxCHCmd,SN,MaxData);
                byte[] CRCNum=GetCRC(CRCTemp);
                byte[] SendCMD= ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            }
            else if(Key==6){
                byte[] MaxData={0x19,0x05,(byte)0xa7,0x00,0x00};
                byte[] CRCTemp=ConcatAll(MaxCHCmd,SN,MaxData);
                byte[] CRCNum=GetCRC(CRCTemp);
                byte[] SendCMD= ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            }
        }

        if(Max_Ch_Num==11){
            byte MaxCHCmd[]={0x14,0x06};
            if(Key==1){
                byte[] MaxData={0x19,0x05,(byte)0xaa,0x03,Var};
                byte[] CRCTemp=ConcatAll(MaxCHCmd,SN,MaxData);
                byte[] CRCNum=GetCRC(CRCTemp);
                byte[] SendCMD= ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            }
        }else if(Max_Ch_Num==12){
            byte MaxCHCmd[]={0x16,0x06};
            if(Key==1){
                byte[] MaxData={0x19,0x05,(byte)0xaa,0x03,Var};
                byte[] CRCTemp=ConcatAll(MaxCHCmd,SN,MaxData);
                byte[] CRCNum=GetCRC(CRCTemp);
                byte[] SendCMD= ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            }else if(Key==2){
                byte[] MaxData={0x19,0x05,(byte)0xac,0x03,Var};
                byte[] CRCTemp=ConcatAll(MaxCHCmd,SN,MaxData);
                byte[] CRCNum=GetCRC(CRCTemp);
                byte[] SendCMD= ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            }
        }else if(Max_Ch_Num==13){
            byte MaxCHCmd[]={0x18,0x06};
            if(Key==1){
                byte[] MaxData={0x19,0x05,(byte)0xaa,0x03,Var};
                byte[] CRCTemp=ConcatAll(MaxCHCmd,SN,MaxData);
                byte[] CRCNum=GetCRC(CRCTemp);
                byte[] SendCMD= ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            }else if(Key==2){
                byte[] MaxData={0x19,0x05,(byte)0xac,0x03,Var};
                byte[] CRCTemp=ConcatAll(MaxCHCmd,SN,MaxData);
                byte[] CRCNum=GetCRC(CRCTemp);
                byte[] SendCMD= ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            }else if(Key==3){
                byte[] MaxData={0x19,0x05,(byte)0xae,0x03,Var};
                byte[] CRCTemp=ConcatAll(MaxCHCmd,SN,MaxData);
                byte[] CRCNum=GetCRC(CRCTemp);
                byte[] SendCMD= ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            }
        }else if(Max_Ch_Num==21){
            byte MaxCHCmd[]={0x14,0x06};
            if(Key==1){
                byte[] MaxData={0x19,0x05,(byte)0xaa,0x04,Var};
                byte[] CRCTemp=ConcatAll(MaxCHCmd,SN,MaxData);
                byte[] CRCNum=GetCRC(CRCTemp);
                byte[] SendCMD= ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            }
        }else if(Max_Ch_Num==22){
            byte MaxCHCmd[]={0x16,0x06};
            if(Key==1){
                byte[] MaxData={0x19,0x05,(byte)0xaa,0x04,Var};
                byte[] CRCTemp=ConcatAll(MaxCHCmd,SN,MaxData);
                byte[] CRCNum=GetCRC(CRCTemp);
                byte[] SendCMD= ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            }else if(Key==2){
                byte[] MaxData={0x19,0x05,(byte)0xac,0x04,Var};
                byte[] CRCTemp=ConcatAll(MaxCHCmd,SN,MaxData);
                byte[] CRCNum=GetCRC(CRCTemp);
                byte[] SendCMD= ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            }
        }else if(Max_Ch_Num==23){
            byte MaxCHCmd[]={0x18,0x06};
            if(Key==1){
                byte[] MaxData={0x19,0x05,(byte)0xaa,0x04,Var};
                byte[] CRCTemp=ConcatAll(MaxCHCmd,SN,MaxData);
                byte[] CRCNum=GetCRC(CRCTemp);
                byte[] SendCMD= ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            }else if(Key==2){
                byte[] MaxData={0x19,0x05,(byte)0xac,0x04,Var};
                byte[] CRCTemp=ConcatAll(MaxCHCmd,SN,MaxData);
                byte[] CRCNum=GetCRC(CRCTemp);
                byte[] SendCMD= ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            }else if(Key==3){
                byte[] MaxData={0x19,0x05,(byte)0xae,0x04,Var};
                byte[] CRCTemp=ConcatAll(MaxCHCmd,SN,MaxData);
                byte[] CRCNum=GetCRC(CRCTemp);
                byte[] SendCMD= ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            }
        }else if(Max_Ch_Num==31){
            byte MaxCHCmd[]={0x14,0x06};
            if(Key==1){
                byte[] MaxData={0x19,0x05,(byte)0xaa,0x05,Var};
                byte[] CRCTemp=ConcatAll(MaxCHCmd,SN,MaxData);
                byte[] CRCNum=GetCRC(CRCTemp);
                byte[] SendCMD= ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            }
        }else if(Max_Ch_Num==32){
            byte MaxCHCmd[]={0x16,0x06};
            if(Key==1){
                byte[] MaxData={0x19,0x05,(byte)0xaa,0x05,Var};
                byte[] CRCTemp=ConcatAll(MaxCHCmd,SN,MaxData);
                byte[] CRCNum=GetCRC(CRCTemp);
                byte[] SendCMD= ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            }else if(Key==2){
                byte[] MaxData={0x19,0x05,(byte)0xac,0x05,Var};
                byte[] CRCTemp=ConcatAll(MaxCHCmd,SN,MaxData);
                byte[] CRCNum=GetCRC(CRCTemp);
                byte[] SendCMD= ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            }
        }else if(Max_Ch_Num==33){
            byte MaxCHCmd[]={0x18,0x06};
            if(Key==1){
                byte[] MaxData={0x19,0x05,(byte)0xaa,0x05,Var};
                byte[] CRCTemp=ConcatAll(MaxCHCmd,SN,MaxData);
                byte[] CRCNum=GetCRC(CRCTemp);
                byte[] SendCMD= ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            }else if(Key==2){
                byte[] MaxData={0x19,0x05,(byte)0xac,0x05,Var};
                byte[] CRCTemp=ConcatAll(MaxCHCmd,SN,MaxData);
                byte[] CRCNum=GetCRC(CRCTemp);
                byte[] SendCMD= ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            }else if(Key==3){
                byte[] MaxData={0x19,0x05,(byte)0xae,0x05,Var};
                byte[] CRCTemp=ConcatAll(MaxCHCmd,SN,MaxData);
                byte[] CRCNum=GetCRC(CRCTemp);
                byte[] SendCMD= ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            }
        }


        return "ERROR".getBytes();
    }


    public static byte [] ConcatAll(byte[] first, byte[]... rest) {
        int totalLength = first.length;
        for (byte[] array : rest) {
            totalLength += array.length;
        }
        byte[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (byte[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
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
