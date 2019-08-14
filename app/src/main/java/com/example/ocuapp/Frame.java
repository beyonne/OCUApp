package com.example.ocuapp;

public class Frame {


    final byte A7SoftwareReset_232CMD = 0x00;
    final byte A7SoftUpdate_232CMD = 0x01;
    public final byte A7SoftWareVer_232CMD = 0x05;
    final byte A7HardVerSet_CMD = 0x06;
    final byte A7InPutCheck_232CMD = 0x07;
    final byte A7OutPutCheck_232CMD = 0x08;
    final byte A7OutPutSet_232CMD = 0x09;
    final byte A7BrdImfCheck_232CMD = 0x0A;
    final byte A7PowerOffReboot_232CMD = 0x0B;
    final byte A7CarLED_232CMD = 0x10;
    final byte A7CameraSplitScreen_232CMD = 0x11;
    final byte A7WeaponControl_232CMD = 0x12;
    final byte A7PTZControl_232CMD = 0x13;
    final byte A7GrabCameraSet_232CMD = 0x14;
    final byte A7BoardImfCheck_232CMD = 0x15;
    final byte A7BatDetailImfCheck_232CMD = 0x16;
    final byte A7YuLiu_232CMD = 0x17;
    final byte A7PosSpdCheck_232CMD = 0x18;
    final byte A7PosSet_232CMD = 0x19;
    final byte A7SpdSet_232CMD = 0x1A;
    final byte A70Calibration_232CMD = 0x1B;
    final byte A7MoveLock_232CMD = 0x1C;
    final byte A7OpenCloseTimer_232CMD = 0x1D;

    private final int FRAME_MAX_LENGTH = 100;
    //    private final byte Framestart = (byte)0xA5;
//    private final byte DevType = (byte)0xC0;
    private final byte Framestart = (byte)0xA6;
    private final byte DevType = (byte)0xB1;
    private final byte SrcAddr = 0x02;
    private final byte DesAddr = 0x01;
    private int Length;
    public byte Cmd;
    private int Crcd;
    private byte State;
    private byte Index;
    public byte[] pData;
    public boolean Frame_OK;

    public Frame() {
        pData = new byte[FRAME_MAX_LENGTH];
        Length = 0;
        Cmd = 0;
        State = 0;
        Index = 0;
        Frame_OK = false;
    }

    public boolean Frame_Pro(byte dat)
    {
        switch (State)
        {
            case 0://起始符
                if (dat == Framestart)
                {
                    Index = 0;
                    pData[Index++] = dat;
                    State = 1;
                }
                break;
            case 1://设备类型
                if (dat == DevType)
                {
                    pData[Index++] = dat;
                    State = 2;
                }
                break;
            case 2://原地址
                if (dat == SrcAddr)
                {
                    pData[Index++] = dat;
                    State = 3;
                }
                break;
            case 3://目标地址
                if (dat == DesAddr)
                {
                    pData[Index++] = dat;
                    State = 4;
                }
                break;
            case 4://长度域高８位
                State = 5;
                Length = (dat<<24);
                pData[Index++] = dat;
                break;
            case 5://长度域高８位
                State = 6;
                Length |= (dat<<16);
                pData[Index++] = dat;
                break;
            case 6://长度域高８位
                State = 7;
                Length |= (dat<<8);
                pData[Index++] = dat;
                break;
            case 7://长度域低８位
                State = 8;
                Length |= dat;
                pData[Index++] = dat;
                break;
            case 8://命令字
                Cmd = dat;
                pData[Index++] = dat;
                if (Length==0)
                {
                    State = 10;
                }
                else
                {
                    State = 9;
                }
                break;
            case 9://数据域
                if (Index<(Length+9))
                {
                    pData[Index++] = dat;
                    if (Index >= (Length+9))
                    {
                        State = 10;
                    }
                }
                break;
            case 10://CRC高８位
                State = 11;
                Crcd = ((int)dat<<8);
                Crcd &= (int)0xFFFF;
                break;
            case 11:
                Crcd |= (0x00FF&dat);
                int CRC_Tem = Crcd;
                Frame_OK = CRC.CRC16Check(pData,Index,CRC_Tem);
                State = 0;
                Index = 0;
                break;
            default:
                break;
        }
        return Frame_OK;
    }
}
