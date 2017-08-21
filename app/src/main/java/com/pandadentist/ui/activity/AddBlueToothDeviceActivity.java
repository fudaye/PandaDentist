package com.pandadentist.ui.activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pandadentist.R;
import com.pandadentist.service.UartService;
import com.pandadentist.ui.base.SwipeRefreshBaseActivity;
import com.pandadentist.util.BLEProtoProcess;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by fudaye on 2017/8/17.
 */

public class AddBlueToothDeviceActivity extends SwipeRefreshBaseActivity {
    private static final String TAG = AddBlueToothDeviceActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final long SCAN_PERIOD = 10000;
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int STATE_OFF = 10;
    private int timecount = 0;
    private int runtype = 0;//0-未运行， 1-接收数据过程， 2-核对丢失帧过程


    @Bind(R.id.ll_loading)
    LinearLayout llLoading;
    @Bind(R.id.new_devices)
    ListView newDevices;
    @Bind(R.id.ll_device)
    LinearLayout llDevice;
    @Bind(R.id.iv_loading)
    ImageView ivLoading;
    @Bind(R.id.ll_not_found)
    LinearLayout llNotFound;


    private BluetoothAdapter mBtAdapter = null;
    List<BluetoothDevice> deviceList;
    Map<String, Integer> devRssiValues;
    private DeviceAdapter deviceAdapter;
    private Handler mHandler;
//    private boolean mScanning;
    private BluetoothDevice mDevice = null;
    private UartService mService = null;
    private BLEProtoProcess bleProtoProcess;
//    private int mState = UART_PROFILE_DISCONNECTED;


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolBarTtitle.setText("连接蓝牙");
        Animation circle_anim = AnimationUtils.loadAnimation(this, R.anim.blue_tooth_round_rotate);
        LinearInterpolator interpolator = new LinearInterpolator();  //设置匀速旋转，在xml文件中设置会出现卡顿
        circle_anim.setInterpolator(interpolator);
        ivLoading.startAnimation(circle_anim);

        //开始动画
        //请求蓝牙权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }
        bleProtoProcess = new BLEProtoProcess();
        mHandler = new Handler();
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBtAdapter = bluetoothManager.getAdapter();
//        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        llLoading.setVisibility(View.VISIBLE);
        llDevice.setVisibility(View.GONE);
        service_init();

    }


    public void checkBleDevice(Context context) {
        if (mBtAdapter != null) {
            if (!mBtAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                enableBtIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(enableBtIntent);
            }
        } else {
            Log.i("blueTooth", "该手机不支持蓝牙");
        }
    }

    @Override
    public int providerLayoutId() {
        return R.layout.activity_add_blue_tooth_device;
    }

    @OnClick(R.id.btn)
    public void onViewClicked() {
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void populateList() {
        /* Initialize device list container */
        Log.d(TAG, "populateList");
        deviceList = new ArrayList<BluetoothDevice>();
        deviceAdapter = new DeviceAdapter(this, deviceList);
        devRssiValues = new HashMap<String, Integer>();

        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(deviceAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        scanLeDevice(true);

    }


    class DeviceAdapter extends BaseAdapter {
        Context context;
        List<BluetoothDevice> devices;
        LayoutInflater inflater;

        public DeviceAdapter(Context context, List<BluetoothDevice> devices) {
            this.context = context;
            inflater = LayoutInflater.from(context);
            this.devices = devices;
        }

        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public Object getItem(int position) {
            return devices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewGroup vg;

            if (convertView != null) {
                vg = (ViewGroup) convertView;
            } else {
                vg = (ViewGroup) inflater.inflate(R.layout.device_element, null);
            }

            BluetoothDevice device = devices.get(position);
            final TextView tvadd = ((TextView) vg.findViewById(R.id.address));
            final TextView tvname = ((TextView) vg.findViewById(R.id.name));
            final TextView tvpaired = (TextView) vg.findViewById(R.id.paired);
            final TextView tvrssi = (TextView) vg.findViewById(R.id.rssi);

            tvrssi.setVisibility(View.VISIBLE);
            byte rssival = (byte) devRssiValues.get(device.getAddress()).intValue();
            if (rssival != 0) {
                tvrssi.setText("Rssi = " + String.valueOf(rssival));
            }

            tvname.setText(device.getName());
            tvadd.setText(device.getAddress());
            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                Log.i(TAG, "device::" + device.getName());
                tvname.setTextColor(Color.BLACK);
                tvadd.setTextColor(Color.BLACK);
                tvpaired.setTextColor(Color.BLACK);
                tvpaired.setVisibility(View.VISIBLE);
                tvpaired.setText("配对");
                tvrssi.setVisibility(View.VISIBLE);
                tvrssi.setTextColor(Color.BLACK);

            } else {
                tvname.setTextColor(Color.BLACK);
                tvadd.setTextColor(Color.BLACK);
                tvpaired.setVisibility(View.GONE);
                tvrssi.setVisibility(View.VISIBLE);
                tvrssi.setTextColor(Color.BLACK);
            }
            return vg;
        }
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            BluetoothDevice device = deviceList.get(position);
            mBtAdapter.stopLeScan(mLeScanCallback);

            // 设备点击事件
            Bundle b = new Bundle();
            b.putString(BluetoothDevice.EXTRA_DEVICE, deviceList.get(position).getAddress());

            String deviceAddress = deviceList.get(position).getAddress();
            mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);

            Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
//            ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName() + " - connecting");
            mService.connect(deviceAddress);

        }
    };
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    addDevice(device, rssi);
                                }
                            });

                        }
                    });
                }
            };

    private void addDevice(BluetoothDevice device, int rssi) {
        boolean deviceFound = false;

        for (BluetoothDevice listDev : deviceList) {
            if (listDev.getAddress().equals(device.getAddress())) {
                deviceFound = true;
                break;
            }
        }


        devRssiValues.put(device.getAddress(), rssi);
        if (!deviceFound) {
            deviceList.add(device);
            llLoading.setVisibility(View.GONE);
            llDevice.setVisibility(View.VISIBLE);

            deviceAdapter.notifyDataSetChanged();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                @Override
                public void run() {
//                    mScanning = false;
                    mBtAdapter.stopLeScan(mLeScanCallback);

                }
            }, SCAN_PERIOD);

//            mScanning = true;
            mBtAdapter.startLeScan(mLeScanCallback);
        } else {
//            mScanning = false;
            mBtAdapter.stopLeScan(mLeScanCallback);
        }

    }

    //UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((UartService.LocalBinder) rawBinder).getService();
            Log.d(TAG, "onServiceConnected mService= " + mService);
            if (mService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                //TODO service 初始化成功  向蓝牙发送请求数据信息
                bleProtoProcess.clearLog();
                populateList();
            }else{
                finish();
            }
        }

        public void onServiceDisconnected(ComponentName classname) {
            ////     mService.disconnect(mDevice);
            mService = null;
        }
    };

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

    Timer timer = null;
    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
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
//                        mState = UART_PROFILE_CONNECTED;
                        //TODO 绑定成功发送数据
                        bleProtoProcess.clearLog();
                        mService.writeRXCharacteristic(bleProtoProcess.getRequests());
                    }
                });
            }

            //*********************//
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
                runOnUiThread(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_DISCONNECT_MSG");
//                        btnConnectDisconnect.setText("Connect");
//                        edtMessage.setEnabled(false);
//                        btnSend.setEnabled(false);
//                        ((TextView) findViewById(R.id.deviceName)).setText("Not Connected");
//                        listAdapter.add("[" + currentDateTimeString + "] Disconnected to: " + mDevice.getName());
//                        mState = UART_PROFILE_DISCONNECTED;
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
                        Toast.makeText(AddBlueToothDeviceActivity.this, "开始接受数据", Toast.LENGTH_SHORT).show();
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
                        Log.d(TAG,"丢失帧接受完毕");
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

    class DataProcessTimer extends TimerTask {  //1s

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void run() {
            Log.d(TAG,"计时器开始执行"+"count-->"+timecount);
            if (runtype == 0)                            //非接收数据过程，什么也不执行，
            {                                            //可以释放timer
                timecount = 0;
                return;
            } else {
                //1 接收数据    2-核对数据
                if(     (runtype == 1 && timecount >= 10 )   ||
                        (runtype == 2 && timecount >= 4) )
                {
                    timecount = 0;
                    if(checkData()) {
                        runtype = 0;
                        timer.cancel();
                    }
                }
                timecount++;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private boolean checkData() {
        try {
            if (bleProtoProcess.checkMissed()) {
                Log.d(TAG,"丢帧");
                byte[] miss = bleProtoProcess.getMissedRequests();
                mService.writeRXCharacteristic(miss);
                return false;
            }
            else {
                //1.发送请求成功帧  2.把数据交给后台处理
                Log.d(TAG,"数据接收完毕!");
                //mService.writeRXCharacteristic(bleProtoProcess.getCompleted());
                byte [] b = bleProtoProcess.getCompleted();
                Log.d(TAG, "b-->"+ Arrays.toString(b));
                mService.writeRXCharacteristic(b);
                Log.d(TAG,"mService-->"+mService.toString());

                //------------发送数据到服务器
                final String base64 = bleProtoProcess.getBuffer();
                Log.d(TAG, "base64-->" + base64);
//                Toast.makeText(MainActivity.this, "完整数据接受成功,发送到服务端", Toast.LENGTH_SHORT).show();
                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
//                            mTvLog.setText("[" + currentDateTimeString + "] RX: " + bleProtoProcess.getLog() +"    end..."/*+ "base64-->" + base64*/);
//                                        messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                });

                return true;
            }
        } catch (IllegalAccessException e) {
            Toast.makeText(AddBlueToothDeviceActivity.this, "异常", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        return true;
    }

    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkBleDevice(this);
        Log.d(TAG, "onResume");
        if (!mBtAdapter.isEnabled()) {
            Log.i(TAG, "onResume - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

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
}
