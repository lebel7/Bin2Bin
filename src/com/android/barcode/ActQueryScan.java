package com.android.barcode;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.*;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.proper.Logger.LogHelper;
import com.proper.MessageQueue.HttpMessageResolver;
import com.proper.bin2bin.QueryView;
import com.proper.bin2bin.R;
import com.proper.data.*;
import com.proper.data.helpers.MyCustomNamingStrategy;
import com.proper.data.helpers.ResponseHelper;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Created by Lebel on 23/04/2014.
 */
public class ActQueryScan extends Activity {
    private UserLoginResponse currentUser = null;   //Authentication
    public static final int KEY_SCAN = 111;
    public static final int KEY_F1 = 112;
    public static final int KEY_F2 = 113;
    public static final int KEY_F3 = 114;
    public static final int KEY_YELLOW = 115;
    private int KEY_POSITION = 0;
    private int inputByHand = 0;
    private Button btnScan;
    private Button btnExit;
    private Button btnEnterBarcode;
    private Button btnEnterBincode;
    private TextView lblBarcode;
    private TextView lblBin;
    private EditText txtBarcode;
    private EditText txtBin;
    private LinearLayout lytMain;
    private int NAV_INSTRUCTION = 0;
    private int NAV_TURN = 0;
    private int fullTurnCount = 0;
    private String deviceIMEI = "";
    private static final String myMessageType = "BarcodeQuery";
    private static final String ApplicationID = "Bin2Bin";
    private java.util.Date utilDate = java.util.Calendar.getInstance().getTime();
    private java.sql.Timestamp today = null;
    private long startTime;
    private long elapseTime;
    private String backPressedParameter = "";
    private LogHelper logger = new LogHelper();
    private com.proper.MessageQueue.Message thisMessage = new com.proper.MessageQueue.Message();
    private HttpMessageResolver resolver = new HttpMessageResolver();
    private ResponseHelper responseHelper = new ResponseHelper();

    private DeviceControl DevCtrl;
    private SerialPort mSerialPort;
    private String scanInput;
    //private String buff = new String();
    public int fd;
    private WebServiceTask wsTask;
    private ReadThread mReadThread;
    private Handler handler = null;
    private static final String TAG = "SerialPort";
    private boolean key_start = true;
    private boolean Powered = false;
    private boolean Opened = false;
    private Timer timer = new Timer();
    private Timer retrig_timer = new Timer();
    private SoundPool soundPool;
    private	int soundId;
    private	int errorSoundId;
    private Handler t_handler = null;
    private Handler n_handler = null;
    private boolean ops = false;
    private String currentBarcode = "";
    private String currentBincode = "";

    public String getScanInput() {
        return scanInput;
    }

    public void setScanInput(String scanInput) {
        this.scanInput = scanInput;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyt_qryscan);

        TelephonyManager mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        deviceIMEI = mngr.getDeviceId();

        Bundle extras = getIntent().getExtras();
        NAV_INSTRUCTION = extras.getInt("INSTRUCTION_EXTRA");
        //compare instructions passed by the previous screen and then do stuff

        lytMain = (LinearLayout) this.findViewById(R.id.lytQryScanMain);
        btnScan = (Button) this.findViewById(R.id.bnQryScanPerformScan);
        btnExit = (Button) this.findViewById(R.id.bnExitActQryScan);
        btnEnterBarcode = (Button) this.findViewById(R.id.bnEnterBarcodeQryScan);
        btnEnterBincode = (Button) this.findViewById(R.id.bnEnterBincodeQryScan);
        lblBarcode = (TextView) this.findViewById(R.id.txtvQryScanBarcode);
        lblBin = (TextView) this.findViewById(R.id.txtvQryScanBinCode);
        txtBarcode = (EditText) this.findViewById(R.id.etxtQryScanBarcode);
        txtBin = (EditText) this.findViewById(R.id.etxtQryScanBin);

        switch (NAV_INSTRUCTION) {
            case R.integer.ACTION_BINQUERY:
                if (lblBarcode.getVisibility() == View.VISIBLE) lblBarcode.setVisibility(View.GONE);
                if (txtBarcode.getVisibility() == View.VISIBLE) txtBarcode.setVisibility(View.GONE);
                if (btnEnterBarcode.getVisibility() == View.VISIBLE) btnEnterBarcode.setVisibility(View.GONE);
                if (lblBin.getVisibility() != View.VISIBLE) lblBin.setVisibility(View.VISIBLE);
                if (txtBin.getVisibility() != View.VISIBLE) txtBin.setVisibility(View.VISIBLE);
                if (btnEnterBincode.getVisibility() != View.VISIBLE) btnEnterBincode.setVisibility(View.VISIBLE);
                PaintButtonText();
                break;
            case R.integer.ACTION_BARCODEQUERY:
                if (lblBin.getVisibility() == View.VISIBLE) lblBin.setVisibility(View.GONE);
                if (txtBin.getVisibility() == View.VISIBLE) txtBin.setVisibility(View.GONE);
                if (btnEnterBincode.getVisibility() == View.VISIBLE) btnEnterBincode.setVisibility(View.GONE);
                if (lblBarcode.getVisibility() != View.VISIBLE) lblBarcode.setVisibility(View.VISIBLE);
                if (txtBarcode.getVisibility() != View.VISIBLE) txtBarcode.setVisibility(View.VISIBLE);
                if (btnEnterBarcode.getVisibility() != View.VISIBLE) btnEnterBarcode.setVisibility(View.VISIBLE);
                PaintButtonText();
                break;
            case R.integer.ACTION_BARCODE_BINQUERY:
                if (txtBin.getVisibility() != View.VISIBLE) txtBin.setVisibility(View.VISIBLE);
                if (txtBin.getVisibility() != View.VISIBLE) txtBin.setVisibility(View.VISIBLE);
                if (btnEnterBincode.getVisibility() != View.VISIBLE) btnEnterBincode.setVisibility(View.VISIBLE);
                lockBinControls();  // disables lblBin & txtBin
                if (lblBarcode.getVisibility() != View.VISIBLE) lblBarcode.setVisibility(View.VISIBLE);
                if (txtBarcode.getVisibility() != View.VISIBLE) txtBarcode.setVisibility(View.VISIBLE);
                if (btnEnterBarcode.getVisibility() != View.VISIBLE) btnEnterBarcode.setVisibility(View.VISIBLE);
                break;
            default:
                if (txtBin.getVisibility() != View.VISIBLE) txtBin.setVisibility(View.VISIBLE);
                if (txtBin.getVisibility() != View.VISIBLE) txtBin.setVisibility(View.VISIBLE);
                if (btnEnterBincode.getVisibility() != View.VISIBLE) btnEnterBincode.setVisibility(View.VISIBLE);
                if (lblBarcode.getVisibility() != View.VISIBLE) lblBarcode.setVisibility(View.VISIBLE);
                if (txtBarcode.getVisibility() != View.VISIBLE) txtBarcode.setVisibility(View.VISIBLE);
                if (btnEnterBarcode.getVisibility() != View.VISIBLE) btnEnterBarcode.setVisibility(View.VISIBLE);
                lockAllControls();
                new AlertDialog.Builder(this).setTitle(R.string.DIA_ALERT).setMessage(R.string.DEV_NAV_ERR).setPositiveButton(R.string.DIA_CHECK, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        finish();
                    }
                }).show();
                break;
        }

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ButtonClicked(view);
            }
        });
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ButtonClicked(view);
            }
        });
        btnEnterBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ButtonClicked(view);
            }
        });
        btnEnterBincode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ButtonClicked(view);
            }
        });
        txtBarcode.setImeOptions(EditorInfo.IME_ACTION_DONE);
        txtBarcode.addTextChangedListener(new TextChanged());
        txtBin.setImeOptions(EditorInfo.IME_ACTION_DONE);
        txtBin.addTextChangedListener(new TextChanged());


        try {
            DevCtrl = new DeviceControl("/proc/driver/scan");

        } catch (SecurityException e) {
            e.printStackTrace();
            today = new java.sql.Timestamp(utilDate.getTime());
            LogEntry log = new LogEntry(1L, ApplicationID, "ActQueryScan - onCreate", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
            logger.Log(log);
        } catch (IOException e) {
            Log.d(TAG, "AAA");
            today = new java.sql.Timestamp(utilDate.getTime());
            LogEntry log = new LogEntry(1L, ApplicationID, "ActQueryScan - onCreate", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
            logger.Log(log);
            new AlertDialog.Builder(this).setTitle(R.string.DIA_ALERT).setMessage(R.string.DEV_OPEN_ERR).setPositiveButton(R.string.DIA_CHECK, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    finish();
                }
            }).show();
            return;
        }
        ops = true;

        KEY_POSITION = 0; //Set for Yellow button to scan

        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        soundId = soundPool.load(getString(R.string.SOUND_SCAN), 0);
        errorSoundId = soundPool.load(getString(R.string.SOUND_ERROR), 0);

        t_handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.what == 1) {
                    try {
                        DevCtrl.PowerOffDevice();
                    } catch (IOException e) {
                        Log.d(TAG, "BBB");
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        today = new java.sql.Timestamp(utilDate.getTime());
                        LogEntry log = new LogEntry(1L, ApplicationID, "ActQueryScan - onCreate", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                        logger.Log(log);
                    }//powersave
                    Powered = false;
                }
            }
        };

        n_handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.what == 1) {
                    try {
                        if(key_start == false)
                        {
                            DevCtrl.TriggerOffDevice();
                            timer = new Timer();				//start a timer, when machine is idle for some time, cut off power to save energy.
                            timer.schedule(new MyTask(), 60000);
                            btnScan.setEnabled(true);
                            key_start = true;
                        }
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        today = new java.sql.Timestamp(utilDate.getTime());
                        LogEntry log = new LogEntry(1L, ApplicationID, "ActQueryScan - onCreate", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                        logger.Log(log);
                    }
                }
            }
        };

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg != null) {
                    if(msg.what == 1) {
                        setScanInput(msg.obj.toString());   //Set object scanned by the hardware
                        switch (NAV_INSTRUCTION) {
                            case R.integer.ACTION_BINQUERY:
                                if (getScanInput().length() == 5) {
                                    if (!txtBin.getText().toString().isEmpty()) {
                                        txtBin.setText("");     //to counter a weird bug in editText control
                                        txtBin.setText(getScanInput());
                                    } else {
                                        txtBin.setText(getScanInput());
                                    }
                                } else {
                                    //Scanned wrong item, bin code etc...
                                    Log.e("A bad scan has occured", "Please scan again");
                                    soundPool.play(errorSoundId, 1, 1, 0, 0, 1);
                                    //soundPool.play(errorSoundId, Float.valueOf("0.1"), Float.valueOf("0.1"), 0, 0, 1);
                                    String mMsg = "Bad scan occured \nThis bin code is invalid";
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ActQueryScan.this);
                                    builder.setMessage(mMsg)
                                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    //do nothing
                                                }
                                            });
                                    builder.show();
                                    unLockBinControls();
                                    txtBin.setText("");
                                }
                                break;
                            case R.integer.ACTION_BARCODE_BINQUERY:
                                if (NAV_TURN == R.integer.TURN_BIN) {
                                    if (getScanInput().length() == 5) {
                                        //do turn``````````````````````````````````````````````````````````````````````````````````````````````````
                                        if (!txtBin.getText().toString().isEmpty()) {
                                            txtBin.setText("");     //to counter a weird bug in editText control
                                            txtBin.setText(getScanInput());
                                            //By now the nav turn state has changed
                                            lockBinControls();
                                        } else {
                                            txtBin.setText(getScanInput());
                                            //By now the nav turn state has changed
                                            lockBinControls();
                                        }
                                    } else {
                                        //Scanned wrong item, bin code etc...
                                        Log.e("A bad scan has occured", "Please scan again");
                                        soundPool.play(errorSoundId, 1, 1, 0, 0, 1);
                                        //soundPool.play(errorSoundId, Float.valueOf("0.1"), Float.valueOf("0.1"), 0, 0, 1);
                                        String mMsg = "Bad scan occured \nThis bin code is invalid";
                                        AlertDialog.Builder builder = new AlertDialog.Builder(ActQueryScan.this);
                                        builder.setMessage(mMsg)
                                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        //do nothing
                                                    }
                                                });
                                        builder.show();
                                        unLockBinControls();
                                        txtBin.setText("");
                                    }
                                }
                                if (NAV_TURN == R.integer.TURN_BARCODE) {
                                    int acceptable[] = {12,13,14};
                                    if (getScanInput().length() > 0 && !(Arrays.binarySearch(acceptable, getScanInput().length()) == -1)) {
                                        //do barcode```````````````````````````````````````````````````````````````````````````````````````````````
                                        if (!txtBarcode.getText().toString().isEmpty()) {
                                            txtBarcode.setText(""); //to counter a weird bug in editText control
                                            txtBarcode.setText(getScanInput());
                                            //By now the nav turn state has changed
                                            lockBarcodeControls();
                                        } else {
                                            txtBarcode.setText(getScanInput());
                                            //By now the nav turn state has changed
                                            lockBarcodeControls();
                                        }
                                    } else {
                                        //Scanned wrong item, barcode etc...
                                        Log.e("A bad scan has occured", "Please scan again");
                                        soundPool.play(errorSoundId, 1, 1, 0, 0, 1);
                                        //soundPool.play(errorSoundId, Float.valueOf("0.1"), Float.valueOf("0.1"), 0, 0, 1);
                                        String mMsg = "Bad scan occured \nThis barcode is invalid";
                                        AlertDialog.Builder builder = new AlertDialog.Builder(ActQueryScan.this);
                                        builder.setMessage(mMsg)
                                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        //do nothing
                                                    }
                                                });
                                        builder.show();
                                        refreshActivity();
                                    }
                                }
                                break;
                            case R.integer.ACTION_BARCODEQUERY:
                                int acceptable[] = {12,13,14};
                                if (getScanInput().length() > 0 && !(Arrays.binarySearch(acceptable, getScanInput().length()) == -1)) {
                                    if (!txtBarcode.getText().toString().isEmpty()) {
                                        txtBarcode.setText(""); //to counter a weird bug in editText control
                                        txtBarcode.setText(getScanInput());
                                    } else {
                                        txtBarcode.setText(getScanInput());
                                    }
                                } else {
                                    //Scanned wrong item, barcode etc...
                                    Log.e("A bad scan has occured", "Please scan again");
                                    soundPool.play(errorSoundId, 1, 1, 0, 0, 1);
                                    //soundPool.play(errorSoundId, Float.valueOf("0.1"), Float.valueOf("0.1"), 0, 0, 1);
                                    String mMsg = "Bad scan occured \nThis barcode is invalid";
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ActQueryScan.this);
                                    builder.setMessage(mMsg)
                                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    //do nothing
                                                }
                                            });
                                    builder.show();
                                    refreshActivity();
                                }
                                break;
                        }
                        AudioManager audioMgr = (AudioManager) getSystemService(AUDIO_SERVICE);
                        float volumeLow = audioMgr.getStreamVolume(AudioManager.STREAM_SYSTEM);
                        float volumeLevel = volumeLow / audioMgr.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
                        soundPool.play(soundId, volumeLevel, volumeLevel, 0, 0, 1);
                        //btnScan.setEnabled(true);
                        if (!btnScan.isEnabled()) {
                            btnScan.setEnabled(true);
                            btnScan.setPaintFlags(btnScan.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                        }
                    }
                }
            }
        };

        //Handle Wifi Connectivity
        ConnectivityManager connManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (!networkInfo.isConnected()) {
            final WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
            // setup a wifi configuration to our chosen network
            WifiConfiguration wc = new WifiConfiguration();
            wc.SSID = getResources().getString(R.string.ssid);
            wc.preSharedKey = getResources().getString(R.string.password);
            wc.status = WifiConfiguration.Status.ENABLED;
            wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            // connect to and enable the connection
            int netId = wifiManager.addNetwork(wc);
            wifiManager.enableNetwork(netId, true);
            wifiManager.setWifiEnabled(true);
        }

        // Initiate the navigation default turn
        NAV_TURN = R.integer.TURN_BARCODE;
        PaintButtonText();

        //Finally Authenticate User
        UserAuthenticator auth = new UserAuthenticator(ActQueryScan.this);
        currentUser = auth.getCurrentUser();   //Gets currently authenticated user
    }

    private void PaintButtonText() {
        switch (NAV_INSTRUCTION) {
            case R.integer.ACTION_BARCODE_BINQUERY:
                if (NAV_TURN == R.integer.TURN_BARCODE) {
                    //do barcode``````````````````````````````````````````````````````````````````````````````````````````````````
                    btnScan.setText(R.string.but_startbarcode);
                    btnScan.setBackgroundResource(R.drawable.button_blue);
                }
                if (NAV_TURN == R.integer.TURN_BIN) {
                    //do turn``````````````````````````````````````````````````````````````````````````````````````````````````
                    btnScan.setText(R.string.but_startbin);
                    btnScan.setBackgroundResource(R.drawable.button_yellow);
                }
                break;
            case R.integer.ACTION_BARCODEQUERY:
                btnScan.setText(R.string.but_startbarcode);
                btnScan.setBackgroundResource(R.drawable.button_blue);
                //btnScan.setBackground(getResources().getDrawable(R.drawable.button_blue)); API level > 16 Only
                //btnScan.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_blue)); depreciated
                break;
            case R.integer.ACTION_BINQUERY:
                btnScan.setText(R.string.but_startbin);
                btnScan.setBackgroundResource(R.drawable.button_yellow);
                break;
        }
    }

    private void ButtonClicked(View view) {
        switch (view.getId()) {
            case R.id.bnQryScanPerformScan:
                try {
                    if(key_start == true)
                    {
                        switch (NAV_INSTRUCTION) {
                            case R.integer.ACTION_BARCODEQUERY:
                                fullTurnCount = 0;      //set to default if it's not so already
                                if (!txtBarcode.isEnabled()) txtBarcode.setEnabled(true);
                                txtBarcode.requestFocus();
                                fullTurnCount ++;
                                break;
                            case R.integer.ACTION_BINQUERY:
                                fullTurnCount = 0;      //set to default if it's not so already
                                if (!txtBin.isEnabled()) txtBin.setEnabled(true);
                                txtBin.requestFocus();
                                fullTurnCount ++;
                                break;
                            case R.integer.ACTION_BARCODE_BINQUERY:
                                if (NAV_TURN == R.integer.TURN_BARCODE) {
                                    //do barcode ``````````````````````````````````````````````````````````````````````````````````````````````````
                                    fullTurnCount = 0;      //set to default if it's not so already
                                    lockBinControls();
                                    if (!txtBarcode.isEnabled()) txtBarcode.setEnabled(true);
                                    txtBarcode.requestFocus();
                                    fullTurnCount ++;
                                }
                                if (NAV_TURN == R.integer.TURN_BIN) {
                                    //do bin ``````````````````````````````````````````````````````````````````````````````````````````````````
                                    lockBarcodeControls();
                                    unLockBinControls();
                                    if (!txtBin.isEnabled()) txtBin.setEnabled(true);
                                    txtBin.requestFocus();
                                    fullTurnCount ++;
                                }
                                break;
                        }
                        if(Powered == false)
                        {
                            Powered = true;
                            DevCtrl.PowerOnDevice();
                        }
                        timer.cancel();
                        DevCtrl.TriggerOnDevice();
                        btnScan.setEnabled(false);
                        key_start = false;
                        retrig_timer = new Timer();
                        retrig_timer.schedule(new RetrigTask(), 3500);	//start a timer, if the data is not received within a period of time, stop the scan.
                    }
                } catch (IOException e) {
                    Log.d(TAG, "FFF");
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    today = new java.sql.Timestamp(utilDate.getTime());
                    LogEntry log = new LogEntry(1L, ApplicationID, "ButtonClicked", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                    logger.Log(log);
                }
                break;
            case R.id.bnExitActQryScan:
                if (mReadThread != null && mReadThread.isInterrupted() == false) {
                    mReadThread.interrupt();
                }
                Intent resultIntent = new Intent();
                if (backPressedParameter != null && !backPressedParameter.equalsIgnoreCase("")) {
                    setResult(1, resultIntent);
                } else {
                    setResult(RESULT_OK, resultIntent);
                }
                this.finish();
                break;
            case R.id.bnEnterBarcodeQryScan:
                manageInputByHand();
                //if (!txtBarcode.isEnabled()) txtBarcode.setEnabled(true);
                break;
            case R.id.bnEnterBincodeQryScan:
                manageInputByHand();
                //if (!txtBin.isEnabled()) txtBin.setEnabled(true);
                break;
            default:
                break;
        }
    }

    private void showSoftKeyboard() {
        InputMethodManager imm =(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        //inputMethodManager.toggleSoftInputFromWindow(linearLayout.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
        if (imm != null){
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
        }
    }

    private void manageInputByHand() {
        switch (NAV_INSTRUCTION) {
            case R.integer.ACTION_BINQUERY:
                if (inputByHand == 0) {
                    turnOnInputByHand();
                    showSoftKeyboard();
                    paintByHandButtons();
                } else {
                    turnOffInputByHand();
                    paintByHandButtons();
                    setScanInput(txtBin.getText().toString());
                    if (!getScanInput().isEmpty()) {
                        fullTurnCount ++;
                        txtBin.setText(getScanInput());     // just to trigger text changed
                    }
                }
                break;
            case R.integer.ACTION_BARCODEQUERY:
                if (inputByHand == 0) {
                    turnOnInputByHand();
                    showSoftKeyboard();
                    paintByHandButtons();
                } else {
                    turnOffInputByHand();
                    paintByHandButtons();
                    setScanInput(txtBarcode.getText().toString());
                    if (!getScanInput().isEmpty()) {
                        fullTurnCount ++;
                        txtBarcode.setText(getScanInput());     // just to trigger text changed
                    }
                }
                break;
            case R.integer.ACTION_BARCODE_BINQUERY:
                switch (NAV_TURN) {
                    case R.integer.TURN_BIN:
                        //1st time = turn on, 2nd FINISH
                        if (inputByHand == 0) {
                            turnOnInputByHand();
                            showSoftKeyboard();
                            paintByHandButtons();
                        } else {
                            turnOffInputByHand();
                            paintByHandButtons();
                            if (!btnEnterBarcode.isEnabled()) {
                                btnEnterBarcode.setEnabled(true);
                                btnEnterBarcode.setPaintFlags(btnEnterBarcode.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                            }
                            if (btnEnterBincode.isEnabled()) {
                                btnEnterBincode.setEnabled(false);
                                btnEnterBincode.setPaintFlags(btnEnterBincode.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                            }
                            setScanInput(txtBin.getText().toString());
                            if (!getScanInput().isEmpty()) {
                                fullTurnCount ++;
                                txtBin.setText(getScanInput());     // just to trigger text changed
                                paintByHandButtons();
                                txtBarcode.requestFocus();
                                lockBinControls();
                                unLockBarcodeControls();
                            }
                            //NAV_TURN = R.integer.TURN_END;
                        }
                        break;
                    case R.integer.TURN_BARCODE:
                        //1st time = turn on, 2nd change NAV-Turn
                        if (inputByHand == 0) {
                            turnOnInputByHand();
                            showSoftKeyboard();
                            paintByHandButtons();
                        } else {
                            turnOffInputByHand();
                            if (!btnEnterBincode.isEnabled()) {
                                btnEnterBincode.setEnabled(true);
                                btnEnterBincode.setPaintFlags(btnEnterBincode.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                            }
                            //Disable Enter Barcode button
                            if (btnEnterBarcode.isEnabled()) {
                                btnEnterBarcode.setEnabled(false);
                                btnEnterBarcode.setPaintFlags(btnEnterBarcode.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                            }
                            setScanInput(txtBarcode.getText().toString());
                            if (!getScanInput().isEmpty()) {
                                fullTurnCount ++;
                                txtBarcode.setText(getScanInput());     // just to trigger text changed
                                paintByHandButtons();
                                txtBin.requestFocus();
                                lockBarcodeControls();
                                unLockBinControls();
                            }
                            //NAV_TURN = R.integer.TURN_BIN;
                        }
                        break;
                }
                break;
        }
    }

    private void turnOnInputByHand(){
        this.inputByHand = 1;    //Turn On Input by Hand
        //this.btnScan.setEnabled(false);
    }

    private void turnOffInputByHand(){
        this.inputByHand = 0;    //Turn On Input by Hand
        //this.btnScan.setEnabled(false);  //
//        switch (NAV_INSTRUCTION) {
//            case R.integer.ACTION_BINQUERY:
//                //do
//                break;
//            case R.integer.ACTION_BARCODEQUERY:
//                //do
//                break;
//            case R.integer.ACTION_BARCODE_BINQUERY:
//                switch (NAV_TURN) {
//                    case R.integer.TURN_BIN:
//                        //do
//                        break;
//                    case R.integer.TURN_BARCODE:
//                        //do
//                        break;
//                }
//                break;
//        }
    }

    private void paintByHandButtons() {
        final String byHand = "ByHand";
        final String finish = "Finish";
        switch (NAV_INSTRUCTION) {
            case R.integer.ACTION_BINQUERY:
                if (inputByHand == 0) {
                    btnEnterBincode.setText(finish);
                } else {
                    btnEnterBincode.setText(byHand);
                }
                break;
            case R.integer.ACTION_BARCODEQUERY:
                if (inputByHand == 0) {
                    btnEnterBincode.setText(finish);
                } else {
                    btnEnterBincode.setText(byHand);
                }
                break;
            case R.integer.ACTION_BARCODE_BINQUERY:
                switch (NAV_TURN) {
                    case R.integer.TURN_BIN:
                        //1st time = turn on, 2nd FINISH
                        if (inputByHand == 0) {
                            btnEnterBincode.setText(byHand);
                        } else {
                            btnEnterBincode.setText(finish);
                        }
                        break;
                    case R.integer.TURN_BARCODE:
                        //1st time = turn on, 2nd change NAV-Turn
                        if (inputByHand == 0) {
                            btnEnterBarcode.setText(byHand);
                        } else {
                            btnEnterBarcode.setText(finish);
                        }
                        break;
                }
                break;
        }
    }

    private void lockBarcodeControls() {
        if (lblBarcode.isEnabled()) lblBarcode.setEnabled(false);
        if (txtBarcode.isEnabled()) txtBarcode.setEnabled(false);
        if (btnEnterBarcode.isEnabled()) {
            btnEnterBarcode.setEnabled(false);
            btnEnterBarcode.setPaintFlags(btnEnterBarcode.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }

    private void unLockBarcodeControls() {
        if (!lblBarcode.isEnabled()) lblBarcode.setEnabled(true);
        if (!txtBarcode.isEnabled()) txtBarcode.setEnabled(true);
        if (!btnEnterBarcode.isEnabled()) {
            btnEnterBarcode.setEnabled(true);
            btnEnterBarcode.setPaintFlags(btnEnterBarcode.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    private void lockBinControls() {
        if (lblBin.isEnabled()) lblBin.setEnabled(false);
        if (txtBin.isEnabled()) txtBin.setEnabled(false);
        if (btnEnterBincode.isEnabled()) {
            btnEnterBincode.setEnabled(false);
            btnEnterBincode.setPaintFlags(btnEnterBincode.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }

    private void unLockBinControls() {
        if (!lblBin.isEnabled()) lblBin.setEnabled(true);
        if (!txtBin.isEnabled()) txtBin.setEnabled(true);
        if (!btnEnterBincode.isEnabled()) {
            btnEnterBincode.setEnabled(true);
            btnEnterBincode.setPaintFlags(btnEnterBincode.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    private void lockAllControls() {
        lockBarcodeControls();
        lockBinControls();

        if (btnScan.isEnabled()) {
            btnScan.setEnabled(false);
            btnScan.setPaintFlags(btnScan.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }

    private void refreshActivity() {
        switch (NAV_INSTRUCTION) {
            case R.integer.ACTION_BINQUERY:
                if (lblBarcode.getVisibility() == View.VISIBLE) lblBarcode.setVisibility(View.GONE);
                if (txtBarcode.getVisibility() == View.VISIBLE) txtBarcode.setVisibility(View.GONE);
                if (btnEnterBarcode.getVisibility() == View.VISIBLE) btnEnterBarcode.setVisibility(View.GONE);
                if (lblBin.getVisibility() != View.VISIBLE) lblBin.setVisibility(View.VISIBLE);
                if (txtBin.getVisibility() != View.VISIBLE) txtBin.setVisibility(View.VISIBLE);
                if (btnEnterBincode.getVisibility() != View.VISIBLE) btnEnterBincode.setVisibility(View.VISIBLE);
                if (!txtBarcode.getText().toString().isEmpty()) txtBarcode.setText("");
                if (!txtBin.getText().toString().equalsIgnoreCase("")) txtBin.setText("");
                break;
            case R.integer.ACTION_BARCODEQUERY:
                if (lblBin.getVisibility() == View.VISIBLE) lblBin.setVisibility(View.GONE);
                if (txtBin.getVisibility() == View.VISIBLE) txtBin.setVisibility(View.GONE);
                if (btnEnterBincode.getVisibility() == View.VISIBLE) btnEnterBincode.setVisibility(View.GONE);
                if (lblBarcode.getVisibility() != View.VISIBLE) lblBarcode.setVisibility(View.VISIBLE);
                if (txtBarcode.getVisibility() != View.VISIBLE) txtBarcode.setVisibility(View.VISIBLE);
                if (btnEnterBarcode.getVisibility() != View.VISIBLE) btnEnterBarcode.setVisibility(View.VISIBLE);
                if (!txtBarcode.getText().toString().equalsIgnoreCase("")) txtBarcode.setText("");
                if (!txtBin.getText().toString().equalsIgnoreCase("")) txtBin.setText("");
                break;
            case R.integer.ACTION_BARCODE_BINQUERY:
//                if (txtBin.getVisibility() != View.VISIBLE) txtBin.setVisibility(View.VISIBLE);
//                if (txtBin.getVisibility() != View.VISIBLE) txtBin.setVisibility(View.VISIBLE);
//                if (btnEnterBincode.getVisibility() != View.VISIBLE) btnEnterBincode.setVisibility(View.VISIBLE);
//                lockBinControls();  // disables lblBin & txtBin
//                if (lblBarcode.getVisibility() != View.VISIBLE) lblBarcode.setVisibility(View.VISIBLE);
//                if (txtBarcode.getVisibility() != View.VISIBLE) txtBarcode.setVisibility(View.VISIBLE);
//                if (btnEnterBarcode.getVisibility() != View.VISIBLE) btnEnterBarcode.setVisibility(View.VISIBLE);
//                if (!txtBarcode.getText().toString().equalsIgnoreCase("")) txtBarcode.setText("");
//                if (!txtBin.getText().toString().equalsIgnoreCase("")) txtBin.setText("");
                if (!txtBarcode.getText().toString().isEmpty()) txtBarcode.setText("");
                if (!txtBin.getText().toString().equalsIgnoreCase("")) txtBin.setText("");
                switch (NAV_TURN) {
                    case R.integer.TURN_BARCODE:
                        lockBinControls();  // disables lblBin & txtBin
                        if (lblBarcode.getVisibility() != View.VISIBLE) lblBarcode.setVisibility(View.VISIBLE);
                        if (txtBarcode.getVisibility() != View.VISIBLE) txtBarcode.setVisibility(View.VISIBLE);
                        // Enable barcode button controls to enable manual barcode entry
                        if (!btnEnterBarcode.isEnabled()) {
                            btnEnterBarcode.setEnabled(true);
                            btnEnterBarcode.setPaintFlags(btnEnterBarcode.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                            lockBinControls();
                            if (btnEnterBincode.isEnabled()) {
                                btnEnterBincode.setEnabled(false);
                                btnEnterBincode.setPaintFlags(btnEnterBincode.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                            }
                        }
                        break;
                    case R.integer.TURN_BIN:
                        lockBarcodeControls();
                        if (txtBin.getVisibility() != View.VISIBLE) txtBin.setVisibility(View.VISIBLE);
                        if (txtBin.getVisibility() != View.VISIBLE) txtBin.setVisibility(View.VISIBLE);
                        // Enable binCode button controls to enable manual barcode entry
                        if (!btnEnterBincode.isEnabled()) {
                            btnEnterBincode.setEnabled(true);
                            btnEnterBincode.setPaintFlags(btnEnterBincode.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                            lockBarcodeControls();
                            if (btnEnterBarcode.isEnabled()) {
                                btnEnterBarcode.setEnabled(false);
                                btnEnterBarcode.setPaintFlags(btnEnterBarcode.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                            }
                        }
                        break;
                }
                break;
        }
        if (!btnScan.isEnabled()) btnScan.setEnabled(true); btnScan.setPaintFlags(btnScan.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        NAV_TURN = R.integer.TURN_BARCODE;
        PaintButtonText();
    }

    private void tidyControls() {
        switch (NAV_INSTRUCTION) {
            case R.integer.ACTION_BINQUERY:
                if (txtBin.getText() != null && !txtBin.getText().toString().equalsIgnoreCase("")) btnScan.setEnabled(false);
                break;
            case R.integer.ACTION_BARCODEQUERY:
                if (txtBarcode.getText() != null && !txtBarcode.getText().toString().equalsIgnoreCase("")) btnScan.setEnabled(false);
                break;
            case R.integer.ACTION_BARCODE_BINQUERY:
                if (txtBarcode.getText() != null && !txtBarcode.getText().toString().equalsIgnoreCase("")) {
                    if (!btnScan.isEnabled()) btnScan.setEnabled(true);
                }
                if (txtBin.getText() != null && !txtBin.getText().toString().equalsIgnoreCase("")) {
                    if (btnScan.isEnabled()) btnScan.setEnabled(false);
                }
                break;
            default:
                break;
        }
    }

    class MyTask extends TimerTask {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            Message message = new Message();
            message.what = 1;
            t_handler.sendMessage(message);
        }
    }

    class RetrigTask extends TimerTask {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            //startTime = System.currentTimeMillis(); // begin long process time elapse count
            Message message = new Message();
            message.what = 1;
            n_handler.sendMessage(message);
        }
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        if(ops == true)
        {
            mReadThread.interrupt();
            timer.cancel();
            retrig_timer.cancel();
            try {
                DevCtrl.PowerOffDevice();
                Thread.sleep(1000);
            } catch (IOException e) {
                Log.d(TAG, "CCC");
                // TODO Auto-generated catch block
                e.printStackTrace();
                today = new java.sql.Timestamp(utilDate.getTime());
                LogEntry log = new LogEntry(1L, ApplicationID, "ActQueryScan - onPause", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                logger.Log(log);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                today = new java.sql.Timestamp(utilDate.getTime());
                LogEntry log = new LogEntry(1L, ApplicationID, "ActQueryScan - onPause", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                logger.Log(log);
            }
            Powered = false;
            if(Opened == true)
            {
                mSerialPort.close(fd);
                Opened = false;
            }
        }
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        if(ops == true)
        {
            if(Opened == false) {
                try {
                    //mSerialPort = new SerialPort("/dev/eser1",9600);//3a
                    mSerialPort = new SerialPort("/dev/eser0",9600);//35
                } catch (SecurityException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    today = new java.sql.Timestamp(utilDate.getTime());
                    LogEntry log = new LogEntry(1L, ApplicationID, "ActQueryScan - onResume", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                    logger.Log(log);
                } catch (IOException e) {
                    Log.d(TAG, "DDD");
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    today = new java.sql.Timestamp(utilDate.getTime());
                    LogEntry log = new LogEntry(1L, ApplicationID, "ActQueryScan - onResume", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                    logger.Log(log);
                    new AlertDialog.Builder(this).setTitle(R.string.DIA_ALERT).setMessage(R.string.DEV_OPEN_ERR).setPositiveButton(R.string.DIA_CHECK, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            finish();
                        }
                    }).show();
                    ops = false;
                    soundPool.release();
                    try {
                        DevCtrl.DeviceClose();
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                        today = new java.sql.Timestamp(utilDate.getTime());
                        LogEntry log1 = new LogEntry(1L, ApplicationID, "ActQueryScan - onResume", deviceIMEI, e1.getClass().getSimpleName(), e1.getMessage(), today);
                        logger.Log(log1);
                    }
                    super.onResume();
                    return;
                }
                fd = mSerialPort.getFd();
                if(fd > 0){
                    Log.d(TAG,"opened");
                    Opened = true;
                }
            }
            mReadThread = new ReadThread();
            mReadThread.setName("MyReadThread_ActQueryScan");
            mReadThread.start();
        }
        Log.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        if(ops == true)
        {
            try {
                soundPool.release();
                DevCtrl.DeviceClose();
            } catch (IOException e) {
                Log.d(TAG, "EEE");
                // TODO Auto-generated catch block
                e.printStackTrace();
                today = new java.sql.Timestamp(utilDate.getTime());
                LogEntry log = new LogEntry(1L, ApplicationID, "ActQueryScan - onDestroy", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                logger.Log(log);
            }
        }
        super.onDestroy();
        //android.os.Process.killProcess(android.os.Process.myPid()); Since it's not longer main entry then we're not killing app *LEBEL*
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 0) {
            //tidyControls();  //clear all controls
            if (wsTask != null && wsTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
                wsTask.cancel(true);
            }
            NAV_INSTRUCTION = R.integer.ACTION_BARCODE_BINQUERY;
            refreshActivity();
        }
        if (resultCode == RESULT_OK) {
            //tidyControls();  //clear all controls
            if (wsTask != null && wsTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
                wsTask.cancel(true);
            }
            NAV_INSTRUCTION = R.integer.ACTION_BARCODE_BINQUERY;
            refreshActivity();
        }
        //New Code Direct the activity to just close itself
        Intent resultIntent = new Intent();
        if (backPressedParameter != null && !backPressedParameter.equalsIgnoreCase("")) {
            setResult(1, resultIntent);
        } else {
            setResult(RESULT_OK, resultIntent);
        }
        com.android.barcode.ActQueryScan.this.finish();
    }

    class TextChanged implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            // TODO Auto-generated method stub

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (fullTurnCount > 0) {
                if (s != null && !s.toString().equalsIgnoreCase("")) {
                    String eanCode = s.toString().trim();
//                    int allAccepted[] = {5,12,13,14};
//                    if (eanCode.length() > 0 && !(Arrays.binarySearch(allAccepted, eanCode.length()) == -1)) {
//                    }
                    if (inputByHand == 0) {

                        switch (NAV_INSTRUCTION) {
                            case R.integer.ACTION_BARCODEQUERY:
                                int acceptableA[] = {12,13,14};
                                if (eanCode.length() > 0 && !(Arrays.binarySearch(acceptableA, eanCode.length()) == -1)) {

                                    //Authenticate current user, Build Message only when all these conditions are right then proceed with asyncTask
                                    UserAuthenticator auth = new UserAuthenticator(ActQueryScan.this);
                                    currentUser = currentUser != null ? currentUser : auth.getCurrentUser();   //Gets currently authenticated user
                                    if (currentUser != null) {

                                        wsTask = new WebServiceTask();
                                        wsTask.execute(String.format("%s", NAV_INSTRUCTION), eanCode);
                                    } else {
                                        soundPool.play(errorSoundId, 1, 1, 0, 0, 1);
                                        Vibrator vib = (Vibrator) ActQueryScan.this.getSystemService(Context.VIBRATOR_SERVICE);
                                        // Vibrate for 500 milliseconds
                                        vib.vibrate(2000);
                                        String mMsg = "User not Authenticated \nPlease login";
                                        AlertDialog.Builder builder = new AlertDialog.Builder(ActQueryScan.this);
                                        builder.setMessage(mMsg)
                                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        //do nothing
                                                    }
                                                });
                                        builder.show();
                                    }
                                } else {
                                    //Check to see if we're making entry by hand
                                    if (inputByHand == 0) {
                                        new AlertDialog.Builder(ActQueryScan.this).setTitle(R.string.DIA_ALERT).setMessage(R.string.DEV_EAN_ERR).setPositiveButton(R.string.DIA_CHECK, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // TODO Auto-generated method stub
                                                refreshActivity();
                                            }
                                        }).show();
                                    }
                                }
                                break;
                            case R.integer.ACTION_BINQUERY:
                                if (eanCode.length() > 0 && (eanCode.length() == 5)) {
                                    //Authenticate current user, Build Message only when all these conditions are right then proceed with asyncTask
                                    UserAuthenticator auth = new UserAuthenticator(ActQueryScan.this);
                                    currentUser = currentUser != null ? currentUser : auth.getCurrentUser();   //Gets currently authenticated user
                                    if (currentUser != null) {

                                        wsTask = new WebServiceTask();
                                        wsTask.execute(String.format("%s", NAV_INSTRUCTION), eanCode);
                                    } else {
                                        soundPool.play(errorSoundId, 1, 1, 0, 0, 1);
                                        Vibrator vib = (Vibrator) ActQueryScan.this.getSystemService(Context.VIBRATOR_SERVICE);
                                        // Vibrate for 500 milliseconds
                                        vib.vibrate(2000);
                                        String mMsg = "User not Authenticated \nPlease login";
                                        AlertDialog.Builder builder = new AlertDialog.Builder(ActQueryScan.this);
                                        builder.setMessage(mMsg)
                                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        //do nothing
                                                    }
                                                });
                                        builder.show();
                                    }
                                } else {
                                    //Check to see if we're making entry by hand
                                    if (inputByHand == 0) {
                                        new AlertDialog.Builder(ActQueryScan.this).setTitle(R.string.DIA_ALERT).setMessage(R.string.DEV_BIN_ERR).setPositiveButton(R.string.DIA_CHECK, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                refreshActivity();
                                            }
                                        }).show();
                                    }
                                }
                                break;
                            case R.integer.ACTION_BARCODE_BINQUERY:
                                if (NAV_TURN == R.integer.TURN_BIN) {
                                    //do bin``````````````````````````````````````````````````````````````````````````````````````````````````
                                    lockBarcodeControls();
                                    if (eanCode.length() > 0 && (eanCode.length() == 5)) {
                                        //Authenticate current user, Build Message only when all these conditions are right then proceed with asyncTask
                                        UserAuthenticator auth = new UserAuthenticator(ActQueryScan.this);
                                        currentUser = currentUser != null ? currentUser : auth.getCurrentUser();   //Gets currently authenticated user
                                        if (currentUser != null) {

                                            currentBincode = eanCode;

                                            wsTask = new WebServiceTask();
                                            wsTask.execute(String.format("%s", NAV_INSTRUCTION), currentBarcode, eanCode);

                                            //Finally end turn by locking all input controls
                                            //lockAllControls();
                                        } else {
                                            soundPool.play(errorSoundId, 1, 1, 0, 0, 1);
                                            Vibrator vib = (Vibrator) ActQueryScan.this.getSystemService(Context.VIBRATOR_SERVICE);
                                            // Vibrate for 500 milliseconds
                                            vib.vibrate(2000);
                                            String mMsg = "User not Authenticated \nPlease login";
                                            AlertDialog.Builder builder = new AlertDialog.Builder(ActQueryScan.this);
                                            builder.setMessage(mMsg)
                                                    .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            //do nothing
                                                        }
                                                    });
                                            builder.show();
                                        }
                                    } else {
                                        //Check to see if we're making entry by hand
                                        if (inputByHand == 0) {
                                            new AlertDialog.Builder(ActQueryScan.this).setTitle(R.string.DIA_ALERT).setMessage(R.string.DEV_BIN_ERR).setPositiveButton(R.string.DIA_CHECK, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    refreshActivity();
                                                }
                                            }).show();
                                        }
                                    }
                                }
                                if (NAV_TURN == R.integer.TURN_BARCODE) {
                                    //do barcode ``````````````````````````````````````````````````````````````````````````````````````````````````
                                    lockBinControls();
                                    int acceptableB[] = {12,13,14};
                                    if (eanCode.length() > 0 && !(Arrays.binarySearch(acceptableB, eanCode.length()) == -1)) {


                                        //Authenticate current user, Build Message only when all these conditions are right then proceed with asyncTask
                                        UserAuthenticator auth = new UserAuthenticator(ActQueryScan.this);
                                        currentUser = currentUser != null ? currentUser : auth.getCurrentUser();   //Gets currently authenticated user
                                        if (currentUser != null) {

                                            //wsTask = new WebServiceTask();
                                            //wsTask.execute(String.format("%s", NAV_INSTRUCTION), eanCode);
                                            currentBarcode = eanCode;
                                            //Finally switch turn (bin)
                                            NAV_TURN = R.integer.TURN_BIN;
                                            if (!btnEnterBincode.isEnabled()) btnEnterBincode.setEnabled(true);
                                            PaintButtonText();
                                        } else {
                                            soundPool.play(errorSoundId, 1, 1, 0, 0, 1);
                                            Vibrator vib = (Vibrator) ActQueryScan.this.getSystemService(Context.VIBRATOR_SERVICE);
                                            // Vibrate for 500 milliseconds
                                            vib.vibrate(2000);
                                            String mMsg = "User not Authenticated \nPlease login";
                                            AlertDialog.Builder builder = new AlertDialog.Builder(ActQueryScan.this);
                                            builder.setMessage(mMsg)
                                                    .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            //do nothing
                                                        }
                                                    });
                                            builder.show();
                                        }
                                    } else {
                                        //Check to see if we're making entry by hand
                                        if (inputByHand == 0) {
                                            new AlertDialog.Builder(ActQueryScan.this).setTitle(R.string.DIA_ALERT).setMessage(R.string.DEV_EAN_ERR).setPositiveButton(R.string.DIA_CHECK, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    // TODO Auto-generated method stub
                                                    refreshActivity();
                                                }
                                            }).show();
                                        }
                                    }
                                } else {
//                                new AlertDialog.Builder(ActQueryScan.this).setTitle(R.string.DIA_ALERT).setMessage(R.string.DEV_EAN_ERR).setPositiveButton(R.string.DIA_CHECK, new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        refreshActivity();
//                                    }
//                                }).show();
                                }
                                break;
                        }

                        //End full turn
                        fullTurnCount = 0;
                    }

                }
                //fullTurnCount = 0;  old code
            }
        }
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KEY_SCAN:
                if(KEY_POSITION == 0) {
                    try {
                        if(key_start == true)
                        {
                            switch (NAV_INSTRUCTION) {
                                case R.integer.ACTION_BARCODEQUERY:
                                    //if (txtBarcode.getVisibility() != View.VISIBLE) txtBarcode.setVisibility(View.VISIBLE);
                                    if (!txtBarcode.isEnabled()) txtBarcode.setEnabled(true);
                                    txtBarcode.requestFocus();
                                    break;
                                case R.integer.ACTION_BINQUERY:
                                    if (!txtBin.isEnabled()) txtBin.setEnabled(true);
                                    txtBin.requestFocus();
                                    break;
                                case R.integer.ACTION_BARCODE_BINQUERY:
                                    if (NAV_TURN == R.integer.TURN_BARCODE) {
                                        //do barcode ``````````````````````````````````````````````````````````````````````````````````````````````````
                                        lockBinControls();
                                        if (!txtBarcode.isEnabled()) txtBarcode.setEnabled(true);
                                        txtBarcode.requestFocus();
                                    }
                                    if (NAV_TURN == R.integer.TURN_BIN) {
                                        //do bin ``````````````````````````````````````````````````````````````````````````````````````````````````
                                        lockBarcodeControls();
                                        if (!txtBin.isEnabled()) txtBin.setEnabled(true);
                                        txtBin.requestFocus();
                                    }
                                    break;
                            }
                            if(Powered == false)
                            {
                                Powered = true;
                                DevCtrl.PowerOnDevice();
                            }
                            timer.cancel();
                            DevCtrl.TriggerOnDevice();
                            btnScan.setEnabled(false);
                            key_start = false;
                            retrig_timer = new Timer();
                            retrig_timer.schedule(new RetrigTask(), 3500);	//start a timer, if the data is not received within a period of time, stop the btnScan.

                        }
                    } catch (IOException e) {
                        Log.d(TAG, "FFF");
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        today = new java.sql.Timestamp(utilDate.getTime());
                        LogEntry log = new LogEntry(1L, ApplicationID, "ActQueryScan - onKeyLongPress", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                        logger.Log(log);
                    }
                }
                break;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KEY_SCAN:
                if(KEY_POSITION == 0){
                    try {
                        if(key_start == true)
                        {
                            switch (NAV_INSTRUCTION) {
                                case R.integer.ACTION_BARCODEQUERY:
                                    //if (txtBarcode.getVisibility() != View.VISIBLE) txtBarcode.setVisibility(View.VISIBLE);
                                    if (!txtBarcode.isEnabled()) txtBarcode.setEnabled(true);
                                    txtBarcode.requestFocus();
                                    break;
                                case R.integer.ACTION_BINQUERY:
                                    if (!txtBin.isEnabled()) txtBin.setEnabled(true);
                                    txtBin.requestFocus();
                                    break;
                                case R.integer.ACTION_BARCODE_BINQUERY:
                                    if (NAV_TURN == R.integer.TURN_BARCODE) {
                                        //do barcode ``````````````````````````````````````````````````````````````````````````````````````````````````
                                        lockBinControls();
                                        if (!txtBarcode.isEnabled()) txtBarcode.setEnabled(true);
                                        txtBarcode.requestFocus();
                                    }
                                    if (NAV_TURN == R.integer.TURN_BIN) {
                                        //do bin ``````````````````````````````````````````````````````````````````````````````````````````````````
                                        lockBarcodeControls();
                                        if (!txtBin.isEnabled()) txtBin.setEnabled(true);
                                        txtBin.requestFocus();
                                    }
                                    break;
                            }
                            if(Powered == false)
                            {
                                Powered = true;
                                DevCtrl.PowerOnDevice();
                            }
                            timer.cancel();
                            DevCtrl.TriggerOnDevice();
                            btnScan.setEnabled(false);
                            key_start = false;
                            retrig_timer = new Timer();
                            retrig_timer.schedule(new RetrigTask(), 3500);	//start a timer, if the data is not received within a period of time, stop the btnScan.

                        }
                    } catch (IOException e) {
                        Log.d(TAG, "FFF");
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        today = new java.sql.Timestamp(utilDate.getTime());
                        LogEntry log = new LogEntry(1L, ApplicationID, "ActQueryScan - onKeyDown", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                        logger.Log(log);
                    }
                }
                break;
            case KEY_YELLOW:
                if (KEY_POSITION == 0) {
                    try {
                        if(key_start == true)
                        {
                            switch (NAV_INSTRUCTION) {
                                case R.integer.ACTION_BARCODEQUERY:
                                    //if (txtBarcode.getVisibility() != View.VISIBLE) txtBarcode.setVisibility(View.VISIBLE);
                                    fullTurnCount = 0;      //set to default if it's not so already
                                    if (!txtBarcode.isEnabled()) txtBarcode.setEnabled(true);
                                    txtBarcode.requestFocus();
                                    fullTurnCount ++;
                                    break;
                                case R.integer.ACTION_BINQUERY:
                                    fullTurnCount = 0;      //set to default if it's not so already
                                    if (!txtBin.isEnabled()) txtBin.setEnabled(true);
                                    txtBin.requestFocus();
                                    fullTurnCount ++;
                                    break;
                                case R.integer.ACTION_BARCODE_BINQUERY:
                                    if (NAV_TURN == R.integer.TURN_BARCODE) {
                                        //do barcode ``````````````````````````````````````````````````````````````````````````````````````````````````
                                        fullTurnCount = 0;      //set to default if it's not so already
                                        lockBinControls();
                                        if (!txtBarcode.isEnabled()) txtBarcode.setEnabled(true);
                                        txtBarcode.requestFocus();
                                        fullTurnCount ++;
                                    }
                                    if (NAV_TURN == R.integer.TURN_BIN) {
                                        //do bin ``````````````````````````````````````````````````````````````````````````````````````````````````
                                        lockBarcodeControls();
                                        if (!txtBin.isEnabled()) txtBin.setEnabled(true);
                                        txtBin.requestFocus();
                                        fullTurnCount ++;
                                    }
                                    break;
                            }
                            if(Powered == false)
                            {
                                Powered = true;
                                DevCtrl.PowerOnDevice();
                            }
                            timer.cancel();
                            DevCtrl.TriggerOnDevice();
                            btnScan.setEnabled(false);
                            key_start = false;
                            retrig_timer = new Timer();
                            retrig_timer.schedule(new RetrigTask(), 3500);	//start a timer, if the data is not received within a period of time, stop the scan.
                        }
                    } catch (IOException e) {
                        Log.d(TAG, "FFF");
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        today = new java.sql.Timestamp(utilDate.getTime());
                        LogEntry log = new LogEntry(1L, ApplicationID, "ButtonClicked", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                        logger.Log(log);
                    }
                }
                break;
            case KEY_F1:
                if(KEY_POSITION == 1){
                    try {
                        if(key_start == true)
                        {
                            switch (NAV_INSTRUCTION) {
                                case R.integer.ACTION_BARCODEQUERY:
                                    //if (txtBarcode.getVisibility() != View.VISIBLE) txtBarcode.setVisibility(View.VISIBLE);
                                    if (!txtBarcode.isEnabled()) txtBarcode.setEnabled(true);
                                    txtBarcode.requestFocus();
                                    break;
                                case R.integer.ACTION_BINQUERY:
                                    if (!txtBin.isEnabled()) txtBin.setEnabled(true);
                                    txtBin.requestFocus();
                                    break;
                                case R.integer.ACTION_BARCODE_BINQUERY:
                                    if (NAV_TURN == R.integer.TURN_BARCODE) {
                                        //do barcode ``````````````````````````````````````````````````````````````````````````````````````````````````
                                        lockBinControls();
                                        if (!txtBarcode.isEnabled()) txtBarcode.setEnabled(true);
                                        txtBarcode.requestFocus();
                                    }
                                    if (NAV_TURN == R.integer.TURN_BIN) {
                                        //do bin ``````````````````````````````````````````````````````````````````````````````````````````````````
                                        lockBarcodeControls();
                                        if (!txtBin.isEnabled()) txtBin.setEnabled(true);
                                        txtBin.requestFocus();
                                    }
                                    break;
                            }
                            if(Powered == false)
                            {
                                Powered = true;
                                DevCtrl.PowerOnDevice();
                            }
                            timer.cancel();
                            DevCtrl.TriggerOnDevice();
                            btnScan.setEnabled(false);
                            key_start = false;
                            retrig_timer = new Timer();
                            retrig_timer.schedule(new RetrigTask(), 3500);	//start a timer, if the data is not received within a period of time, stop the btnScan.
                        }
                    } catch (IOException e) {
                        Log.d(TAG, "FFF");
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        today = new java.sql.Timestamp(utilDate.getTime());
                        LogEntry log = new LogEntry(1L, ApplicationID, "ActQueryScan - onKeyDown", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                        logger.Log(log);
                    }
                }
                break;
            case KEY_F2:
                if(KEY_POSITION == 2){
                    try {
                        if(key_start == true)
                        {
                            switch (NAV_INSTRUCTION) {
                                case R.integer.ACTION_BARCODEQUERY:
                                    //if (txtBarcode.getVisibility() != View.VISIBLE) txtBarcode.setVisibility(View.VISIBLE);
                                    if (!txtBarcode.isEnabled()) txtBarcode.setEnabled(true);
                                    txtBarcode.requestFocus();
                                    break;
                                case R.integer.ACTION_BINQUERY:
                                    if (!txtBin.isEnabled()) txtBin.setEnabled(true);
                                    txtBin.requestFocus();
                                    break;
                                case R.integer.ACTION_BARCODE_BINQUERY:
                                    if (NAV_TURN == R.integer.TURN_BARCODE) {
                                        //do barcode ``````````````````````````````````````````````````````````````````````````````````````````````````
                                        lockBinControls();
                                        if (!txtBarcode.isEnabled()) txtBarcode.setEnabled(true);
                                        txtBarcode.requestFocus();
                                    }
                                    if (NAV_TURN == R.integer.TURN_BIN) {
                                        //do bin ``````````````````````````````````````````````````````````````````````````````````````````````````
                                        lockBarcodeControls();
                                        if (!txtBin.isEnabled()) txtBin.setEnabled(true);
                                        txtBin.requestFocus();
                                    }
                                    break;
                            }
                            if(Powered == false)
                            {
                                Powered = true;
                                DevCtrl.PowerOnDevice();
                            }
                            timer.cancel();
                            DevCtrl.TriggerOnDevice();
                            btnScan.setEnabled(false);
                            key_start = false;
                            retrig_timer = new Timer();
                            retrig_timer.schedule(new RetrigTask(), 3500);	//start a timer, if the data is not received within a period of time, stop the btnScan.
                        }
                    } catch (IOException e) {
                        Log.d(TAG, "FFF");
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        today = new java.sql.Timestamp(utilDate.getTime());
                        LogEntry log = new LogEntry(1L, ApplicationID, "ActQueryScan - onKeyDown", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                        logger.Log(log);
                    }
                }
                break;
            case KEY_F3:
                if(KEY_POSITION == 3){
                    try {
                        if(key_start == true)
                        {
                            switch (NAV_INSTRUCTION) {
                                case R.integer.ACTION_BARCODEQUERY:
                                    //if (txtBarcode.getVisibility() != View.VISIBLE) txtBarcode.setVisibility(View.VISIBLE);
                                    if (!txtBarcode.isEnabled()) txtBarcode.setEnabled(true);
                                    txtBarcode.requestFocus();
                                    break;
                                case R.integer.ACTION_BINQUERY:
                                    if (!txtBin.isEnabled()) txtBin.setEnabled(true);
                                    txtBin.requestFocus();
                                    break;
                                case R.integer.ACTION_BARCODE_BINQUERY:
                                    if (NAV_TURN == R.integer.TURN_BARCODE) {
                                        //do barcode ``````````````````````````````````````````````````````````````````````````````````````````````````
                                        lockBinControls();
                                        if (!txtBarcode.isEnabled()) txtBarcode.setEnabled(true);
                                        txtBarcode.requestFocus();
                                    }
                                    if (NAV_TURN == R.integer.TURN_BIN) {
                                        //do bin ``````````````````````````````````````````````````````````````````````````````````````````````````
                                        lockBarcodeControls();
                                        if (!txtBin.isEnabled()) txtBin.setEnabled(true);
                                        txtBin.requestFocus();
                                    }
                                    break;
                            }
                            if(Powered == false)
                            {
                                Powered = true;
                                DevCtrl.PowerOnDevice();
                            }
                            timer.cancel();
                            DevCtrl.TriggerOnDevice();
                            btnScan.setEnabled(false);
                            key_start = false;
                            retrig_timer = new Timer();
                            retrig_timer.schedule(new RetrigTask(), 3500);	//start a timer, if the data is not received within a period of time, stop the btnScan.
                        }
                    } catch (IOException e) {
                        Log.d(TAG, "FFF");
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        today = new java.sql.Timestamp(utilDate.getTime());
                        LogEntry log = new LogEntry(1L, ApplicationID, "ActQueryScan - onKeyDown", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                        logger.Log(log);
                    }
                }
                break;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (mReadThread != null && mReadThread.isInterrupted() == false) {
            mReadThread.interrupt();
        }
        Intent resultIntent = new Intent();
        if (backPressedParameter != null && !backPressedParameter.equalsIgnoreCase("")) {
            setResult(1, resultIntent);
        } else {
            setResult(RESULT_OK, resultIntent);
        }
        com.android.barcode.ActQueryScan.this.finish();
    }

    private class ReadThread extends Thread {

        @Override
        public void run() {
            super.run();
            while(!isInterrupted()) {
                try {
                    Log.d(TAG,"read");
                    String buff = new String();
                    buff = mSerialPort.ReadSerial(fd, 1024);
                    Log.d(TAG,"end");
                    if(buff != null){
                        Message msg = new Message();
                        msg.what = 1;
                        msg.obj = buff;
                        handler.sendMessage(msg);
                        timer = new Timer();
                        timer.schedule(new MyTask(), 60000);
                    }else{
                        Message msg = new Message();
                        msg.what = 0;
                        handler.sendMessage(msg);
                    }
                } catch (SecurityException e) {
                    e.printStackTrace();
                    today = new java.sql.Timestamp(utilDate.getTime());
                    LogEntry log = new LogEntry(1L, ApplicationID, "ActQueryScan - ReadThread - Run", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                    logger.Log(log);
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    today = new java.sql.Timestamp(utilDate.getTime());
                    LogEntry log = new LogEntry(1L, ApplicationID, "ActQueryScan - ReadThread - Run", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                    logger.Log(log);
                }
            }
        }
    }

    private class WebServiceTask extends AsyncTask<String, Void, Object> {
        protected ProgressDialog xDialog;
        private String originalEAN = "";


        @Override
        protected Object doInBackground(String... input) {
            int instruction = Integer.parseInt(input[0]);
            String barcode = "";
            String bincode = "";
            String msg = "";
            originalEAN = barcode.toString().trim();
            today = new java.sql.Timestamp(utilDate.getTime());
            thisMessage.setSource(deviceIMEI);
            //thisMessage.setMessageType(myMessageType);
            thisMessage.setIncomingStatus(1); //default value
            //thisMessage.setIncomingMessage(msg);
            thisMessage.setOutgoingStatus(0);   //default value
            thisMessage.setOutgoingMessage("");
            thisMessage.setInsertedTimeStamp(today);
            thisMessage.setTTL(100);    //default value
            Object retObject = null;
            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(new MyCustomNamingStrategy());
            switch (instruction) {
                case R.integer.ACTION_BARCODEQUERY:
                    barcode = input[1];
                    msg = String.format("{\"UserId\":\"%s\", \"UserCode\":\"%s\",\"Barcode\":\"%s\"}",
                            currentUser.getUserId(), currentUser.getUserCode(), barcode);
                    BarcodeResponse bcResponse = new BarcodeResponse();
                    thisMessage.setMessageType("BarcodeQuery");
                    thisMessage.setIncomingMessage(msg);
                    try {
                        String response = resolver.resolveMessageQuery(thisMessage);    //We hide the inner workings of the http being sent
                        response = responseHelper.refineProductResponse(response);
                        if (response.contains("not recognised")) {
                            //manually error trap this error
                            String iMsg = "The Response object return null due to msg queue not recognising your improper request.";
                            today = new java.sql.Timestamp(utilDate.getTime());
                            LogEntry log = new LogEntry(1L, ApplicationID, "ActQueryScan - WebServiceTask - Line:1257", deviceIMEI, RuntimeException.class.getSimpleName(), iMsg, today);
                            logger.Log(log);
                            throw new RuntimeException("The barcode you have scanned have not been recognised. Please check and scan again");
                        }else {
                            bcResponse = mapper.readValue(response, BarcodeResponse.class);
                            retObject = bcResponse;
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        today = new java.sql.Timestamp(utilDate.getTime());
                        LogEntry log = new LogEntry(1L, ApplicationID, "ActQueryScan - doInBackground", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
                        logger.Log(log);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        today = new java.sql.Timestamp(utilDate.getTime());
                        LogEntry log = new LogEntry(1L, ApplicationID, "ActQueryScan - doInBackground", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
                        logger.Log(log);
                    }
                    break;
                case R.integer.ACTION_BINQUERY:
                    bincode = input[1];
                    msg = String.format("{\"UserId\":\"%s\", \"UserCode\":\"%s\",\"BinCode\":\"%s\"}",
                            currentUser.getUserId(), currentUser.getUserCode(), bincode);
                    BinResponse msgResponse = new BinResponse();
                    thisMessage.setMessageType("BinQuery");
                    thisMessage.setIncomingMessage(msg);
                    try {
                        String response = resolver.resolveMessageQuery(thisMessage);
                        //response = responseHelper.refineOutgoingMessage(response);
                        response = responseHelper.refineResponse(response);
                        if (response.contains("not recognised")) {
                            //manually error trap this error
                            String iMsg = "The Response object return null due to msg queue not recognising your improper request.";
                            today = new java.sql.Timestamp(utilDate.getTime());
                            LogEntry log = new LogEntry(1L, ApplicationID, "ActBinMain - WebServiceTask - Line:1291", deviceIMEI, RuntimeException.class.getSimpleName(), iMsg, today);
                            logger.Log(log);
                            throw new RuntimeException("The bin you have scanned have not been recognised. Please check and scan again");
                        }else {
                            msgResponse = mapper.readValue(response, BinResponse.class);
                            retObject = msgResponse;
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        today = new java.sql.Timestamp(utilDate.getTime());
                        LogEntry log = new LogEntry(1L, ApplicationID, "ActQueryScan - doInBackground", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
                        logger.Log(log);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        today = new java.sql.Timestamp(utilDate.getTime());
                        LogEntry log = new LogEntry(1L, ApplicationID, "ActQueryScan - doInBackground", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
                        logger.Log(log);
                    }
                    break;
                case R.integer.ACTION_BARCODE_BINQUERY:
                    barcode = input[1];
                    bincode = input[2];
                    msg = String.format("{\"UserId\":\"%s\", \"UserCode\":\"%s\",\"Barcode\" : \"%s\", \"BinCode\" : \"%s\"}",
                            currentUser.getUserId(), currentUser.getUserCode(), barcode, bincode);
                    BarcodeBinResponse thisResponse = new BarcodeBinResponse();
                    thisMessage.setMessageType("BarcodeBinQuery");
                    thisMessage.setIncomingMessage(msg);
                    try {
                        String response = resolver.resolveMessageQuery(thisMessage);
                        response = responseHelper.refineResponse(response);
                        if (response.contains("not recognised")) {
                            //manually error trap this error
                            String iMsg = "The Response object return null due to msg queue not recognising your improper request.";
                            today = new java.sql.Timestamp(utilDate.getTime());
                            LogEntry log = new LogEntry(1L, ApplicationID, "ActQueryScan - WebServiceTask - Line:1325", deviceIMEI, RuntimeException.class.getSimpleName(), iMsg, today);
                            logger.Log(log);
                            throw new RuntimeException("The product and bin combination you have scanned have not been recognised. Please check and scan again");
                        }else {
                            thisResponse = mapper.readValue(response, BarcodeBinResponse.class);
                            retObject = thisResponse;
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        today = new java.sql.Timestamp(utilDate.getTime());
                        LogEntry log = new LogEntry(1L, ApplicationID, "ActQueryScan - doInBackground", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
                        logger.Log(log);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        today = new java.sql.Timestamp(utilDate.getTime());
                        LogEntry log = new LogEntry(1L, ApplicationID, "ActQueryScan - doInBackground", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
                        logger.Log(log);
                    }
                    break;
            }
            //Log.d("===============Instruction: ", input[0]);
            //Log.d("===============Barcode: ", barcode);
            //Log.d("===============Bincode: ", bincode);
            return retObject;
        }

        @Override
        protected void onPreExecute() {
            startTime = new Date().getTime(); //get start time
            xDialog = new ProgressDialog(ActQueryScan.this);
            CharSequence message = "Working hard...contacting webservice...";
            CharSequence title = "Please Wait";
            xDialog.setCancelable(true);
            xDialog.setCanceledOnTouchOutside(false);
            xDialog.setMessage(message);
            xDialog.setTitle(title);
            xDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            xDialog.show();
        }

        @Override
        protected void onPostExecute(Object responseObject) {
            if (xDialog != null && xDialog.isShowing()) xDialog.dismiss();
            //Finally end turn by locking all input controls
            lockAllControls();
            //Navigate to QueryView screen with an appropriate dataSet
            switch (NAV_INSTRUCTION) {
                case R.integer.ACTION_BARCODEQUERY:
                    BarcodeResponse bcResponse = (BarcodeResponse) responseObject;
                    Intent intent = new Intent(ActQueryScan.this, QueryView.class);
                    intent.putExtra("BARCODERESPONSE_EXTRA", bcResponse);
                    intent.putExtra("INSTRUCTION_EXTRA", NAV_INSTRUCTION);
                    startActivityForResult(intent, NAV_INSTRUCTION);
                    break;
                case R.integer.ACTION_BARCODE_BINQUERY:
                    BarcodeBinResponse bbResponse = (BarcodeBinResponse) responseObject;
                    Intent i2 = new Intent(ActQueryScan.this, QueryView.class);
                    i2.putExtra("PRODUCTBINRESPONSE_EXTRA", bbResponse);
                    i2.putExtra("INSTRUCTION_EXTRA", NAV_INSTRUCTION);
                    startActivityForResult(i2, NAV_INSTRUCTION);
                    break;
                case R.integer.ACTION_BINQUERY:
                    BinResponse bResponse = (BinResponse) responseObject;
                    Intent i = new Intent(ActQueryScan.this, QueryView.class);
                    i.putExtra("BINRESPONSE_EXTRA", bResponse);
                    i.putExtra("INSTRUCTION_EXTRA", NAV_INSTRUCTION);
                    startActivityForResult(i, NAV_INSTRUCTION);
                    break;
            }

            //Finally we restore the default nav turn
            refreshActivity();
        }

        @Override
        protected void onCancelled() {
            /*if (wsTask != null && wsTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
                wsTask.cancel(true);
            }*/
            wsTask = null;
            if (xDialog != null && xDialog.isShowing()) xDialog.dismiss();
            refreshActivity();
        }
    }
}