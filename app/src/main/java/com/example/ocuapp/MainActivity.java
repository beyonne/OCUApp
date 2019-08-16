package com.example.ocuapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    Frame RecvFrame = new Frame();
    boolean isConnect = false;
    TextView viewtxt;
    Button ConnectButton;//定义连接按钮
    Button SendButton;//定义发送按钮
    EditText IPEditText;//定义ip输入框
    EditText PortText;//定义端口输入框
    EditText SendEditText;//定义信息输出框
//    EditText RecvEditText;//定义信息输入框
    TextView TVRecvData;
    Socket socket = null;//定义socket
    private OutputStream outputStream = null;//定义输出流
    private InputStream inputStream = null;//定义输入流
//    private PrintWriter pw;
    //虚拟摇杆
    private MyRockerView mRockerViewXY;
    TextView TV_X;
    TextView TV_Y;
    int PosX;
    int PosY;
    int PosXLast;
    int PosYLast;
    int XYRate;
    String xytemp;
    private Timer mTimer = null;
    private TimerTask mTimerTask = null;
    private Handler mHandler = null;
    private static int count = 0;
//    private boolean isPause = false;
    private boolean isStop = true;
    private static int delay = 500; //1s
    private static int period = 500; //1s
    private static final int UPDATE_TEXTVIEW = 0;
    private Button btTimerOpenClose = null;

    byte[] SendByteArray;
    int SendLen;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ConnectButton = (Button)findViewById(R.id.ConnectBtn);//获得按钮对象
        SendButton = (Button)findViewById(R.id.SendBtn);//获得按钮对象
        IPEditText = (EditText)findViewById(R.id.IP_EditText);//获得ip文本框对象
        PortText = (EditText)findViewById(R.id.Port_EditText);//获得端口文本框按钮对象
        SendEditText = (EditText)findViewById(R.id.Send_EditText);//获得发送消息文本框对象
        TVRecvData = (TextView) findViewById(R.id.tv_recvdata);
        Log.i("chushihua", "onCreate: 开始");
        viewtxt = (TextView)findViewById(R.id.textview1);
        int count = 0;
        SendLen = 0;
        SendByteArray = new byte[50];

        btTimerOpenClose = findViewById(R.id.TimerOpenCloseBtn);

        mRockerViewXY = (MyRockerView) findViewById(R.id.rockerXY_View);//8方向
        TV_X = findViewById(R.id.textviewX);
        TV_Y = findViewById(R.id.textviewY);
        XYRate = 32768/340;//保证坐标范围在-32768~32767之间
        //摇杆点击事件
        initMyClick();
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case UPDATE_TEXTVIEW:
                        updateTextView();
                        default:
                            break;
                }
                super.handleMessage(msg);
            }
        };
    }

    //摇杆点击事件
    private void initMyClick() {
        //xy轴
        //方向
        mRockerViewXY.setOnShakeListener(MyRockerView.DirectionMode.DIRECTION_8, new MyRockerView.OnShakeListener() {
            @Override
            public void onStart() {
            }

            @Override
            public void direction(MyRockerView.Direction direction) {
            }

            @Override
            public void onFinish() {
            }
        });
        //角度
        mRockerViewXY.setOnAngleChangeListener(new MyRockerView.OnAngleChangeListener() {
            @Override
            public void onStart() {
            }

            @Override
            public void angle(double angle) {
            }

            public void onDistanceShow(int xpos,int ypos)
            {
                PosX = xpos*XYRate;
                PosY = ypos*XYRate;
                xytemp = ("X轴坐标："+PosX);
                TV_X.setText(xytemp);
                xytemp = ("Y轴坐标："+PosY);
                TV_Y.setText(xytemp);
            }

            @Override
            public void onFinish(int xpos,int ypos) {
                PosX = xpos*XYRate;
                PosY = ypos*XYRate;
                xytemp = ("X轴坐标："+PosX);
                TV_X.setText(xytemp);
                xytemp = ("Y轴坐标："+PosY);
                TV_Y.setText(xytemp);
//                if(PosXLast != PosX || PosYLast != PosY)
//                {
//                    PosXLast = PosX;
//                    PosYLast = PosY;
////                    if (socket!=null)
////                    {
////                        if (socket.isConnected())
////                        {
////                            if (!socket.isOutputShutdown())
////                            {
////                                byte[] SendBuff = new byte[]{(byte)0xA5,(byte)0xC0,0x01,0x02,0x00,0x18,0x1A,0x00,0x03,0x07,0x01,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,(byte)0xCE,0x6F};
////
////                                SendBuff[11] = (byte)(PosX>>8);
////                                SendBuff[12] = (byte)PosX;
////                                SendBuff[13] = (byte)(PosY>>8);
////                                SendBuff[14] = (byte)PosY;
////                                int crctem = CRC.getCRC1021(SendBuff,31);
////                                SendBuff[31] = (byte)(crctem>>8);
////                                SendBuff[32] = (byte)crctem;
////
////                                SendLen=0;
////                                for (int i=0;i<SendBuff.length;i++)
////                                {
////                                    SendByteArray[SendLen++] = SendBuff[i];
////                                }
////                                Send_Thread send_Thread = new Send_Thread();
////                                send_Thread.start();
////                            }
////                        }
////                        else
////                        {
////                            Log.i("未连接", "Socket 未连接，请先连接！");
////                            Toast.makeText(MainActivity.this,"未建立连接，请先建立连接！",Toast.LENGTH_SHORT).show();//需要开启手机通知推送
////                        }
////                    }
//                }
            }
        });
        //级别
        mRockerViewXY.setOnDistanceLevelListener(new MyRockerView.OnDistanceLevelListener() {
            @Override
            public void onDistanceLevel(int level) {
//                levelXY = ("当前距离级别："+level);
//                Log.e(TAG, "XY轴"+levelXY);
//                levelXY_Text.setText(levelXY);
            }
        });
    }


    public void TimerOpenCloseBtn_clickHander(View v)
    {
        if (isStop)
        {
            isStop = false;
            startTimer();
            btTimerOpenClose.setText("关闭定时器");
        }
        else
        {
            isStop = true;
            stopTimer();
            btTimerOpenClose.setText("开启定时器");
        }
    }

    private void startTimer(){
        if (mTimer == null)
        {
            mTimer = new Timer();
        }
        if (mTimerTask == null) {
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    sendMessage(UPDATE_TEXTVIEW);
                }
            };
        }

        if(mTimer != null && mTimerTask != null )
            mTimer.schedule(mTimerTask, delay, period);
    }
    private void stopTimer(){
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
        count = 0;
    }
    public void sendMessage(int id){
        if (mHandler != null) {
            Message message = Message.obtain(mHandler, id);
            mHandler.sendMessage(message);
        }
    }

    private void updateTextView(){
        count++;
        viewtxt.setText(String.valueOf(count));
        if (socket!=null)
        {
            if (socket.isConnected())
            {
                if (!socket.isOutputShutdown())
                {
                    byte[] SendBuff = new byte[]{(byte)0xA5,(byte)0xC0,0x01,0x02,0x00,0x18,0x1A,0x00,0x03,0x07,0x01,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,(byte)0xCE,0x6F};

                    SendBuff[9] = 0x0C;
                    SendBuff[11] = (byte)(PosY>>8);
                    SendBuff[12] = (byte)PosY;
                    SendBuff[13] = (byte)(PosX>>8);
                    SendBuff[14] = (byte)PosX;
                    int crctem = CRC.getCRC1021(SendBuff,31);
                    SendBuff[31] = (byte)(crctem>>8);
                    SendBuff[32] = (byte)crctem;

                    SendLen=0;
                    for (int i=0;i<SendBuff.length;i++)
                    {
                        SendByteArray[SendLen++] = SendBuff[i];
                    }
                    Send_Thread send_Thread = new Send_Thread();
                    send_Thread.start();
                }
            }
            else
            {
                Log.i("未连接", "Socket 未连接，请先连接！");
                Toast.makeText(MainActivity.this,"未建立连接，请先建立连接！",Toast.LENGTH_SHORT).show();//需要开启手机通知推送
            }
        }
    }

    public void ConnectBtn_clickHander(View v)
    {
        if (isConnect==false)
        {
//            ConnectButton.setText("断开");
//            Log.i("连接", "创建连接线程");
            //启动连接线程
            Connect_Thread connect_Thread = new Connect_Thread();
            connect_Thread.start();
            Log.i("连接", "ConnectBtn_clickHander: 连接");
        }
        else
        {
//            ConnectButton.setText("连接");
            try
            {
                if (socket!=null)
                {
                    if (socket.isConnected())
                    {
                        Log.i("断开", "ConnectBtn_clickHander: 断开");
                        socket.close();
                        viewtxt.setText("连接断开！");
                        ConnectButton.setText("连接");
                        socket = null;
                        isConnect = false;
                    }
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void SendBtn_clickHander(View v)
    {
//        Log.i("发送按钮", "SendBtn_clickHander: 触发发送按钮");
//        Toast.makeText(MainActivity.this,"发送按钮按下！",Toast.LENGTH_SHORT).show();//需要开启手机通知推送
        try
        {
            if (socket!=null) {
                if (socket.isConnected()) {
                    if (!socket.isOutputShutdown()) {
                        outputStream = socket.getOutputStream();
                        outputStream.write(SendEditText.toString().getBytes());
                        Toast.makeText(MainActivity.this, "已发送", Toast.LENGTH_SHORT).show();
//                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
//                    dos.write(SendEditText.toString().getBytes());
                    }
                }
                else
                {
                    Toast.makeText(MainActivity.this,"未建立连接，请先建立连接！",Toast.LENGTH_SHORT).show();
                }
            }
            else
            {
                Toast.makeText(MainActivity.this,"未建立连接，请先建立连接！",Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e)
        {
            Toast.makeText(MainActivity.this,"发送抛出异常",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void CheckVerBtn_clickHander(View v)
    {
        if (socket!=null)
        {
            if (socket.isConnected())
            {
                if (!socket.isOutputShutdown())
                {
                    //A5 C0 01 02 00 02 05 C0 00 47 3E（查询A7主控板）
//                    int[] SendBuff = new int[]{0xA5,0xC0,0x01,0x02,0x00,0x02,0x05,0xC0,0x00,0x47,0x3E};
                    //A6 B1 01 02 00 00 00 01 05 B1 56 1A查询避障检测板
//                    int[] SendBuff = new int[]{0xA6,0xB1,0x01,0x02,0x00,0x00,0x00,0x01,0x05,0xB1,0x56,0x1A};
                    //A5 C0 01 02 00 02 05 C0 00 47 3E
//                    int[] SendBuff = new int[]{0xA5, 0xC0, 0x01, 0x02, 0x00, 0x02, 0x05, 0xC0, 0x00, 0x47, 0x3E};
                    SendLen=0;
                    SendByteArray[SendLen++] = (byte)0xA5;
                    SendByteArray[SendLen++] = (byte)0xC0;
                    SendByteArray[SendLen++] = (byte)0x01;
                    SendByteArray[SendLen++] = (byte)0x02;
                    SendByteArray[SendLen++] = (byte)0x00;
                    SendByteArray[SendLen++] = (byte)0x02;
                    SendByteArray[SendLen++] = (byte)0x05;
                    SendByteArray[SendLen++] = (byte)0xC0;
                    SendByteArray[SendLen++] = (byte)0x00;
                    SendByteArray[SendLen++] = (byte)0x47;
                    SendByteArray[SendLen++] = (byte)0x3E;
                    Send_Thread send_Thread = new Send_Thread();
                    send_Thread.start();
                }
            }
            else
            {
                Log.i("未连接", "Socket 未连接，请先连接！");
                Toast.makeText(MainActivity.this,"未建立连接，请先建立连接！",Toast.LENGTH_SHORT).show();//需要开启手机通知推送
            }
        }
        else
        {
            Toast.makeText(MainActivity.this,"未建立连接，请先建立连接！",Toast.LENGTH_SHORT).show();
        }
    }

    // 发送线程
    class Send_Thread extends Thread
    {
        public void run()
        {
            try {
                outputStream = socket.getOutputStream();
                outputStream.write(SendByteArray,0,SendLen);
            } catch (Exception e) {
                Toast.makeText(MainActivity.this,"发送抛出异常",Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    class Connect_Thread extends Thread
    {
        public void run()
        {
            try
            {
                if (socket==null)//如果已经连接上了，就不再执行连接程序
                {
                    InetAddress ipAddress = InetAddress.getByName(IPEditText.getText().toString());
                    int port = Integer.valueOf(PortText.getText().toString());
                    socket = new Socket(ipAddress,port);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            if (socket!=null) {
                if (socket.isConnected()) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            viewtxt.setText("连接成功！");
                            ConnectButton.setText("断开");
                            try {
                                socket.setSoTimeout(5000);
                                isConnect = socket.isConnected();
//                                outputStream = socket.getOutputStream();
//                                pw = new PrintWriter(socket.getOutputStream(),true);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }

                            Log.i("连接成功", "创建接收线程");
                            Toast.makeText(MainActivity.this,"连接成功",Toast.LENGTH_SHORT).show();
                        }
                    });

                    //在创建完连接后启动接收线程
                    Receive_Thread receive_Thread = new Receive_Thread();
                    receive_Thread.start();
                }
                else
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            viewtxt.setText("网络连接失败！");
                            Log.i("连接失败", "连接失败，请检查网络!");
                        }
                    });
                }
            }
            else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        viewtxt.setText("Socket创建失败！");
                        Log.i("连接失败", "连接失败，请检查网络!");
                    }
                });
            }
        }
    }

    class Receive_Thread extends Thread
    {
        public void run()//重写
        {
            try
            {
                while (true)
                {
                    if (socket!=null)
                    {
                        if (!socket.isClosed())//如果服务器没有关闭
                        {
                            if (socket.isConnected())//连接正常
                            {
                                if (!socket.isInputShutdown())//如果输入流没有断开
                                {
                                    final byte[] buffer = new byte[512];
                                    inputStream = socket.getInputStream();
                                    final int len = inputStream.read(buffer);//数据读出来，并且返回数据的长度
                                    if (len<=0)
                                    {
                                        continue;
                                    }
                                    else
                                    {
                                        RecvFrame.Frame_OK = false;
                                        for (int i=0;i<len;i++)
                                        {
                                            if (RecvFrame.Frame_Pro(buffer[i]))
                                            {
                                                break;
                                            }
                                        }
                                    }
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (RecvFrame.Frame_OK) {
//                                                Toast.makeText(MainActivity.this, "接收到完整数据帧", Toast.LENGTH_SHORT).show();
                                                Cmd_Pro();
                                                RecvFrame.Frame_OK = false;
                                            }
                                            //界面显示
                                            String str = "";
                                            StringBuilder sb = new StringBuilder("");
                                            for (int i=0;i<len;i++)
                                            {
                                                str = Integer.toHexString(buffer[i] & 0xFF);
                                                sb.append((str.length()==1)? ("0"+str) : str);
                                                sb.append(" ");
                                            }
                                            TVRecvData.setText(sb.toString().toUpperCase());
//                                            TVRecvData.append(sb.toString().toUpperCase());
//                                            RecvEditText.append(sb.toString().toUpperCase());
                                        }
                                    });
                                }
                            }
                            else
                            {
                                break;
                            }
                        }
                        else
                        {
                            break;
                        }
                    }
                    else
                    {
                        break;
                    }
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void Cmd_Pro()
    {
        switch (RecvFrame.Cmd)
        {
            case 0x00://A7SoftwareReset_232CMD
                break;
            case 0x01://A7SoftUpdate_232CMD
                break;
            case 0x05://A7SoftWareVer_232CMD
                Toast.makeText(MainActivity.this, "收到版本号信息", Toast.LENGTH_SHORT).show();
                break;
            case 0x0A://A7BrdImfCheck_232CMD
                break;
            case 0x0B://A7PowerOffReboot_232CMD
                break;
        }
    }
}
