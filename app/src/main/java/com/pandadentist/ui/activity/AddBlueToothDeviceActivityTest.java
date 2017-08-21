package com.pandadentist.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pandadentist.R;
import com.pandadentist.listener.OnItemClickListener;
import com.pandadentist.service.UartService;
import com.pandadentist.ui.adapter.BlueToothDeviceAdapter;
import com.pandadentist.ui.base.SwipeRefreshBaseActivity;
import com.pandadentist.util.BLEProtoProcess;
import com.pandadentist.widget.RecycleViewDivider;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;

/**
 * Created by fudaye on 2017/8/17.
 */

public class AddBlueToothDeviceActivityTest extends SwipeRefreshBaseActivity {

    private static final String TAG = AddBlueToothDeviceActivity.class.getSimpleName();

    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int STATE_OFF = 10;
    private static final long SCAN_PERIOD = 5000; //蓝牙扫描时长6秒

    @Bind(R.id.ll_not_found)
    LinearLayout llNotFound;
    @Bind(R.id.ll_loading)
    LinearLayout llLoading;
    @Bind(R.id.rv)
    RecyclerView rv;
    @Bind(R.id.ll_device)
    LinearLayout llDevice;
    @Bind(R.id.iv_loading)
    ImageView ivLoading;


    private int mState = UART_PROFILE_DISCONNECTED;
    private UartService mService = null;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;
    private BLEProtoProcess bleProtoProcess;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private int timecount = 0;
    private int runtype = 0;//0-未运行， 1-接收数据过程， 2-核对丢失帧过程
    private BlueToothDeviceAdapter mAdapter;
    private List<BluetoothDevice> devices = new ArrayList<>();
    private ScanListener mScanListener;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bleProtoProcess = new BLEProtoProcess();
        //开启动画
        Animation circle_anim = AnimationUtils.loadAnimation(this, R.anim.blue_tooth_round_rotate);
        LinearInterpolator interpolator = new LinearInterpolator();  //设置匀速旋转，在xml文件中设置会出现卡顿
        circle_anim.setInterpolator(interpolator);
        ivLoading.startAnimation(circle_anim);
        //检查权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }

        //发送请求
//        findViewById(R.id.btn_reqeust).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                bleProtoProcess.clearLog();
//                mService.writeRXCharacteristic(bleProtoProcess.getRequests());
//            }
//        });

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        // 扫描显示列表
        // 初始化列表
        llLoading.setVisibility(View.VISIBLE);
        llDevice.setVisibility(View.GONE);
        mAdapter = new BlueToothDeviceAdapter(devices);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.addItemDecoration(new RecycleViewDivider(this,LinearLayoutManager.VERTICAL, 1));
        rv.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                String deviceAddress = devices.get(position).getAddress();
                mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
                Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
//                ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName() + " - connecting");
                mService.connect(deviceAddress);
            }
        });
        //扫描附近蓝牙设备
        mScanListener = new ScanListener();
        BluetoothLeScanner bluetoothLeScanner = mBtAdapter.getBluetoothLeScanner();
        bluetoothLeScanner.startScan(mScanListener);
        //扫描6秒后停止扫描
        rv.postDelayed(new Runnable() {
            @Override
            public void run() {
                llLoading.setVisibility(View.VISIBLE);
                llDevice.setVisibility(View.GONE);
                if(devices.size() == 0){
                    llLoading.setVisibility(View.GONE);
                    llDevice.setVisibility(View.GONE);
                    llNotFound.setVisibility(View.VISIBLE);
                }else{
                    llLoading.setVisibility(View.GONE);
                    llDevice.setVisibility(View.VISIBLE);
                }
                bluetoothLeScanner.stopScan(mScanListener);
                mAdapter.setData(devices);
            }
        },SCAN_PERIOD);

        service_init();

    }

    @Override
    public int providerLayoutId() {
        return R.layout.activity_add_blue_tooth_device;
    }


    //UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((UartService.LocalBinder) rawBinder).getService();
            Log.d(TAG, "onServiceConnected mService= " + mService);
            if (!mService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }

        }

        public void onServiceDisconnected(ComponentName classname) {
            ////     mService.disconnect(mDevice);
            mService = null;
        }
    };

    private Handler mHandler = new Handler() {
        @Override

        //Handler events that received from UART service
        public void handleMessage(Message msg) {

        }
    };
    Timer timer = null;
    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            final Intent mIntent = intent;
            //*********************//
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_CONNECT_MSG");
//                        btnConnectDisconnect.setText("Disconnect");
//                        ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName() + " - ready");
                        mState = UART_PROFILE_CONNECTED;
                    }
                });
            }

            //*********************//
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_DISCONNECT_MSG");
//                        btnConnectDisconnect.setText("Connect");
//                        edtMessage.setEnabled(false);
//                        btnSend.setEnabled(false);
//                        ((TextView) findViewById(R.id.deviceName)).setText("Not Connected");
//                        listAdapter.add("[" + currentDateTimeString + "] Disconnected to: " + mDevice.getName());
                        mState = UART_PROFILE_DISCONNECTED;
                        mService.close();
                        //setUiState();

                    }
                });
            }


            //*********************//
            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mService.enableTXNotification();
            }
            //*********************//
            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {
                timecount = 0;
                final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
                int status = bleProtoProcess.interp(txValue);

                switch (status) {
                    case BLEProtoProcess.BLE_DATA_START:
                        runtype = 1;
                        Toast.makeText(AddBlueToothDeviceActivityTest.this, "开始接受数据", Toast.LENGTH_SHORT).show();
                        timer = new Timer();
                        timer.schedule(new DataProcessTimer(), 0, 200);
                        break;
                    case BLEProtoProcess.BLE_DATA_RECEIVER:
                        break;
                    case BLEProtoProcess.BLE_DATA_END:
                        runtype = 2;
                        //timer.cancel();
                        timecount = 100;
                        //if(checkData()) runtype = 0;
                        break;
                    case BLEProtoProcess.BLE_MISSED_RECEIVER:
                        //检测丢帧
                        //timer.cancel();
                        break;
                    case BLEProtoProcess.BLE_MISSED_END:
                        Log.d(TAG, "丢失帧接受完毕");
                        //if(checkData())     runtype = 0;
                        timecount = 100;
                        break;
                    case BLEProtoProcess.BLE_NO_SYNC:
                        //timer.cancel();
                        break;
                }


            }
            //*********************//
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)) {
                showMessage("Device doesn't support UART. Disconnecting");
                mService.disconnect();
            }


        }
    };

    private boolean checkData() {
        try {
            if (bleProtoProcess.checkMissed()) {
                Log.d(TAG, "丢帧");
                byte[] miss = bleProtoProcess.getMissedRequests();
                mService.writeRXCharacteristic(miss);
                return false;
            } else {
                //1.发送请求成功帧  2.把数据交给后台处理
                Log.d(TAG, "数据接收完毕!");
                //mService.writeRXCharacteristic(bleProtoProcess.getCompleted());
                byte[] b = bleProtoProcess.getCompleted();
                Log.d(TAG, "b-->" + Arrays.toString(b));
                mService.writeRXCharacteristic(b);
                Log.d(TAG, "mService-->" + mService.toString());

                //------------发送数据到服务器
                final String base64 = bleProtoProcess.getBuffer();
                Log.d(TAG, "base64-->" + base64);
//                Toast.makeText(AddBlueToothDeviceActivityTest.this, "完整数据接受成功,发送到服务端", Toast.LENGTH_SHORT).show();
                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
//                            String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
//                            mTvLog.setText("[" + currentDateTimeString + "] RX: " + bleProtoProcess.getLog() + "    end..."/*+ "base64-->" + base64*/);
//                                        messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                });

                return true;
            }
        } catch (IllegalAccessException e) {
            Toast.makeText(AddBlueToothDeviceActivityTest.this, "异常", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        return true;
    }

    private void service_init() {
        Intent bindIntent = new Intent(this, UartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        }
        unbindService(mServiceConnection);
        mService.stopSelf();
        mService = null;

    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (!mBtAdapter.isEnabled()) {
            Log.i(TAG, "onResume - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case REQUEST_SELECT_DEVICE:
                //When the DeviceListActivity return, with the selected device address
                if (resultCode == Activity.RESULT_OK && data != null) {
                    String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);

                    Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
                    ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName() + " - connecting");
                    mService.connect(deviceAddress);


                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();

                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                Log.e(TAG, "wrong request code");
                break;
        }
    }


    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onBackPressed() {
        if (mState == UART_PROFILE_CONNECTED) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            showMessage("nRFUART's running in background.\n             Disconnect to exit");
        } else {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.popup_title)
                    .setMessage(R.string.popup_message)
                    .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.popup_no, null)
                    .show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // TODO request success
                }
                break;
        }
    }

    public void copy(String content, Context context) {
// 得到剪贴板管理器
        ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setText(content.trim());
        Toast.makeText(AddBlueToothDeviceActivityTest.this, "复制成功", Toast.LENGTH_LONG).show();
    }

    class DataProcessTimer extends TimerTask {  //1s

        @Override
        public void run() {
            Log.d(TAG, "计时器开始执行" + "count-->" + timecount);
            if (runtype == 0)                            //非接收数据过程，什么也不执行，
            {                                            //可以释放timer
                timecount = 0;
                return;
            } else {
                //1 接收数据    2-核对数据
                if ((runtype == 1 && timecount >= 10) ||
                        (runtype == 2 && timecount >= 4)) {
                    timecount = 0;
                    if (checkData()) {
                        runtype = 0;
                        timer.cancel();
                    }
                }
                timecount++;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    class ScanListener extends ScanCallback {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            String s =result.getDevice().getName();
            if(!TextUtils.isEmpty(result.getDevice().getName())&&!devices.contains(result.getDevice())){
                devices.add(result.getDevice());
                mAdapter.setData(devices);
            }
            Log.d(TAG,"onScanResult"+s);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Log.d(TAG,"onScanResult"+results.size());
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.d(TAG,"onScanResult"+errorCode);
        }
    }
}
