package com.pandadentist.configwifi.android;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.Status;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.security.Credentials;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

import com.pandadentist.R;
import com.pandadentist.configwifi.model.ATCommand;
import com.pandadentist.configwifi.model.ATCommandListener;
import com.pandadentist.configwifi.model.Module;
import com.pandadentist.configwifi.model.NetworkProtocol;
import com.pandadentist.configwifi.net.UdpBroadcast;
import com.pandadentist.configwifi.net.UdpUnicast;
import com.pandadentist.configwifi.utils.Constants;
import com.pandadentist.configwifi.utils.Utils;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.List;

import static com.pandadentist.R.id.editText1;


/**
 * The main activity of this application
 *
 * @author ZhangGuoYin
 */
public class WifiConfigActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = WifiConfigActivity.class.getSimpleName();

    private static final int MSG_ENTER_CMD = 1;
    private static final int MSG_RETRY_ENTER_CMD = 2;
    private static final int MSG_ENABLE_WIFI = 3;

    //The top WiFi turn ON/OFF panel
    private TextView mWiFiStateTextView;
    private TextView mStatusTextView;
    private CheckBox mToggleButton;
    private EditText mSsidEditText;
    private EditText mPasswordEditText;
    private Button mChooseButton;
    private Button mOkButton;
    private Button mCancelButton;
    private CheckBox mShowPasswordCheckBox;
    /**
     * The dialog to choose wifi AP
     */
    private AlertDialog mChooseApDialog;

    private final IntentFilter mFilter;
    private final BroadcastReceiver mReceiver;

    private WifiManager mWifiManager;
    private WifiEnabler mWifiEnabler;
    private DetailedState mLastState;
    private WifiInfo mLastInfo;
//    private Scanner mScanner;
    //    private WifiAutomaticConnecter mWifiAutomaticConnecter;
    private boolean mResetNetworks = true;

    private List<AccessPoint> mLatestAccessPoints;
    private AccessPointAdapter mAccessPointAdapter;
    private List<Module> mModules;
//    private WifiStatus mLastWifiStatus;
    private long mLastCMD;
    private boolean mIsCMDMode;
    private boolean mApPasswdEmptyWarning;
    /**
     * The time when press back button on android device
     */
    private long mBackLastTime;
    /**
     * The flag to indicate the activity is destroyed
     */
    private boolean mIsExit;
    private int mFailedTimes;
    private UdpBroadcast mScanBroadcast;
    private UdpUnicast mUdpUnicast;
    private ATCommand mATCommand;
    private ATCommandListener mATCommandListener;
    /**
     * The response of AT command
     */
    private StringBuffer mAtResponse = new StringBuffer();
    private Handler mNetworkHandler;
    private Repeater mTestCmdRepeater;
    private List<ScanResult> tempwifiList;

    private ScanResult mConnect2ScanResult;

    public WifiConfigActivity() {
        //监听wifi各种状态
        mFilter = new IntentFilter();
        mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mFilter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        mFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        //处理wifi变化的广播
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleEvent(intent);
            }
        };
        //UDP 广播
        mScanBroadcast = new UdpBroadcast() {

            @Override
            public void onReceived(List<DatagramPacket> packets) {
                Log.d(TAG, "UDP 广播--->onReceived");
                mScanBroadcast.close();
                mModules = Utils.decodePackets(WifiConfigActivity.this, packets);
                //save the ap module  mid info into local
                if (mModules != null && mModules.size() > 0 && mModules.get(0) != null) {
                    Log.d(TAG, "ScanBroadcast: save the module info in local file:" + mModules.get(0));
                    Utils.saveDevice(WifiConfigActivity.this, generateNetworkKey(), mModules.get(0));

                    //disconnect the current connection, so that the wifi automatic connector will connect it again;
                    //and it can get the module info from local, and enter cmd mode; it avoid send broadcast to get mid info
                    //each times when connected
                    mWifiManager.disconnect();
                } else {
                    Log.d(TAG, "ScanBroadcast: not find any module info");
                    mScanBroadcast.open();
                    mScanBroadcast.send(Utils.getCMDScanModules(WifiConfigActivity.this));
                }
            }
        };
        // 网络处理器 貌似用来发送
        mNetworkHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                switch (msg.what) {
                    case MSG_ENTER_CMD:

                        Log.d(TAG, "try to enter cmd mode");

                        mUdpUnicast.setIp(mModules.get(0).getIp());
                        mUdpUnicast.open();
                        mFailedTimes = 0;
                        new Thread(new Runnable() {

                            @Override
                            public void run() {
                                mATCommand.enterCMDMode();
                            }
                        }).start();
                        break;
                    case MSG_RETRY_ENTER_CMD:
                        Log.d(TAG, "重新尝试进入  cmd 模式");
                        setProgressBarIndeterminateVisibility(true);
                        if (mFailedTimes > 3) {
                            showStatusText(R.color.dark_blue, getString(R.string.retry));
                        } else {
                            showStatusText(R.color.dark_blue, getString(R.string.waitting));
                        }
                        new Thread(new Runnable() {

                            @Override
                            public void run() {
                                //as there's no transparent transmission mode in this application,
                                //so just send AT+Q\r CMD to exit
                                if (mUdpUnicast.send(Constants.CMD_EXIT_CMD_MODE)) {
                                    try {
                                        Thread.sleep(300);
                                    } catch (InterruptedException e) {
                                    }
                                    mATCommand.enterCMDMode();
                                } else {
                                    mWifiManager.setWifiEnabled(false);
                                    try {
                                        Thread.sleep(600);
                                    } catch (InterruptedException e) {
                                    }
                                    mWifiManager.setWifiEnabled(true);
                                    mWifiManager.reassociate();
                                }
                            }
                        }).start();
                        break;
                    case MSG_ENABLE_WIFI:
                        //更改checkbox状态
                        mToggleButton.setChecked(true);
                        break;

                    default:
                        break;
                }
            }
        };
        // 热点列表
        mLatestAccessPoints = new ArrayList<AccessPoint>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //标题显示进度条
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        //使屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.wifi_activity);
        //标题栏显示进度条
        setProgressBarIndeterminateVisibility(true);
        tempwifiList = new ArrayList<>();
        //扫描wifi
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//        mWifiManager.startScan();
//        List<ScanResult> scanResults = mWifiManager.getScanResults();
//        检索上次连接的结果
//        mConnect2ScanResult = Utils.getLastScanResult(this);
        //检索当前WiFi连接是否存在
//        mLastWifiStatus = new WifiStatus(this);
//        mLastWifiStatus.load();
//        String bssid = mLastWifiStatus.getBSSID();
//        if (bssid != null) {//if the wifi is connected
//            //如果wifi连接 这里直接连接 BJYDD
//            if (scanResults != null) {
//                ScanResult scanResult = null;
//                for (int i = 0; i < scanResults.size(); i++) {
//                    if (Utils.removeDoubleQuotes(scanResults.get(i).BSSID).equals(Utils.removeDoubleQuotes(bssid))) {
//                        scanResult = scanResults.get(i);
//                        break;
//                    }
//                }
//                // 把上次连接wifi的结果赋值
//                if (scanResult != null) {
//                    mConnect2ScanResult = scanResult;
//                }
//            }
//        } else if (mConnect2ScanResult != null) {//if wifi is not connected
//
//            if (scanResults != null) {
//                for (int i = 0; i < scanResults.size(); i++) {
//                    if (Utils.removeDoubleQuotes(scanResults.get(i).BSSID).equals(Utils.removeDoubleQuotes(mConnect2ScanResult.BSSID))) {
//                        mConnect2ScanResult = scanResults.get(i);
//                        break;
//                    }
//                }
//            }
//        }

        //设置views的初始化和监听
        setupViews();
        //创建选择热点也就是wifi的列表的adapter
        mAccessPointAdapter = new AccessPointAdapter(this, Utils.getSettingApSSID(this)) {

            @Override
            public void onItemClicked(AccessPoint accessPoint, int position) {
                super.onItemClicked(accessPoint, position);
                // 选择wifi点击事件
                if (mChooseApDialog != null && mChooseApDialog.isShowing()) {
                    Button button = mChooseApDialog.getButton(Dialog.BUTTON_POSITIVE);
                    if (!button.isEnabled()) {
                        button.setEnabled(true);
                    }
                }
            }
        };
        //查看wifi是否可用并且通过textview 显示
        mWifiEnabler = new WifiEnabler(this, mToggleButton, mWiFiStateTextView);
        //扫描wifi 每6秒一次
//        mScanner = new Scanner(this);
        // wifi 自动连接器
//		mWifiAutomaticConnecter = new WifiAutomaticConnecter(this) {
//
//			@Override
//			public void connectSecurity(AccessPoint accessPoint) {
//
//				String password = Utils.getSettingApPassword(WifiConfigActivity.this);
//				if (password == null || password.length()==0) {
//
//					if (!mApPasswdEmptyWarning) {
//						mApPasswdEmptyWarning = true;
//						new AlertDialog.Builder(WifiConfigActivity.this)
//								.setTitle(R.string.warning)
//								.setMessage(getString(R.string.password_not_empty, getSsid()))
//								.setCancelable(false)
//								.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//
//									@Override
//									public void onClick(DialogInterface dialog, int which) {
//										startActivityForResult(new Intent(WifiConfigActivity.this, SettingActivity.class), 1);
//									}
//								}).create().show();
//						return;
//					}
//				}
//
//				connectAP(accessPoint, password);
//			}
//
//			@Override
//			public void connectOpenNone(AccessPoint accessPoint, int networkId) {
//				connect(networkId);
//			}
//
//			@Override
//			public void onSsidNotFind() {
//				updateViews(false);
//				showStatusText(R.color.red, getString(R.string.ap_not_find, Utils.getSettingApSSID(WifiConfigActivity.this)));
//			}
//		};

        mATCommandListener = new ATCommandListener() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "onResponse:" + response);

                response = response.trim();
                if ((response.equals("+ok") || response.startsWith(Constants.RESPONSE_ERR))
                        && (System.currentTimeMillis() - mLastCMD < 2000)) {
                    synchronized (mAtResponse) {
                        mAtResponse.setLength(0);
                        mAtResponse.append(response);
                        mAtResponse.notifyAll();
                    }
                }
            }

            @Override
            public void onEnterCMDMode(boolean success) {
                Log.d(TAG, "onEnterCMDMode:" + success);

                setProgressBarIndeterminateVisibility(false);
                updateViews(success);
                mIsCMDMode = success;
                if (success) {

                    mStatusTextView.setText(null);
                    //start a test cmd repeater to send test cmd in periodic 50 seconds to keep connection
                    if (mTestCmdRepeater != null) {
                        mTestCmdRepeater.pause();
                    }
                    mTestCmdRepeater = new Repeater(Constants.TIMER_CHECK_CMD) {

                        @Override
                        public void repeateAction() {
                            mATCommand.send(Constants.CMD_VER);
                        }
                    };
                    mTestCmdRepeater.resume();
                } else {

                    //show the error info about udp failed
                    mFailedTimes++;
                    if (mFailedTimes > 3) {
                        showStatusText(R.color.red, getString(R.string.enter_cmd_mode_failed));
                    }

                    if (!mIsExit) {
                        logD("Retry to enter CMD mode again for times");
                        mNetworkHandler.sendEmptyMessageDelayed(MSG_RETRY_ENTER_CMD, 1000);
                    }
                }
            }

            @Override
            public void onExitCMDMode(boolean success, NetworkProtocol protocol) {
                logD("onExitCMDMode:" + success);
            }

            @Override
            public void onReload(boolean success) {
            }

            @Override
            public void onReset(boolean success) {
            }

            @Override
            public void onSendFile(boolean success) {
            }

            @Override
            public void onResponseOfSendFile(String response) {
            }
        };
        mUdpUnicast = new UdpUnicast();
        mUdpUnicast.setPort(Utils.getUdpPort(this));
        mATCommand = new ATCommand(mUdpUnicast);
        mATCommand.setListener(mATCommandListener);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mWifiEnabler.resume();
//        mWifiAutomaticConnecter.resume();
        registerReceiver(mReceiver, mFilter);

        if (!mWifiManager.isWifiEnabled()) {
            logD("Wifi is not enable, enable it in 2 seconds!");
            mNetworkHandler.sendEmptyMessageDelayed(MSG_ENABLE_WIFI, 2000);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWifiEnabler.pause();

        try {
            unregisterReceiver(mReceiver);
        } catch (Exception e) {
        }
//        mScanner.pause();
//        mWifiAutomaticConnecter.pause();
        if (!mIsCMDMode) {
            mScanBroadcast.close();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIsExit = true;
        mUdpUnicast.send(Constants.CMD_EXIT_CMD_MODE);
        closeActions();
//        mLastWifiStatus.reload();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            mApPasswdEmptyWarning = false;
        }
    }


    @SuppressLint("Override")
    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        // TODO Auto-generated method stub
        if (id == 1) {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle(R.string.default_dialog_title);
            progressDialog.setMessage(getString(R.string.waitting));
            progressDialog.setCancelable(false);
            return progressDialog;
        }
        return super.onCreateDialog(id, args);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {

            long time = System.currentTimeMillis();
            if (time - mBackLastTime > 2000) {
                mBackLastTime = time;
                toast(R.string.press_again_to_exist);
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @SuppressWarnings("serial")
    @Override
    public void onClick(View view) {
        if (view == mChooseButton) {

            retrieveAccessPointsAdapter();
            mAccessPointAdapter.setSelected(mConnect2ScanResult);
            mChooseApDialog = new AlertDialog.Builder(WifiConfigActivity.this)
                    .setTitle(R.string.pls_choose_ap)
                    .setSingleChoiceItems(mAccessPointAdapter, -1, null)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AccessPoint accessPoint = mAccessPointAdapter.getSelected();
                            if (accessPoint != null) {
                                mConnect2ScanResult = accessPoint.getScanResult();
//								mSsidEditText.setText(mConnect2ScanResult.SSID);
                                mPasswordEditText.setText("aixinxin");
                            }
                            updateViews(true);
                        }
                    }).setNegativeButton(R.string.cancel, null)
                    .create();
            mChooseApDialog.show();

            if (mAccessPointAdapter.getSelected() == null) {
                mChooseApDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
            }
        } else if (view == mOkButton) {

            //only the ssid is input, that means there's a scan result to connect
            if (mConnect2ScanResult != null) {

                //retrieve the latest wifi scan results
                retrieveAccessPointsAdapter();
                //check whether there's the specific ssid in the list
                mAccessPointAdapter.setSelected(mConnect2ScanResult);
                AccessPoint accessPoint = mAccessPointAdapter.getSelected();
                //if exist
                if (accessPoint != null && accessPoint.getScanResult() != null) {
                    final ScanResult scanResult = accessPoint.getScanResult();

                    //check if the wifi network with ssid has changed
                    boolean ssidChanged = mConnect2ScanResult.SSID != null
                            && !mConnect2ScanResult.SSID.equals(scanResult.SSID);
                    boolean capabilitesChanged = mConnect2ScanResult.capabilities != null
                            && !mConnect2ScanResult.capabilities.equals(scanResult.capabilities);
                    boolean openNone = Utils.SECURITY_OPEN_NONE.equals(Utils.parseSecurity(scanResult.capabilities));

                    if (ssidChanged && capabilitesChanged && !openNone) {

                        simpleDialog(getString(R.string.network_changed_title, mConnect2ScanResult.SSID),
                                getString(R.string.network_changed_msg_ssid_capabilites, scanResult.SSID), true,
                                new SimpleDialogListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
//								mSsidEditText.setText(scanResult.SSID);
                                        mConnect2ScanResult = scanResult;
                                        updateViews(true);
                                    }
                                });

                        return;
                    } else if ((ssidChanged && capabilitesChanged && openNone) || ssidChanged) {

                        simpleDialog(getString(R.string.network_changed_title, mConnect2ScanResult.SSID),
                                getString(R.string.network_changed_msg_ssid, scanResult.SSID), true,
                                new SimpleDialogListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {

//								mSsidEditText.setText(scanResult.SSID);
                                        mConnect2ScanResult = scanResult;

                                        if (checkSsidPasswordInput()) {
                                            switchModule2STA(mConnect2ScanResult, mPasswordEditText.getText().toString());
                                        }
                                    }
                                });
                        return;
                    } else if (capabilitesChanged) {

                        if (openNone) {
                            mConnect2ScanResult = scanResult;
                        } else {
                            simpleDialog(getString(R.string.network_changed_title, mConnect2ScanResult.SSID),
                                    getString(R.string.network_changed_msg_capabilites), true,
                                    new SimpleDialogListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                            int which) {

                                            mConnect2ScanResult = scanResult;
                                            updateViews(true);
                                        }
                                    });
                            return;
                        }
                    }
                }

                if (checkSsidPasswordInput()) {
                    switchModule2STA(mConnect2ScanResult, mPasswordEditText.getText().toString());
                }
            }
        } else if (view == mCancelButton) {
            finish();
        } else if (view.getId() == R.id.button_setting) {
            startActivity(new Intent(this, SettingActivity.class));
        } else if (view.getId() == R.id.button_about) {
            startActivity(new Intent(this, AboutActivity.class));
        }
    }

    /**
     * setup views and listeners
     */
    private void setupViews() {

        mWiFiStateTextView = (TextView) findViewById(R.id.textView1);
        mToggleButton = (CheckBox) findViewById(R.id.toggleButton1);
        mStatusTextView = (TextView) findViewById(R.id.textView2);
        Paint paint = mStatusTextView.getPaint();
        paint.setAntiAlias(true);
        paint.setUnderlineText(true);

        mSsidEditText = (EditText) findViewById(editText1);
        mPasswordEditText = (EditText) findViewById(R.id.editText2);
        mChooseButton = (Button) findViewById(R.id.button1);
        mOkButton = (Button) findViewById(R.id.button2);
        mCancelButton = (Button) findViewById(R.id.button3);
        mShowPasswordCheckBox = (CheckBox) findViewById(R.id.checkBox1);

        if (mConnect2ScanResult != null) {
//    		mSsidEditText.setText(mConnect2ScanResult.SSID);
            mPasswordEditText.setText("aixinxin");
        }

        final TextWatcher passwordTextWatcher = new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateViews(true);
            }
        };
        mPasswordEditText.addTextChangedListener(passwordTextWatcher);
        mPasswordEditText.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mPasswordEditText.setSelection(mPasswordEditText.getText().length());
                }
            }
        });

        mShowPasswordCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mPasswordEditText.removeTextChangedListener(passwordTextWatcher);
                mPasswordEditText.setInputType(isChecked ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL :
                        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                mPasswordEditText.setSelection(mPasswordEditText.getText().length());
                mPasswordEditText.addTextChangedListener(passwordTextWatcher);
            }
        });

        mChooseButton.setOnClickListener(this);
        mOkButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);
        findViewById(R.id.button_setting).setOnClickListener(this);
        findViewById(R.id.button_about).setOnClickListener(this);
    }

    /**
     * update the status of these views: {@link #mChooseButton}, {@link #mSsidEditText},
     * {@link #mPasswordEditText}, {@link #mOkButton}
     *
     * @param LPBConnected true if the android device is connected to module HF-LPB, otherwise false
     */
    private void updateViews(boolean LPBConnected) {

        if (LPBConnected != mChooseButton.isEnabled()) {
            mChooseButton.setEnabled(LPBConnected);
        }
        if (!LPBConnected && mPasswordEditText.isEnabled()) {
            mPasswordEditText.setEnabled(LPBConnected);
        }

        if (mConnect2ScanResult == null) {
//			mOkButton.setEnabled(false);
            mSsidEditText.requestFocus();
            return;
        }

        if (LPBConnected) {

            boolean needPassword = true;
            if (Utils.SECURITY_OPEN_NONE.equals(Utils.parseSecurity(mConnect2ScanResult.capabilities))) {
                needPassword = false;
            }

            if (needPassword) {
//    			mOkButton.setEnabled(mPasswordEditText.getText().length() != 0);
                if (!mPasswordEditText.isEnabled()) {
                    mPasswordEditText.setEnabled(true);
                }
                mPasswordEditText.requestFocus();
                mPasswordEditText.setSelection(mPasswordEditText.getText().length());
            } else {
                if (!mOkButton.isEnabled()) {
                    mOkButton.setEnabled(true);
                }
                if (mPasswordEditText.isEnabled()) {
                    mPasswordEditText.setEnabled(false);
                }
            }
        } else {
            if (mOkButton.isEnabled()) {
//    			mOkButton.setEnabled(false);
            }
            mSsidEditText.requestFocus();
        }
    }

    private boolean checkSsidPasswordInput() {

        logD("checkSsidPasswordInput");

        String passwd = mPasswordEditText.getText().toString();
        String security = Utils.parseSecurity(mConnect2ScanResult.capabilities);
        if (security != null && !security.equals(Utils.SECURITY_OPEN_NONE)) {

            if (passwd.length() == 0) {
                simpleDialog(getString(R.string.warning), getString(R.string.password_not_empty, mConnect2ScanResult.SSID), false, null);
                return false;
            } else if (security.equals(Utils.SECURITY_WEP)) {
                int wepType = Utils.checkWepType(passwd);
                System.out.println("wepType: " + wepType);
                if (wepType == Utils.WEP_INVALID) {
                    //prompt dialog that password invalid
                    simpleDialog(getString(R.string.warning), getString(R.string.wep_password_invalid), false, null);
                    return false;
                }
            }
        }

        Utils.saveLastScanResult(getApplicationContext(), mConnect2ScanResult);
        Utils.saveScanResultPassword(getApplicationContext(), mConnect2ScanResult, passwd);

        return true;
    }

    /**
     * switch the module to station mode
     *
     * @param scanResult the AP to connect to
     * @param password   the password
     */
    private void switchModule2STA(final ScanResult scanResult, final String password) {

        new AsyncTask<Void, Void, Integer>() {

            private static final int RESULT_CMD_MODE_FAILED = -1;
            private static final int RESULT_RESPONSE_ERROR = -2;
            private static final int RESULT_RESPONSE_TIME_OUT = -3;
            private static final int RESULT_SUCCESS = 0;

            @Override
            protected void onPreExecute() {
                showDialog(1);
            }

            @Override
            protected Integer doInBackground(Void... params) {

                StringBuffer response = new StringBuffer();
                //send AT+ to test if it's in command mode
                if (!sendAtCmd(Constants.CMD_TEST, response)) {
                    //not in command mode, try to enter into command mode
                    synchronized (mAtResponse) {
                        logD("Try to enter into cmd mode again");
                        mFailedTimes = 0;
                        if (mTestCmdRepeater != null) {
                            mTestCmdRepeater.pause();
                        }
                        mAtResponse.setLength(0);
                        mATCommand.enterCMDMode();
                        try {
                            mAtResponse.wait(15000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    if (mAtResponse.toString().trim().equals("")) {
                        //enter into cmd mode failed
                        logW("Enter into cmd mode failed");
                        return RESULT_CMD_MODE_FAILED;
                    }
                }

                //send AT+WMODE=STA
                response.setLength(0);
                if (!sendAtCmd(Constants.CMD_STA, response)) {
                    logW("Failed: send AT+WMODE=STA");
                    return response.toString().equals(Constants.RESPONSE_ERR) ?
                            RESULT_RESPONSE_ERROR : RESULT_RESPONSE_TIME_OUT;
                }

                //send AT+WSSSID=%s
                response.setLength(0);
                if (!sendAtCmd(Utils.generateWsssid(scanResult.SSID), response)) {
                    logW("Failed: send AT+WSSSID=%s");
                    return response.toString().equals(Constants.RESPONSE_ERR) ?
                            RESULT_RESPONSE_ERROR : RESULT_RESPONSE_TIME_OUT;
                }

                //send AT+WSKEY=%s
                response.setLength(0);
                if (!sendAtCmd(Utils.generateWskeyCmd(scanResult, password), response)) {
                    logW("Failed: send AT+WSKEY=%s");
                    return response.toString().equals(Constants.RESPONSE_ERR) ?
                            RESULT_RESPONSE_ERROR : RESULT_RESPONSE_TIME_OUT;
                }

                //send AT+Z
                sendAtCmd(Constants.CMD_RESET, null);
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mWifiManager.disconnect();
                return RESULT_SUCCESS;
            }

            @Override
            protected void onPostExecute(Integer result) {
                if (result != null) {
                    switch (result) {
                        case RESULT_CMD_MODE_FAILED:
                            simpleDialog(R.string.enter_cmd_mode_failed);
                            break;
                        case RESULT_RESPONSE_ERROR:
                            simpleDialog(R.string.response_failed);
                            break;
                        case RESULT_RESPONSE_TIME_OUT:
                            simpleDialog(R.string.response_time_out);
                            break;
                        case RESULT_SUCCESS:
                            updateViews(false);
                            showStatusText(R.color.red, getString(R.string.ap_lost, Utils.getSettingApSSID(getApplicationContext())));
                            toast(R.string.reset_wait, true);
                            break;

                        default:
                            break;
                    }
                }
                dismissDialog(1);
            }
        }.execute(null, null, null);
    }

    /**
     * send the AT command whose response is "+ok"
     *
     * @param cmd
     * @return
     */
    private boolean sendAtCmd(String cmd, StringBuffer response) {

        if (mTestCmdRepeater != null) {
            mTestCmdRepeater.pause();
        }

        boolean success = false;
        for (int i = 0; i < 2; i++) {//re-send it if the previous sending is failed

            synchronized (mAtResponse) {
                mAtResponse.setLength(0);

                if (mATCommand != null) {
                    mLastCMD = System.currentTimeMillis();
                    mATCommand.send(cmd);
                }

                if (!Constants.CMD_RESET.equals(cmd)) {
                    try {
                        mAtResponse.wait(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    String atResp = mAtResponse.toString().trim();
                    if (atResp.equals(Constants.RESPONSE_OK)) {
                        if (response != null) {
                            response.append(Constants.RESPONSE_OK);
                        }
                        success = true;
                        break;
                    } else if (atResp.startsWith(Constants.RESPONSE_ERR)) {
                        if (response != null) {
                            response.append(Constants.RESPONSE_ERR);
                        }
                        success = false;
                        break;
                    }
                } else {
                    success = true;
                }
            }
        }

        if (mTestCmdRepeater != null) {
            mTestCmdRepeater.resumeWithDelay();
        }

        return success;
    }

    /**
     * Some actions need to be closed
     */
    private void closeActions() {

        mNetworkHandler.removeMessages(MSG_RETRY_ENTER_CMD);
        mScanBroadcast.close();
        mUdpUnicast.close();
        if (mTestCmdRepeater != null) {
            mTestCmdRepeater.pause();
        }
    }

    private boolean requireKeyStore(WifiConfiguration config) {
        if (Utils.requireKeyStore(config)) {
            Credentials.getInstance().unlock(this);
            return true;
        }
        return false;
    }

    /**
     * Connect the configured network
     *
     * @param networkId
     */
    private void connect(int networkId) {
        if (networkId == -1) {
            return;
        }

        // Connect to network by disabling others.
        mWifiManager.enableNetwork(networkId, true);
        mWifiManager.reconnect();
        updateAccessPoints();
    }

    private void enableNetworks() {
        for (int i = mLatestAccessPoints.size() - 1; i >= 0; --i) {
            WifiConfiguration config = mLatestAccessPoints.get(i).getConfig();
            if (config != null && config.status != Status.ENABLED) {
                mWifiManager.enableNetwork(config.networkId, false);
            }
        }
    }

    private void saveNetworks() {
        // Always save the configuration with all networks enabled.
        mWifiManager.saveConfiguration();
        updateAccessPoints();
    }

    private synchronized List<AccessPoint> updateAccessPoints() {

        List<AccessPoint> accessPoints = new ArrayList<AccessPoint>();

        List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
        if (configs != null) {
            for (WifiConfiguration config : configs) {

                // Shift the status to make enableNetworks() more efficient.
                if (config.status == Status.CURRENT) {
                    config.status = Status.ENABLED;
                } else if (mResetNetworks && config.status == Status.DISABLED) {
                    config.status = Status.CURRENT;
                }

                AccessPoint accessPoint = new AccessPoint(this, config);
                accessPoint.update(mLastInfo, mLastState);
                accessPoints.add(accessPoint);
            }
        }

        List<AccessPoint> scanAccessPoints = new ArrayList<AccessPoint>();
        List<ScanResult> results = mWifiManager.getScanResults();
        if (results != null) {
            for (ScanResult result : results) {
                // Ignore hidden and ad-hoc networks.
                if (result.SSID == null || result.SSID.length() == 0 ||
                        result.capabilities.contains("[IBSS]")) {
                    continue;
                }

                scanAccessPoints.add(new AccessPoint(this, result));
                for (AccessPoint accessPoint : accessPoints) {
                    accessPoint.update(result);
                }
            }
        }

        mLatestAccessPoints.clear();
        mLatestAccessPoints.addAll(scanAccessPoints);
        return scanAccessPoints;
    }

    /**
     * Update the access points using {@link #updateAccessPoints()}, and get the latest {@link #mAccessPointAdapter}
     */
    private void retrieveAccessPointsAdapter() {
        mAccessPointAdapter.setDefaultSSID(Utils.getSettingApSSID(this));
        mAccessPointAdapter.updateAccessPoints(updateAccessPoints());
    }

    private void handleEvent(Intent intent) {
        String action = intent.getAction();

        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
            updateWifiState(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                    WifiManager.WIFI_STATE_UNKNOWN));
        } else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
            //TODO 搜索到模块直接连接AP
            tempwifiList.clear();
            tempwifiList = mWifiManager.getScanResults();
            ScanResult result=null;
            for (ScanResult sr : tempwifiList) {
                if (sr.SSID.contains("BJYDD")) {
                    mSsidEditText.setText(sr.SSID);
                    result = sr;
                    break;
                }
            }
            if(result != null){
//                AccessPoint accessPoint = new AccessPoint(WifiConfigActivity.this, result);
//                connectAP(accessPoint,"");

                int netId = mWifiManager.addNetwork(createWifiConfig("BJYDD_1120", "", WIFICIPHER_NOPASS));
                //WifiManager的enableNetwork接口，就可以连接到netId对应的wifi了
                //其中boolean参数，主要用于指定是否需要断开其它Wifi网络
                boolean enable = mWifiManager.enableNetwork(netId, true);
                Log.d(TAG, "enable: " + enable);
                //可选操作，让Wifi重新连接最近使用过的接入点
                //如果上文的enableNetwork成功，那么reconnect同样连接netId对应的网络
                //若失败，则连接之前成功过的网络
                boolean reconnect = mWifiManager.reconnect();
                Log.d(TAG, "reconnect: " + reconnect);
            }





//            updateAccessPoints();
        } else if (WifiManager.NETWORK_IDS_CHANGED_ACTION.equals(action)) {
            updateAccessPoints();
        } else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
            updateConnectionState(WifiInfo.getDetailedStateOf((SupplicantState)
                    intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE)));
        } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {

            DetailedState state = ((NetworkInfo) intent.getParcelableExtra(
                    WifiManager.EXTRA_NETWORK_INFO)).getDetailedState();

            logD(state.name());
            String ssid = AccessPoint.removeDoubleQuotes(mWifiManager.getConnectionInfo().getSSID());
            if (state == DetailedState.CONNECTED && ssid != null && ssid.equals(Utils.getSettingApSSID(this))) {

                logD(ssid + " is connected.");
                if (ssid.equals(Utils.getSettingApSSID(this))) {

                    if (!mIsCMDMode) {

                        Module module = Utils.getDevice(WifiConfigActivity.this, generateNetworkKey());
                        if (module != null) {

                            if (mModules == null) {
                                mModules = new ArrayList<Module>();
                                mModules.add(module);
                            } else {
                                mModules.add(0, module);
                            }
                            mNetworkHandler.removeMessages(MSG_RETRY_ENTER_CMD);
                            mNetworkHandler.sendEmptyMessage(MSG_ENTER_CMD);
                        } else {

                            logD("Start to broadcast to find module info...");
                            mScanBroadcast.open();
                            mScanBroadcast.send(Utils.getCMDScanModules(this));
                        }
                        showStatusText(R.color.blue, getString(R.string.connected_ap, Utils.getSettingApSSID(this)));
                    }
                } else {
                    logD("Disconnect it.");
                    mWifiManager.disconnect();
                }
            } else if (state == DetailedState.DISCONNECTED || state == DetailedState.OBTAINING_IPADDR) {
                mIsCMDMode = false;
                closeActions();
                updateViews(false);
                showStatusText(R.color.red, getString(R.string.ap_lost, Utils.getSettingApSSID(this)));
                setProgressBarIndeterminateVisibility(true);
            }
            updateConnectionState(state);
        } else if (WifiManager.RSSI_CHANGED_ACTION.equals(action)) {
            updateConnectionState(null);
        }
    }

    private void updateConnectionState(DetailedState state) {
        /* sticky broadcasts can call this when wifi is disabled */
        if (!mWifiManager.isWifiEnabled()) {
//            mScanner.pause();
            return;
        }

        if (state == DetailedState.OBTAINING_IPADDR) {
//            mScanner.pause();
        } else {
//            mScanner.resume();
        }

        mLastInfo = mWifiManager.getConnectionInfo();
        if (state != null) {
            mLastState = state;
        }

        for (int i = mLatestAccessPoints.size() - 1; i >= 0; --i) {
            mLatestAccessPoints.get(i).update(mLastInfo, mLastState);
        }

        if (mResetNetworks && (state == DetailedState.CONNECTED ||
                state == DetailedState.DISCONNECTED || state == DetailedState.FAILED)) {
            updateAccessPoints();
            enableNetworks();
        }
    }

    private void updateWifiState(int state) {
        if (state == WifiManager.WIFI_STATE_ENABLED) {
//            mScanner.resume();
            updateAccessPoints();
        } else {
//            mScanner.pause();
            mLatestAccessPoints.clear();
        }
    }

    private void connectAP(AccessPoint accessPoint, String password) {

        Log.d(TAG,"connectSecurityAP- " + accessPoint.toString());
        WifiConfiguration config = Utils.generateWifiConfiguration(accessPoint, password);

        if (config == null) {
            if (accessPoint != null && !requireKeyStore(accessPoint.getConfig())) {
                connect(accessPoint.getNetworkId());
            }
        } else if (config.networkId != -1) {
            if (accessPoint != null) {
                mWifiManager.updateNetwork(config);
                saveNetworks();
            }
        } else {
            int networkId = mWifiManager.addNetwork(config);
            if (networkId != -1) {
                mWifiManager.enableNetwork(networkId, false);
                config.networkId = networkId;
                if (requireKeyStore(config)) {
                    saveNetworks();
                } else {
                    connect(networkId);
                }
            }
        }
    }

    private String generateNetworkKey() {

        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        if (wifiInfo != null && wifiInfo.getBSSID() != null) {
            return wifiInfo.getBSSID();
        } else {
            return Utils.getSettingApSSID(this);
        }
    }

    private void showStatusText(int color, String text) {
        if (color == -1) {
            mStatusTextView.setTextColor(Color.GRAY);
        } else {
            mStatusTextView.setTextColor(getResources().getColor(color));
        }
        mStatusTextView.setText(text);
        mStatusTextView.setVisibility(View.VISIBLE);
    }
    private static final int WIFICIPHER_NOPASS = 0;
    private static final int WIFICIPHER_WEP = 1;
    private static final int WIFICIPHER_WPA = 2;

    private WifiConfiguration createWifiConfig(String ssid, String password, int type) {
        //初始化WifiConfiguration
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        //指定对应的SSID
        config.SSID = "\"" + ssid + "\"";
        //如果之前有类似的配置
        WifiConfiguration tempConfig = isExist(ssid);
        if(tempConfig != null) {
            //则清除旧有配置
            mWifiManager.removeNetwork(tempConfig.networkId);
        }
        //不需要密码的场景
        if(type == WIFICIPHER_NOPASS) {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            //以WEP加密的场景
        } else if(type == WIFICIPHER_WEP) {
            config.hiddenSSID = true;
            config.wepKeys[0]= "\""+password+"\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
            //以WPA加密的场景，自己测试时，发现热点以WPA2建立时，同样可以用这种配置连接
        } else if(type == WIFICIPHER_WPA) {
            config.preSharedKey = "\""+password+"\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }
    private WifiConfiguration isExist(String ssid) {
        List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();

        for (WifiConfiguration config : configs) {
            if (config.SSID.equals("\""+ssid+"\"")) {
                return config;
            }
        }
        return null;
    }
}