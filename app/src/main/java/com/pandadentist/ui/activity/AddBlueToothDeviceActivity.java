package com.pandadentist.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.ClipboardManager;
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
import com.pandadentist.config.Constants;
import com.pandadentist.entity.WXEntity;
import com.pandadentist.listener.OnItemClickListener;
import com.pandadentist.listener.OnZhenListener;
import com.pandadentist.network.APIFactory;
import com.pandadentist.network.APIService;
import com.pandadentist.service.UartService;
import com.pandadentist.ui.adapter.BlueToothDeviceAdapter;
import com.pandadentist.ui.base.SwipeRefreshBaseActivity;
import com.pandadentist.util.BLEProtoProcess;
import com.pandadentist.util.Toasts;
import com.pandadentist.widget.ColorProgressBar;
import com.pandadentist.widget.RecycleViewDivider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by fudaye on 2017/8/17.
 */

public class AddBlueToothDeviceActivity extends SwipeRefreshBaseActivity {

    private static final String TAG = AddBlueToothDeviceActivity.class.getSimpleName();

    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int STATE_OFF = 10;
    private static final long SCAN_PERIOD = 10000; //蓝牙扫描时长10秒

    @Bind(R.id.ll_not_found)
    LinearLayout llNotFound;
    @Bind(R.id.ll_loading)
    LinearLayout llLoading;
    @Bind(R.id.ll_loading_tip)
    LinearLayout llLoadingTip;
    @Bind(R.id.rv)
    RecyclerView rv;
    @Bind(R.id.ll_device)
    LinearLayout llDevice;
    @Bind(R.id.iv_loading)
    ImageView ivLoading;
    @Bind(R.id.iv_upload_loading)
    ImageView ivUploadLoading;
    @Bind(R.id.circle_progress_bar1)
    ColorProgressBar colorProgressBar;
    @Bind(R.id.tv_percent)
    TextView tvPercent;
    @Bind(R.id.tv_upload_tip)
    TextView tvUploadTip;
    @Bind(R.id.ll_upload)
    LinearLayout llUpload;


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
    //    private ScanListener mScanListener;
    private boolean isBind = false;
    private BluetoothLeScanner bluetoothLeScanner;
    private String macAddress;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolBarTtitle.setText("连接蓝牙");
        mToolbarFuncTv.setText("帮助");
        mToolbarFuncTv.setTextColor(Color.parseColor("#20CBE7"));
        mToolbarFuncRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AddBlueToothDeviceActivity.this,BlueHelperActivity.class));
            }
        });
        bleProtoProcess = new BLEProtoProcess();
        bleProtoProcess.setOnZhenListener(new OnZhenListener() {
            @Override
            public void onZhen(int zhen, int total) {
                float fz = zhen;
                float ft = total;
                float percent = fz / ft * 100f;
                int ip = (int) percent;
                tvPercent.setText(ip + "%");
                colorProgressBar.setValue(ip);
            }
        });
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

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "该设备不支持蓝牙", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        // 打开蓝牙
        if (!mBtAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            init();
        }

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanBlueDecvice();
            }
        });

    }

    @Override
    public int providerLayoutId() {
        return R.layout.activity_add_blue_tooth_device;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void init() {
        // 扫描显示列表
        // 初始化列表
        mAdapter = new BlueToothDeviceAdapter(devices);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.addItemDecoration(new RecycleViewDivider(this, LinearLayoutManager.VERTICAL, 1));
        rv.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                String deviceAddress = devices.get(position).getAddress();
                mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
                Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
                macAddress = devices.get(position).getAddress();
//                ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName() + " - connecting");
                mService.connect(deviceAddress);

            }
        });
        //扫描附近蓝牙设备
//        mScanListener = new ScanListener();
        bluetoothLeScanner = mBtAdapter.getBluetoothLeScanner();
        bluetoothLeScanner.startScan(new ScanCallback() {
        });
        //扫描
        scanBlueDecvice();
        service_init();
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

    Timer timer = null;
    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toasts.showShort("蓝牙连接成功");
                        mState = UART_PROFILE_CONNECTED;
//                        bleProtoProcess.clearLog();
                        rv.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mService.writeRXCharacteristic(bleProtoProcess.getRequests());
                            }
                        }, 2000);
                    }
                });
            }
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toasts.showShort("断开蓝牙连接");
                        mState = UART_PROFILE_DISCONNECTED;
                        mService.close();
                    }
                });
            }
            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mService.enableTXNotification();
            }
            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {
                timecount = 0;
                final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
                int status = bleProtoProcess.interp(txValue);

                switch (status) {
                    case BLEProtoProcess.BLE_DATA_START:
                        showUpload();
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
            rv.post(new Runnable() {
                @Override
                public void run() {
                    colorProgressBar.setValue(99);
                    tvPercent.setText("99%");
                    bleProtoProcess.removeZhenListener();
                }
            });
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
                uploadData();

                return true;
            }
        } catch (IllegalAccessException e) {
            Toast.makeText(AddBlueToothDeviceActivity.this, "异常", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        return true;
    }

    private void service_init() {
        Intent bindIntent = new Intent(this, UartService.class);
        isBind = bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
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


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
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
                    init();
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
        Toast.makeText(AddBlueToothDeviceActivity.this, "复制成功", Toast.LENGTH_LONG).show();
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



    private void showNotFound() {
        llLoading.setVisibility(View.VISIBLE);
        llDevice.setVisibility(View.GONE);
        llLoadingTip.setVisibility(View.GONE);
        llNotFound.setVisibility(View.VISIBLE);
    }

    private void showList() {
        if(llLoading == null){
            return;
        }
        llLoading.setVisibility(View.GONE);
        llDevice.setVisibility(View.VISIBLE);
        llLoadingTip.setVisibility(View.GONE);
        llNotFound.setVisibility(View.GONE);
    }

    private void showLoadingView() {
        llLoading.setVisibility(View.VISIBLE);
        llDevice.setVisibility(View.GONE);
        llLoadingTip.setVisibility(View.VISIBLE);
        llNotFound.setVisibility(View.GONE);
    }

    private void showUpload() {
        llLoading.setVisibility(View.GONE);
        llDevice.setVisibility(View.GONE);
        llLoadingTip.setVisibility(View.GONE);
        llNotFound.setVisibility(View.GONE);
        llUpload.setVisibility(View.VISIBLE);
        //开启动画
        Animation circle_anim = AnimationUtils.loadAnimation(this, R.anim.blue_tooth_round_rotate);
        LinearInterpolator interpolator = new LinearInterpolator();  //设置匀速旋转，在xml文件中设置会出现卡顿
        circle_anim.setInterpolator(interpolator);
        ivUploadLoading.startAnimation(circle_anim);
        tvUploadTip.setText("数据正在上传");
    }

    private Handler mHandler = new Handler();

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void scanBlueDecvice() {
        showLoadingView();
        mHandler.postDelayed(scanRunnable, SCAN_PERIOD);
        mBtAdapter.startLeScan(mLeScanCallback);
//        bluetoothLeScanner.startScan(mScanListener);
        //扫描6秒后停止扫描
//        mHandler.postDelayed(scanRunnable, SCAN_PERIOD);
    }

    private ScanRunnable scanRunnable = new ScanRunnable();

    class ScanRunnable implements Runnable {

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void run() {
            if (devices.size() == 0) {
                showNotFound();
            } else {
                showList();
            }
            mBtAdapter.stopLeScan(mLeScanCallback);
            mAdapter.setData(devices);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        if (isBind) {
            unbindService(mServiceConnection);
        }
        if (mService != null) {
            mService.stopSelf();
            mService = null;
        }
        mHandler.removeCallbacks(scanRunnable);
        mBtAdapter.stopLeScan(mLeScanCallback);
    }


    private void uploadData() {
        APIService api = new APIFactory().create(APIService.class);
        String addr = macAddress.replaceAll(":","");
        String str = "设备地址："+addr+"-"+"Software："+bleProtoProcess.getSoftware() +"-"+
                "Factory："+bleProtoProcess.getFactory()+"-"+"Model："+bleProtoProcess.getModel()+"-"+
                "Power："+bleProtoProcess.getPower()+"-"+"Time："+bleProtoProcess.getTime()+"-"+
                "Hardware："+bleProtoProcess.getHardware()+"-";


        Subscription s = api.uploadData(addr, bleProtoProcess.getSoftware() + "",
                bleProtoProcess.getFactory() + "", bleProtoProcess.getModel() + "",
                bleProtoProcess.getPower() + "", bleProtoProcess.getTime() + "",
                bleProtoProcess.getHardware() + "", bleProtoProcess.getBuffer())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<WXEntity>() {
                    @Override
                    public void call(WXEntity wxEntity) {

                        if (Constants.SUCCESS == wxEntity.getCode()) {
                            Toasts.showShort("上传成功");
                            tvUploadTip.setText("上传成功");
                            tvPercent.setText("100%");
                            colorProgressBar.setProgressColor();
                        }else if (99 == wxEntity.getCode()){
                            Toasts.showShort("系统错误");
                        }else if (20002 == wxEntity.getCode()){
                            Toasts.showShort("设备版本未找到");
                        } else {
                            Toasts.showShort("未知错误");
                            Log.d(TAG, "错误code ：" + wxEntity.getCode() + "错误信息：" + wxEntity.getMessage());
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.d("throwable", "throwable-->" + throwable.toString());
                    }
                });
        addSubscription(s);
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (devices.size() != 0) {
                                showList();
                            }
                            String s = device.getName();
                            if (!devices.contains(device)) {
                                devices.add(device);
                                mAdapter.setData(devices);
                            }
                            Log.d("onScanResult", "onScanResult" + s);
                        }
                    });
                }
            };
}
