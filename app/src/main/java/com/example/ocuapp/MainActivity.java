package com.example.ocuapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity {

    Frame RecvFrame = new Frame();
    boolean isConnect = false;
    TextView viewtxt;
    Button ConnectButton;//定义连接按钮
    Button SendButton;//定义发送按钮
    EditText IPEditText;//定义ip输入框
    EditText PortText;//定义端口输入框
    EditText SendEditText;//定义信息输出框
    EditText RecvEditText;//定义信息输入框
    TextView TVRecvData;
    Socket socket = null;//定义socket
    private OutputStream outputStream = null;//定义输出流
    private InputStream inputStream = null;//定义输入流
    private PrintWriter pw;

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
        RecvEditText = (EditText)findViewById(R.id.Recv_EditText);//获得接收消息文本框对象
        TVRecvData = (TextView) findViewById(R.id.tv_recvdata);
        Log.i("chushihua", "onCreate: 开始");
        viewtxt = (TextView)findViewById(R.id.textview1);
        SendLen = 0;
        SendByteArray = new byte[12];
    }

    public void ConnectBtn_clickHander(View v)
    {
        if (isConnect==false)
        {
//            ConnectButton.setText("断开");
            Log.i("连接", "创建连接线程");
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
                    SendLen=0;
                    SendByteArray[SendLen++] = (byte)0xA6;
                    SendByteArray[SendLen++] = (byte)0xB1;
                    SendByteArray[SendLen++] = (byte)0x01;
                    SendByteArray[SendLen++] = (byte)0x02;
                    SendByteArray[SendLen++] = (byte)0x00;
                    SendByteArray[SendLen++] = (byte)0x00;
                    SendByteArray[SendLen++] = (byte)0x00;
                    SendByteArray[SendLen++] = (byte)0x01;
                    SendByteArray[SendLen++] = (byte)0x05;
                    SendByteArray[SendLen++] = (byte)0xB1;
                    SendByteArray[SendLen++] = (byte)0x56;
                    SendByteArray[SendLen++] = (byte)0x1A;
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
                                                Toast.makeText(MainActivity.this, "接收到完整数据帧", Toast.LENGTH_SHORT).show();
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
                                            TVRecvData.append(sb.toString().toUpperCase());
                                            RecvEditText.append(sb.toString().toUpperCase());
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

    private Boolean OpenFlag=false;

    public void CloseBtn_clickHander(View v)
    {
        TextView txt = (TextView)findViewById(R.id.textview1);
        Button CloseBtn = (Button)findViewById(R.id.CloseBtn);
        if (OpenFlag)
        {
            txt.setText("Open Beyonne");
            CloseBtn.setText("奔驰");
        }
        else
        {
            txt.setText("Close Bruce");
            CloseBtn.setText("宝马");
        }
        OpenFlag = !OpenFlag;
    }
}
