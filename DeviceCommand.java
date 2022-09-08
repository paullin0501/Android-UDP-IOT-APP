package com.rexlite.rexlitebasicnew;

import java.util.Arrays;

public class DeviceCommand {

    public byte[] MaxScene_APMode(String SSID, String password) {

        byte APMode_CMD[] = {0x0b, 0x10, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x19, 0x05, (byte) 0xf4};
        byte Temp0_CMD[] = {0x41};
        byte SSID_TEMP[] = SSID.getBytes();
        byte PASS_Temp[] = password.getBytes();
        byte Temp1_CMD[] = {0x0d, 0x0a};

        byte Get_length[] = {(byte) (Temp0_CMD.length + SSID_TEMP.length + Temp1_CMD.length + PASS_Temp.length + Temp1_CMD.length)};
        byte ALL[] = ConcatAll(APMode_CMD, Get_length, Temp0_CMD, SSID_TEMP, Temp1_CMD, PASS_Temp, Temp1_CMD);
        byte CRC[] = GetCRC(ALL);
        return ConcatAll(ALL, CRC);

    }
    public byte[] oneTouchCommand(byte[] SN, boolean status) {

        byte commandData[] = {0x19, 0x05};
        byte command[] = {0x0a,0x06};
        byte CMD[] = {};
        if(status){
            byte statusCMD[] = {(byte) 0xa3,0x05,0x00};
            byte[] commandTemp = ConcatAll(command,SN, commandData ,statusCMD);
            byte[] CRCTemp = GetCRC(commandTemp);
            byte[] SendCMD = ConcatAll(commandTemp, CRCTemp);
            CMD = SendCMD;

        }
        if(!status){
            byte statusCMD[] = {(byte) 0xa2,0x00,0x00};
            byte[] commandTemp = ConcatAll(command,SN, commandData ,statusCMD);
            byte[] CRCTemp = GetCRC(commandTemp);
            byte[] SendCMD = ConcatAll(commandTemp, CRCTemp);
            CMD = SendCMD;
        }
        return CMD;

    }
    //設定窗簾連動
    public byte[] curtainBindingSetting(String type, byte[] sn,String bindType,String bindSN,String button) {
        byte commandData[] = {0x19, 0x05, (byte) 0xf4, 0x09,0x61};
        byte commandData1[] = UDP.hexToByte(bindSN);
        byte command[] = {0x10};
        byte maxType[] = UDP.hexToByte(type);
        byte bindType1[] = UDP.hexToByte(bindType);
        byte button1[] = UDP.hexToByte(button);
        byte[] commandTemp = ConcatAll(maxType, command,sn, commandData ,bindType1, commandData1,button1);
        byte[] CRCTemp = GetCRC(commandTemp);
        byte[] SendCMD = ConcatAll(commandTemp, CRCTemp);
        return  SendCMD;
    }
    //binds窗簾控制
    public byte[] bindsSetting(String type,int chNum, byte[] sn,String direction) {
        byte commandData[] = {0x19, 0x05};
        if (type.equals("14")) {
            switch (chNum) {
                case 2:
                    byte[] dataTemp1 = new byte[]{0x19, 0x05, (byte) 0xAA, 0x05};
                    commandData = dataTemp1;
                    break;
                case 1:
                    byte[] dataTemp2 = new byte[]{0x19, 0x05, (byte) 0xAC, 0x05};
                    commandData = dataTemp2;
                    break;
                case 3:
                    byte[] dataTemp3 = new byte[]{0x19, 0x05, (byte) 0xAE, 0x05};
                    commandData = dataTemp3;
                    break;
            }
        }

        if (type.equals("16")) {
            switch (chNum) {
                case 2:
                    byte[] dataTemp1 = new byte[]{0x19, 0x05, (byte) 0xAA, 0x05};
                    commandData = dataTemp1;
                    break;
                case 1:
                    byte[] dataTemp2 = new byte[]{0x19, 0x05, (byte) 0xAC, 0x05};
                    commandData = dataTemp2;
                    break;
                case 3:
                    byte[] dataTemp3 = new byte[]{0x19, 0x05, (byte) 0xAE, 0x05};
                    commandData = dataTemp3;
                    break;
            }
        }
        if (type.equals("18")) {
            switch (chNum) {
                case 2:
                    byte[] dataTemp1 = new byte[]{0x19, 0x05, (byte) 0xAC, 0x05};
                    commandData = dataTemp1;
                    break;
                case 1:
                    byte[] dataTemp2 = new byte[]{0x19, 0x05, (byte) 0xAE, 0x05};
                    commandData = dataTemp2;
                    break;
                case 3:
                    byte[] dataTemp3 = new byte[]{0x19, 0x05, (byte) 0xAA, 0x05};
                    commandData = dataTemp3;
                    break;
            }
        }
        byte directionCmd[]= {0x00};
        if(direction.equals("left")){
            directionCmd = new byte[]{0x03};
        }
        if(direction.equals("right")){
            directionCmd = new byte[]{0x07};
        }
        if(direction.equals("stop")){
            directionCmd = new byte[]{0x00};
        }
        byte command[] = {0x06};
        byte maxType[] = UDP.hexToByte(type);
        byte[] commandTemp = ConcatAll(maxType, command, sn, commandData,directionCmd);
        byte[] CRCTemp = GetCRC(commandTemp);
        byte[] SendCMD = ConcatAll(commandTemp, CRCTemp);
        return SendCMD;

    }
    //查看窗簾連動狀態
    public byte[] curtainSettingStatus(String type, byte[] sn) {
        byte commandData[] = {0x19, 0x05, (byte) 0xf4, 0x01,0x62};
        byte command[] = {0x10};
        byte maxType[] = UDP.hexToByte(type);
        byte[] commandTemp = ConcatAll(maxType, command,sn, commandData);
        byte[] CRCTemp = GetCRC(commandTemp);
        byte[] SendCMD = ConcatAll(commandTemp, CRCTemp);
        return  SendCMD;
    }

    //裝置閃爍  bright代表要開還是關
    public byte[] settingSearchDeviceCMD(byte[] maxType, boolean bright, byte[] deviceSN) {
        byte commandData[] = {0x19, 0x05, 0x65, 0x00};
        byte brightCMD[];
        byte command[] = {0x06};
        if (bright) {
            brightCMD = new byte[]{0x01};
        } else {
            brightCMD = new byte[]{0x00};
        }
        byte[] commandTemp = ConcatAll(maxType, command, deviceSN, commandData, brightCMD);
        byte[] CRCTemp = GetCRC(commandTemp);
        byte[] SendCMD = ConcatAll(commandTemp, CRCTemp);
        return SendCMD;

    }

    //根據CH來定義指令
    public byte[] settingCurtainCMD(int chNum, byte maxType) {
        byte[] dataTemp = new byte[]{};
        if (maxType == 0x14) {
            switch (chNum) {
                case 2:
                    byte[] dataTemp1 = new byte[]{0x19, 0x05, (byte) 0xAA, 0x05};
                    dataTemp = dataTemp1;
                    break;
                case 1:
                    byte[] dataTemp2 = new byte[]{0x19, 0x05, (byte) 0xAC, 0x05};
                    dataTemp = dataTemp2;
                    break;
                case 3:
                    byte[] dataTemp3 = new byte[]{0x19, 0x05, (byte) 0xAE, 0x05};
                    dataTemp = dataTemp3;
                    break;
            }
        }

        if (maxType == 0x16) {
            switch (chNum) {
                case 2:
                    byte[] dataTemp1 = new byte[]{0x19, 0x05, (byte) 0xAA, 0x05};
                    dataTemp = dataTemp1;
                    break;
                case 1:
                    byte[] dataTemp2 = new byte[]{0x19, 0x05, (byte) 0xAC, 0x05};
                    dataTemp = dataTemp2;
                    break;
                case 3:
                    byte[] dataTemp3 = new byte[]{0x19, 0x05, (byte) 0xAE, 0x05};
                    dataTemp = dataTemp3;
                    break;
            }
        }
        if (maxType == 0x18) {
            switch (chNum) {
                case 2:
                    byte[] dataTemp1 = new byte[]{0x19, 0x05, (byte) 0xAC, 0x05};
                    dataTemp = dataTemp1;
                    break;
                case 1:
                    byte[] dataTemp2 = new byte[]{0x19, 0x05, (byte) 0xAE, 0x05};
                    dataTemp = dataTemp2;
                    break;
                case 3:
                    byte[] dataTemp3 = new byte[]{0x19, 0x05, (byte) 0xAA, 0x05};
                    dataTemp = dataTemp3;
                    break;
            }
        }
        return dataTemp;
    }

    //組合按鈕的指令到可以發送
    public byte[] settingCMD(byte[] chData, byte maxType, byte[] deviceSN) {
        byte[] liteCHCmd = {maxType, 0x06};
        byte[] CRCTemp = ConcatAll(liteCHCmd, deviceSN, chData);
        byte[] CRCNum = GetCRC(CRCTemp);
        byte[] sendCMD = ConcatAll(CRCTemp, CRCNum);
        return sendCMD;
    }

    public byte[] MaxScene(byte SN[], String Cmd) {

        byte MaxSceneCMD[] = {0x0b, 0x06};

        if (Cmd.equals("flash")) {
            byte[] DATATemp = {0x19, 0x05, 0x65, 0x00, 0x00};
            byte[] MaxScene_Temp = ConcatAll(MaxSceneCMD, SN, DATATemp);
            byte[] CRCTemp = GetCRC(MaxScene_Temp);
            byte[] SendCMD = ConcatAll(MaxScene_Temp, CRCTemp);
            return SendCMD;
        } else if (Cmd.equals("scene1")) {
            byte[] DATATemp = {0x19, 0x05, 0x70, 0x00, 0x00};
            byte[] MaxScene_Temp = ConcatAll(MaxSceneCMD, SN, DATATemp);
            byte[] CRCTemp = GetCRC(MaxScene_Temp);
            byte[] SendCMD = ConcatAll(MaxScene_Temp, CRCTemp);
            return SendCMD;
        } else if (Cmd.equals("scene2")) {
            byte[] DATATemp = {0x19, 0x05, 0x72, 0x00, 0x00};
            byte[] MaxScene_Temp = ConcatAll(MaxSceneCMD, SN, DATATemp);
            byte[] CRCTemp = GetCRC(MaxScene_Temp);
            byte[] SendCMD = ConcatAll(MaxScene_Temp, CRCTemp);
            return SendCMD;
        } else if (Cmd.equals("scene3")) {
            byte[] DATATemp = {0x19, 0x05, 0x74, 0x00, 0x00};
            byte[] MaxScene_Temp = ConcatAll(MaxSceneCMD, SN, DATATemp);
            byte[] CRCTemp = GetCRC(MaxScene_Temp);
            byte[] SendCMD = ConcatAll(MaxScene_Temp, CRCTemp);
            return SendCMD;
        } else if (Cmd.equals("scene4")) {
            byte[] DATATemp = {0x19, 0x05, 0x76, 0x00, 0x00};
            byte[] MaxScene_Temp = ConcatAll(MaxSceneCMD, SN, DATATemp);
            byte[] CRCTemp = GetCRC(MaxScene_Temp);
            byte[] SendCMD = ConcatAll(MaxScene_Temp, CRCTemp);
            return SendCMD;
        } else if (Cmd.equals("scene5")) {
            byte[] DATATemp = {0x19, 0x05, 0x78, 0x00, 0x00};
            byte[] MaxScene_Temp = ConcatAll(MaxSceneCMD, SN, DATATemp);
            byte[] CRCTemp = GetCRC(MaxScene_Temp);
            byte[] SendCMD = ConcatAll(MaxScene_Temp, CRCTemp);
            return SendCMD;
        } else if (Cmd.equals("scene6")) {
            byte[] DATATemp = {0x19, 0x05, 0x7a, 0x00, 0x00};
            byte[] MaxScene_Temp = ConcatAll(MaxSceneCMD, SN, DATATemp);
            byte[] CRCTemp = GetCRC(MaxScene_Temp);
            byte[] SendCMD = ConcatAll(MaxScene_Temp, CRCTemp);
            return SendCMD;
        } else if (Cmd.equals("powerOn")) {
            byte[] DATATemp = {0x19, 0x05, (byte) 0x86, 0x00, 0x00};
            byte[] MaxScene_Temp = ConcatAll(MaxSceneCMD, SN, DATATemp);
            byte[] CRCTemp = GetCRC(MaxScene_Temp);
            byte[] SendCMD = ConcatAll(MaxScene_Temp, CRCTemp);
            return SendCMD;

        }
        else if (Cmd.equals("powerOff")) {
            byte[] DATATemp = {0x19, 0x05, (byte) 0x86, 0x00, 0x01};
            byte[] MaxScene_Temp = ConcatAll(MaxSceneCMD, SN, DATATemp);
            byte[] CRCTemp = GetCRC(MaxScene_Temp);
            byte[] SendCMD = ConcatAll(MaxScene_Temp, CRCTemp);
            return SendCMD;

        }
        else if (Cmd.equals("up")) {
            byte[] DATATemp = {0x19, 0x05, (byte) 0x88, 0x00, 0x00};
            byte[] MaxScene_Temp = ConcatAll(MaxSceneCMD, SN, DATATemp);
            byte[] CRCTemp = GetCRC(MaxScene_Temp);
            byte[] SendCMD = ConcatAll(MaxScene_Temp, CRCTemp);
            return SendCMD;
        } else if (Cmd.equals("down")) {
            byte[] DATATemp = {0x19, 0x05, (byte) 0x8a, 0x00, 0x00};
            byte[] MaxScene_Temp = ConcatAll(MaxSceneCMD, SN, DATATemp);
            byte[] CRCTemp = GetCRC(MaxScene_Temp);
            byte[] SendCMD = ConcatAll(MaxScene_Temp, CRCTemp);
            return SendCMD;
        } else if (Cmd.equals("status")) {
            byte DATATemp[] = {0x19, 0x05, (byte) 0xc7, 0x00, 0x10};
            byte[] MaxScene_Temp = ConcatAll(MaxSceneCMD, SN, DATATemp);
            byte[] CRCTemp = GetCRC(MaxScene_Temp);
            byte[] SendCMD = ConcatAll(MaxScene_Temp, CRCTemp);
            return SendCMD;
        } else if (Cmd.equals("shortcut")) {
            byte DATATemp[] = {0x19, 0x30, 0x03, 0x10, 0x20};
            byte maxCMD[] = {0x0b, 0x12};
            byte[] MaxScene_Temp = ConcatAll(maxCMD, SN, DATATemp);
            byte[] CRCTemp = GetCRC(MaxScene_Temp);
            byte[] SendCMD = ConcatAll(MaxScene_Temp, CRCTemp);
            return SendCMD;
        } else if (Cmd.equals("lock")) {
            byte DATATemp[] = {0x19, 0x05, (byte) 0xf4, 0x09, 0x48, 0x0b, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
            byte maxCMD[] = {0x0b, 0x10};
            byte lock[] = {0x00};
            byte[] MaxScene_Temp = ConcatAll(maxCMD, SN, DATATemp, lock);
            byte[] CRCTemp = GetCRC(MaxScene_Temp);
            byte[] SendCMD = ConcatAll(MaxScene_Temp, CRCTemp);
            return SendCMD;
        } else if (Cmd.equals("unlock")) {
            byte DATATemp[] = {0x19, 0x05, (byte) 0xf4, 0x09, 0x48, 0x0b, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
            byte maxCMD[] = {0x0b, 0x10};
            byte lock[] = {(byte) 0xff};
            byte[] MaxScene_Temp = ConcatAll(maxCMD, SN, DATATemp, lock);
            byte[] CRCTemp = GetCRC(MaxScene_Temp);
            byte[] SendCMD = ConcatAll(MaxScene_Temp, CRCTemp);
            return SendCMD;
        } else if (Cmd.equals("checkPassword")) {
            byte DATATemp[] = {0x19, 0x02, (byte) 0xc0, 0x31};
            byte maxCMD[] = {0x0b, 0x02};
            byte[] MaxScene_Temp = ConcatAll(maxCMD, SN, DATATemp);
            byte[] CRCTemp = GetCRC(MaxScene_Temp);
            byte[] SendCMD = ConcatAll(MaxScene_Temp, CRCTemp);
            return SendCMD;
        } else if (Cmd.equals("checkVersion")) {
            byte DATATemp[] = {0x19, (byte) 0x0f, (byte) 0xe0};
            byte maxCMD[] = {0x0b, 0x02};
            byte length[] = {0x06};
            byte[] MaxScene_Temp = ConcatAll(maxCMD, SN, DATATemp, length);
            byte[] CRCTemp = GetCRC(MaxScene_Temp);
            byte[] SendCMD = ConcatAll(MaxScene_Temp, CRCTemp);
            return SendCMD;
        } else if (Cmd.equals("resetPassword")) {
            byte DATATemp[] = {0x19, 0x02, (byte) 0xd7};
            byte maxCMD[] = {0x0b, 0x0f, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
            byte[] end = {(byte) 0x0d, (byte) 0x0a};
            byte[] MaxScene_Temp = ConcatAll(maxCMD, DATATemp, SN, end);
            byte[] CRCTemp = GetCRC(MaxScene_Temp);
            byte[] SendCMD = ConcatAll(MaxScene_Temp, CRCTemp);
            return SendCMD;
        }

        return "ERROR".getBytes();
    }




    //key代表上或下false為關掉(下)
    //設定按鈕的指令
    public byte[] settingMaxLiteBtnCMD(int chNum, boolean key, byte maxType) {
        byte[] dataTemp = new byte[]{};
        if (maxType == 0x16) {
            switch (chNum) {
                case 2:
                    if (!key) {
                        byte[] dataTemp1 = new byte[]{0x19, 0x05, (byte) 0xA3, 0x00, 0x00};
                        dataTemp = dataTemp1;
                        break;
                    }
                    if (key) {

                        byte[] dataTemp1 = new byte[]{0x19, 0x05, (byte) 0xA2, 0x00, 0x00};
                        dataTemp = dataTemp1;
                        break;
                    }
                case 1:
                    if (!key) {
                        byte[] dataTemp2 = new byte[]{0x19, 0x05, (byte) 0xA5, 0x00, 0x00};
                        dataTemp = dataTemp2;
                        break;
                    }
                    if (key) {
                        byte[] dataTemp2 = new byte[]{0x19, 0x05, (byte) 0xA4, 0x00, 0x00};
                        dataTemp = dataTemp2;
                        break;
                    }
            }
        }
        if (maxType == 0x18) {
            switch (chNum) {
                case 2:
                    if (!key) {
                        byte[] dataTemp1 = new byte[]{0x19, 0x05, (byte) 0xA5, 0x00, 0x00};
                        dataTemp = dataTemp1;
                        break;
                    }
                    if (key) {
                        byte[] dataTemp1 = new byte[]{0x19, 0x05, (byte) 0xA4, 0x00, 0x00};
                        dataTemp = dataTemp1;
                        break;
                    }
                case 1:
                    if (!key) {
                        byte[] dataTemp2 = new byte[]{0x19, 0x05, (byte) 0xA7, 0x00, 0x00};
                        dataTemp = dataTemp2;
                        break;
                    }
                    if (key) {
                        byte[] dataTemp2 = new byte[]{0x19, 0x05, (byte) 0xA6, 0x00, 0x00};
                        dataTemp = dataTemp2;
                        break;
                    }
                case 3:
                    if (!key) {
                        byte[] dataTemp3 = new byte[]{0x19, 0x05, (byte) 0xA3, 0x00, 0x00};
                        dataTemp = dataTemp3;
                        break;
                    }
                    if (key) {
                        byte[] dataTemp3 = new byte[]{0x19, 0x05, (byte) 0xA2, 0x00, 0x00};
                        dataTemp = dataTemp3;
                        break;
                    }

            }

        }
        if (maxType == 0x14 && chNum == 1) {
            if (!key) {
                byte[] dataTemp2 = new byte[]{0x19, 0x05, (byte) 0xA3, 0x00, 0x00};
                dataTemp = dataTemp2;
            }
            if (key) {
                byte[] dataTemp2 = new byte[]{0x19, 0x05, (byte) 0xA2, 0x00, 0x00};
                dataTemp = dataTemp2;
            }

        }
        return dataTemp;
    }

    public byte[] settingMaxLiteSleepCMD(int chNum, boolean key, byte maxType) {
        byte[] dataTemp = new byte[]{};
        if (maxType == 0x16) {
            switch (chNum) {
                case 2:
                    if (!key) {
                        byte[] dataTemp1 = new byte[]{0x19, 0x05, (byte) 0xA3};
                        dataTemp = dataTemp1;
                        break;
                    }
                    if (key) {

                        byte[] dataTemp1 = new byte[]{0x19, 0x05, (byte) 0xA2};
                        dataTemp = dataTemp1;
                        break;
                    }
                case 1:
                    if (!key) {
                        byte[] dataTemp2 = new byte[]{0x19, 0x05, (byte) 0xA5};
                        dataTemp = dataTemp2;
                        break;
                    }
                    if (key) {
                        byte[] dataTemp2 = new byte[]{0x19, 0x05, (byte) 0xA4};
                        dataTemp = dataTemp2;
                        break;
                    }
            }
        }
        if (maxType == 0x18) {
            switch (chNum) {
                case 2:
                    if (!key) {
                        byte[] dataTemp1 = new byte[]{0x19, 0x05, (byte) 0xA5};
                        dataTemp = dataTemp1;
                        break;
                    }
                    if (key) {
                        byte[] dataTemp1 = new byte[]{0x19, 0x05, (byte) 0xA4};
                        dataTemp = dataTemp1;
                        break;
                    }
                case 1:
                    if (!key) {
                        byte[] dataTemp2 = new byte[]{0x19, 0x05, (byte) 0xA7};
                        dataTemp = dataTemp2;
                        break;
                    }
                    if (key) {
                        byte[] dataTemp2 = new byte[]{0x19, 0x05, (byte) 0xA6};
                        dataTemp = dataTemp2;
                        break;
                    }
                case 3:
                    if (!key) {
                        byte[] dataTemp3 = new byte[]{0x19, 0x05, (byte) 0xA3};
                        dataTemp = dataTemp3;
                        break;
                    }
                    if (key) {
                        byte[] dataTemp3 = new byte[]{0x19, 0x05, (byte) 0xA2};
                        dataTemp = dataTemp3;
                        break;
                    }

            }

        }
        if (maxType == 0x14 && chNum == 1) {
            if (!key) {
                byte[] dataTemp2 = new byte[]{0x19, 0x05, (byte) 0xA3};
                dataTemp = dataTemp2;
            }
            if (key) {
                byte[] dataTemp2 = new byte[]{0x19, 0x05, (byte) 0xA2};
                dataTemp = dataTemp2;
            }

        }
        return dataTemp;
    }


    /**
     * @param Max_Ch_Num //裝置種類
     * @param SN         裝置SN
     * @param Key
     * @param Var
     * @return
     */
    public byte[] maxLite(int Max_Ch_Num, byte SN[], int Key, byte Var) {


        //一切
        if (Max_Ch_Num == 1) {
            byte MaxCHCmd[] = {0x14, 0x06};
            //key與Var代0代表詢問裝置資訊
            if (Key == 0 && Var == 0) {
                byte DATATemp[] = {0x19, 0x05, (byte) 0xc7, 0x00, 0x10};
                byte[] MaxLite_Temp = ConcatAll(MaxCHCmd, SN, DATATemp);
                byte[] CRCTemp = GetCRC(MaxLite_Temp);
                byte[] SendCMD = ConcatAll(MaxLite_Temp, CRCTemp);
                return SendCMD;
            }
            if (Key == 1) {
                byte[] MaxData = {0x19, 0x05, (byte) 0xa2, 0x00, 0x00};
                byte[] CRCTemp = ConcatAll(MaxCHCmd, SN, MaxData);
                byte[] CRCNum = GetCRC(CRCTemp);
                byte[] SendCMD = ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            } else if (Key == 2) {
                byte[] MaxData = {0x19, 0x05, (byte) 0xa3, 0x00, 0x00};
                byte[] CRCTemp = ConcatAll(MaxCHCmd, SN, MaxData);
                byte[] CRCNum = GetCRC(CRCTemp);
                byte[] SendCMD = ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            }
            //二切
        } else if (Max_Ch_Num == 2) {
            byte MaxCHCmd[] = {0x16, 0x06};
            //key與Var代0代表詢問裝置資訊
            if (Key == 0 && Var == 0) {
                byte DATATemp[] = {0x19, 0x05, (byte) 0xc7, 0x00, 0x10};
                byte[] MaxLite_Temp = ConcatAll(MaxCHCmd, SN, DATATemp);
                byte[] CRCTemp = GetCRC(MaxLite_Temp);
                byte[] SendCMD = ConcatAll(MaxLite_Temp, CRCTemp);
                return SendCMD;
            }
            if (Key == 1) {
                byte[] MaxData = {0x19, 0x05, (byte) 0xa2, 0x00, 0x00};
                byte[] CRCTemp = ConcatAll(MaxCHCmd, SN, MaxData);
                byte[] CRCNum = GetCRC(CRCTemp);
                byte[] SendCMD = ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            } else if (Key == 2) {
                byte[] MaxData = {0x19, 0x05, (byte) 0xa3, 0x00, 0x00};
                byte[] CRCTemp = ConcatAll(MaxCHCmd, SN, MaxData);
                byte[] CRCNum = GetCRC(CRCTemp);
                byte[] SendCMD = ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            } else if (Key == 3) {
                byte[] MaxData = {0x19, 0x05, (byte) 0xa4, 0x00, 0x00};
                byte[] CRCTemp = ConcatAll(MaxCHCmd, SN, MaxData);
                byte[] CRCNum = GetCRC(CRCTemp);
                byte[] SendCMD = ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            } else if (Key == 4) {
                byte[] MaxData = {0x19, 0x05, (byte) 0xa5, 0x00, 0x00};
                byte[] CRCTemp = ConcatAll(MaxCHCmd, SN, MaxData);
                byte[] CRCNum = GetCRC(CRCTemp);
                byte[] SendCMD = ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            }
            //三切
        } else if (Max_Ch_Num == 3) {
            byte MaxCHCmd[] = {0x18, 0x06};
            //key與Var代0代表詢問裝置資訊
            if (Key == 0 && Var == 0) {
                byte DATATemp[] = {0x19, 0x05, (byte) 0xc7, 0x00, 0x10};
                byte[] MaxLite_Temp = ConcatAll(MaxCHCmd, SN, DATATemp);
                byte[] CRCTemp = GetCRC(MaxLite_Temp);
                byte[] SendCMD = ConcatAll(MaxLite_Temp, CRCTemp);
                return SendCMD;
            }
            if (Key == 1) {
                byte[] MaxData = {0x19, 0x05, (byte) 0xa2, 0x00, 0x00};
                byte[] CRCTemp = ConcatAll(MaxCHCmd, SN, MaxData);
                byte[] CRCNum = GetCRC(CRCTemp);
                byte[] SendCMD = ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            } else if (Key == 2) {
                byte[] MaxData = {0x19, 0x05, (byte) 0xa3, 0x00, 0x00};
                byte[] CRCTemp = ConcatAll(MaxCHCmd, SN, MaxData);
                byte[] CRCNum = GetCRC(CRCTemp);
                byte[] SendCMD = ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            } else if (Key == 3) {
                byte[] MaxData = {0x19, 0x05, (byte) 0xa4, 0x00, 0x00};
                byte[] CRCTemp = ConcatAll(MaxCHCmd, SN, MaxData);
                byte[] CRCNum = GetCRC(CRCTemp);
                byte[] SendCMD = ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            } else if (Key == 4) {
                byte[] MaxData = {0x19, 0x05, (byte) 0xa5, 0x00, 0x00};
                byte[] CRCTemp = ConcatAll(MaxCHCmd, SN, MaxData);
                byte[] CRCNum = GetCRC(CRCTemp);
                byte[] SendCMD = ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            } else if (Key == 5) {
                byte[] MaxData = {0x19, 0x05, (byte) 0xa6, 0x00, 0x00};
                byte[] CRCTemp = ConcatAll(MaxCHCmd, SN, MaxData);
                byte[] CRCNum = GetCRC(CRCTemp);
                byte[] SendCMD = ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            } else if (Key == 6) {
                byte[] MaxData = {0x19, 0x05, (byte) 0xa7, 0x00, 0x00};
                byte[] CRCTemp = ConcatAll(MaxCHCmd, SN, MaxData);
                byte[] CRCNum = GetCRC(CRCTemp);
                byte[] SendCMD = ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            }
        }

        if (Max_Ch_Num == 11) {
            byte MaxCHCmd[] = {0x14, 0x06};
            if (Key == 1) {
                byte[] MaxData = {0x19, 0x05, (byte) 0xaa, 0x03, Var};
                byte[] CRCTemp = ConcatAll(MaxCHCmd, SN, MaxData);
                byte[] CRCNum = GetCRC(CRCTemp);
                byte[] SendCMD = ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            }
        } else if (Max_Ch_Num == 12) {
            byte MaxCHCmd[] = {0x16, 0x06};
            if (Key == 1) {
                byte[] MaxData = {0x19, 0x05, (byte) 0xaa, 0x03, Var};
                byte[] CRCTemp = ConcatAll(MaxCHCmd, SN, MaxData);
                byte[] CRCNum = GetCRC(CRCTemp);
                byte[] SendCMD = ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            } else if (Key == 2) {
                byte[] MaxData = {0x19, 0x05, (byte) 0xac, 0x03, Var};
                byte[] CRCTemp = ConcatAll(MaxCHCmd, SN, MaxData);
                byte[] CRCNum = GetCRC(CRCTemp);
                byte[] SendCMD = ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            }
        } else if (Max_Ch_Num == 13) {
            byte MaxCHCmd[] = {0x18, 0x06};
            if (Key == 1) {
                byte[] MaxData = {0x19, 0x05, (byte) 0xaa, 0x03, Var};
                byte[] CRCTemp = ConcatAll(MaxCHCmd, SN, MaxData);
                byte[] CRCNum = GetCRC(CRCTemp);
                byte[] SendCMD = ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            } else if (Key == 2) {
                byte[] MaxData = {0x19, 0x05, (byte) 0xac, 0x03, Var};
                byte[] CRCTemp = ConcatAll(MaxCHCmd, SN, MaxData);
                byte[] CRCNum = GetCRC(CRCTemp);
                byte[] SendCMD = ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            } else if (Key == 3) {
                byte[] MaxData = {0x19, 0x05, (byte) 0xae, 0x03, Var};
                byte[] CRCTemp = ConcatAll(MaxCHCmd, SN, MaxData);
                byte[] CRCNum = GetCRC(CRCTemp);
                byte[] SendCMD = ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            }
        } else if (Max_Ch_Num == 21) {
            byte MaxCHCmd[] = {0x14, 0x06};
            if (Key == 1) {
                byte[] MaxData = {0x19, 0x05, (byte) 0xaa, 0x04, Var};
                byte[] CRCTemp = ConcatAll(MaxCHCmd, SN, MaxData);
                byte[] CRCNum = GetCRC(CRCTemp);
                byte[] SendCMD = ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            }
        } else if (Max_Ch_Num == 22) {
            byte MaxCHCmd[] = {0x16, 0x06};
            if (Key == 1) {
                byte[] MaxData = {0x19, 0x05, (byte) 0xaa, 0x04, Var};
                byte[] CRCTemp = ConcatAll(MaxCHCmd, SN, MaxData);
                byte[] CRCNum = GetCRC(CRCTemp);
                byte[] SendCMD = ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            } else if (Key == 2) {
                byte[] MaxData = {0x19, 0x05, (byte) 0xac, 0x04, Var};
                byte[] CRCTemp = ConcatAll(MaxCHCmd, SN, MaxData);
                byte[] CRCNum = GetCRC(CRCTemp);
                byte[] SendCMD = ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            }
        } else if (Max_Ch_Num == 23) {
            byte MaxCHCmd[] = {0x18, 0x06};
            if (Key == 1) {
                byte[] MaxData = {0x19, 0x05, (byte) 0xaa, 0x04, Var};
                byte[] CRCTemp = ConcatAll(MaxCHCmd, SN, MaxData);
                byte[] CRCNum = GetCRC(CRCTemp);
                byte[] SendCMD = ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            } else if (Key == 2) {
                byte[] MaxData = {0x19, 0x05, (byte) 0xac, 0x04, Var};
                byte[] CRCTemp = ConcatAll(MaxCHCmd, SN, MaxData);
                byte[] CRCNum = GetCRC(CRCTemp);
                byte[] SendCMD = ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            } else if (Key == 3) {
                byte[] MaxData = {0x19, 0x05, (byte) 0xae, 0x04, Var};
                byte[] CRCTemp = ConcatAll(MaxCHCmd, SN, MaxData);
                byte[] CRCNum = GetCRC(CRCTemp);
                byte[] SendCMD = ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            }
        } else if (Max_Ch_Num == 31) {
            byte MaxCHCmd[] = {0x14, 0x06};
            if (Key == 1) {
                byte[] MaxData = {0x19, 0x05, (byte) 0xaa, 0x05, Var};
                byte[] CRCTemp = ConcatAll(MaxCHCmd, SN, MaxData);
                byte[] CRCNum = GetCRC(CRCTemp);
                byte[] SendCMD = ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            }
        } else if (Max_Ch_Num == 32) {
            byte MaxCHCmd[] = {0x16, 0x06};
            if (Key == 1) {
                byte[] MaxData = {0x19, 0x05, (byte) 0xaa, 0x05, Var};
                byte[] CRCTemp = ConcatAll(MaxCHCmd, SN, MaxData);
                byte[] CRCNum = GetCRC(CRCTemp);
                byte[] SendCMD = ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            } else if (Key == 2) {
                byte[] MaxData = {0x19, 0x05, (byte) 0xac, 0x05, Var};
                byte[] CRCTemp = ConcatAll(MaxCHCmd, SN, MaxData);
                byte[] CRCNum = GetCRC(CRCTemp);
                byte[] SendCMD = ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            }
        } else if (Max_Ch_Num == 33) {
            byte MaxCHCmd[] = {0x18, 0x06};
            if (Key == 1) {
                byte[] MaxData = {0x19, 0x05, (byte) 0xaa, 0x05, Var};
                byte[] CRCTemp = ConcatAll(MaxCHCmd, SN, MaxData);
                byte[] CRCNum = GetCRC(CRCTemp);
                byte[] SendCMD = ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            } else if (Key == 2) {
                byte[] MaxData = {0x19, 0x05, (byte) 0xac, 0x05, Var};
                byte[] CRCTemp = ConcatAll(MaxCHCmd, SN, MaxData);
                byte[] CRCNum = GetCRC(CRCTemp);
                byte[] SendCMD = ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            } else if (Key == 3) {
                byte[] MaxData = {0x19, 0x05, (byte) 0xae, 0x05, Var};
                byte[] CRCTemp = ConcatAll(MaxCHCmd, SN, MaxData);
                byte[] CRCNum = GetCRC(CRCTemp);
                byte[] SendCMD = ConcatAll(CRCTemp, CRCNum);
                return SendCMD;
            }
        }


        return "ERROR".getBytes();
    }


    public byte[] ConcatAll(byte[] first, byte[]... rest) {
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


    public byte[] GetCRC(byte[] bytes) {

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

        String str = result.substring(2, 4) + result.substring(0, 2);

        if (str == null) {
            return null;
        }
        if (str.length() == 0) {
            return new byte[0];
        }
        byte[] byteArray = new byte[str.length() / 2];
        for (int x = 0; x < byteArray.length; x++) {
            String subStr = str.substring(2 * x, 2 * x + 2);
            byteArray[x] = ((byte) Integer.parseInt(subStr, 16));
        }
        return byteArray;

    }
}
