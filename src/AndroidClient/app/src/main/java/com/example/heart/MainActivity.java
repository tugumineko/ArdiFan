package com.example.heart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    LinearLayout ll_start,ll_stop;

    // 获取到蓝牙适配器
    private BluetoothAdapter mBluetoothAdapter;
    // UUID，蓝牙建立链接需要的
    private final UUID MY_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");

    // 选中发送数据的蓝牙设备，全局变量，否则连接在方法执行完就结束了
    private BluetoothDevice selectDevice;
    // 获取到选中设备的客户端串口，全局变量，否则连接在方法执行完就结束了
    private BluetoothSocket clientSocket;
    // 获取到向设备写的输出流，全局变量，否则连接在方法执行完就结束了
    public static InputStream is;
    public static OutputStream os;
    private String receivedData="";
    private Thread thread;
    private volatile boolean running = true; // 线程运行控制标志

    private TextView tv_1,tv_2;
    private static final int PERMISSION_REQUEST_CODE = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ll_start = findViewById(R.id.ll_start);
        ll_stop = findViewById(R.id.ll_stop);


        tv_1 = findViewById(R.id.textView1);
        tv_2 = findViewById(R.id.textView2);



        ll_start.setOnClickListener(view -> {
            Toast.makeText(this, "开始连接", Toast.LENGTH_SHORT).show();
            init蓝牙();
        });
        ll_stop.setOnClickListener(view -> {
            running = false; // 安全地停止线程
        });
        // 检查并申请权限
        if (!hasRequiredPermissions()) {
            requestRequiredPermissions();
        }

        findViewById(R.id.open).setOnClickListener(v -> {
            if (os == null) {
                Toast.makeText(this, "请先连接", Toast.LENGTH_SHORT).show();
                return;
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        os.write("on".getBytes());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
            Toast.makeText(this, "发送成功", Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.close).setOnClickListener(v -> {
            if (os == null) {
                Toast.makeText(this, "请先连接", Toast.LENGTH_SHORT).show();
                return;
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        os.write("off".getBytes());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
            Toast.makeText(this, "发送成功", Toast.LENGTH_SHORT).show();
        });
    }
    private boolean hasRequiredPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestRequiredPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[] {
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                },
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                // 所有权限都已授予，执行需要权限的操作
            } else {
                // 权限被拒绝，处理拒绝情况
            }
        }
    }
    private void init蓝牙() {
        // 获取到蓝牙默认的适配器
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // 对其进行分割，获取到这个设备的地址
        String address = "98:D3:61:F9:4F:B8";
        // 判断当前是否还是正在搜索周边设备，如果是则暂停搜索
        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            //判断是否正在搜索
        }
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        // 如果选择设备为空则代表还没有选择设备
        if (selectDevice == null) {
            //通过地址获取到该设备
            selectDevice = mBluetoothAdapter.getRemoteDevice(address);
        }
        // 这里需要try catch一下，以防异常抛出
        try {
            // 判断客户端接口是否为空
            if (clientSocket == null) {
                clientSocket = selectDevice
                        .createRfcommSocketToServiceRecord(MY_UUID);
                // 向服务端发送连接
                clientSocket.connect();
                // 获取到输出流，向外写数据
                os = clientSocket.getOutputStream();

                // 获取到输入流，用于接收数据
                is = clientSocket.getInputStream();


            }
            // 吐司一下，告诉用户成功
            if (os != null){
                Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                running = true; // 安全地开始线程
                thread = new GetData();
                thread.start();
            }else {
                Toast.makeText(MainActivity.this, "建立连接失败", Toast.LENGTH_SHORT).show();
            }

        } catch (IOException e) {
            e.printStackTrace();
            // 如果发生异常则告诉用户发送失败
            Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
        }
    }
    class GetData extends Thread {
        @Override
        public void run() {
            while (running) {
                try {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    StringBuilder stringBuilder = new StringBuilder();

                    while ((bytesRead = is.read(buffer)) != -1) {
                        receivedData = new String(buffer, 0, bytesRead);
                        stringBuilder.append(receivedData);
                        //System.out.println(receivedData);
                        if (!running)break;
                        if(receivedData!=null){
                            if (receivedData.contains("\n"))
                                receivedData = receivedData.split("\n")[0];
                            if (receivedData.contains("on")||receivedData.contains("of")){
                                String[] a = receivedData.trim().split(" ");
                                for (int i = 0; i < a.length; i++) {
                                    if (a[i].contains("on")||a[i].contains("of")){
                                        tv_2.setText("运行状态: "+a[1]);
                                    }else {
                                        if (!a[i].isEmpty()){
                                            tv_1.setText("检测距离: "+a[0]+" cm");
                                        }
                                    }
                                }
                                System.out.println("data:"+receivedData);
                            }
                        }
                    }
                } catch (IOException e) {
                   // e.printStackTrace();
                    if (!running) {
                        // 如果线程是因为调用 stopRunning 而中断的，则退出循环
                        break;
                    }

                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
        }
    }


    public static String getCurrentTimeString() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 获取当前日期和时间
            LocalDateTime now = null;
            now = LocalDateTime.now();
            // 定义日期时间格式
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            // 将当前日期时间格式化为字符串
            return now.format(formatter);
        }else {
            return "2023-12-09 16:06:34";
        }
    }

}