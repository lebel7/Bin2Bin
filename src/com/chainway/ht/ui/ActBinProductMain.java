package com.chainway.ht.ui;

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
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
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
import com.chainway.deviceapi.Barcode1D;
import com.chainway.deviceapi.exception.ConfigurationException;
import com.proper.Logger.LogHelper;
import com.proper.MessageQueue.HttpMessageResolver;
import com.proper.fragments.QuantityDialogFragment;
import com.proper.bin2bin.R;
import com.proper.data.*;
import com.proper.data.core.ICommunicator;
import com.proper.data.helpers.DialogHelper;
import com.proper.data.helpers.ResponseHelper;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by Lebel on 24/07/2014.
 */
public class ActBinProductMain extends FragmentActivity implements ICommunicator {
    private UserLoginResponse currentUser = null;   //Authentication
    public static final int KEY_SCAN = 139;
    private int inputByHand = 0;
    int readerStatus = 0;
    private boolean threadStop = true;
    private boolean isBarcodeOpened = false;
    private Barcode1D mInstance;
    private Button btnScan;
    private Button btnExit;
    private Button btnEnterSrcBin;
    private Button btnEnterBarcode;
    private Button btnEnterDstBin;
    private TextView lblSourceBin;
    private TextView lblBarcode;
    private TextView lblDestinationBin;
    private EditText txtSourceBin;
    private EditText txtBarcode;
    private EditText txtDestinationBin;
    private LinearLayout lytMain;
    private int NAV_TURN = 0;
    private int fullTurnCount = 0;
    private String deviceIMEI = "";
    private static final String myMessageType = "BarcodeQuery";
    private static final String ApplicationID = "Bin2Bin";
    private Date utilDate = Calendar.getInstance().getTime();
    private java.sql.Timestamp today = null;
    private long startTime;
    private long elapseTime;
    private String backPressedParameter = "";
    private BinResponse currentBinResponse = null;
    private ProductBinResponse currentProduct = null;
    private ProductBinSelection currentBinSelection = null;
    private LogHelper logger = new LogHelper();
    private com.proper.MessageQueue.Message thisMessage = new com.proper.MessageQueue.Message();
    private HttpMessageResolver resolver = new HttpMessageResolver();
    private ResponseHelper responseHelper = new ResponseHelper();

    private WebServiceTask wsTask;
    private Thread mReadThread;
    private Handler handler = null;
    private String scanInput;
    private static final String TAG = "ActBinProductMain";
    private SoundPool soundPool;
    private	int soundId;
    private	int errorSoundId;
    private String currentSource = "";
    private String currentBarcode = "";
    private String currentDestination = "";

    public String getScanInput() {
        return scanInput;
    }

    public void setScanInput(String scanInput) {
        this.scanInput = scanInput;
    }

    public ProductBinSelection getCurrentBinSelection() {
        return currentBinSelection;
    }

    public void setCurrentBinSelection(ProductBinSelection currentBinSelection) {
        this.currentBinSelection = currentBinSelection;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyt_binproductmain);

        TelephonyManager mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        deviceIMEI = mngr.getDeviceId();

        lytMain = (LinearLayout) this.findViewById(R.id.lytBPMMain);
        btnScan = (Button) this.findViewById(R.id.bnBPMScan);
        btnExit = (Button) this.findViewById(R.id.bnExitActBinProductMain);

        btnEnterSrcBin = (Button) this.findViewById(R.id.bnEnterSrcBinBPM);
        btnEnterBarcode = (Button) this.findViewById(R.id.bnEnterBarcodeBPM);
        btnEnterDstBin = (Button) this.findViewById(R.id.bnEnterDstBinBPM);
        lblSourceBin = (TextView) this.findViewById(R.id.txtvBPMSrcBin);
        lblBarcode = (TextView) this.findViewById(R.id.txtvBPMBarcode);
        lblDestinationBin = (TextView) this.findViewById(R.id.txtvBPMDstBin);
        txtSourceBin =  (EditText) this.findViewById(R.id.etxtBPMSrcBin);
        txtBarcode = (EditText) this.findViewById(R.id.etxtBPMBarcode);
        txtDestinationBin = (EditText) this.findViewById(R.id.etxtBPMDstBin);


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
        btnEnterSrcBin.setOnClickListener(new View.OnClickListener() {
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
        btnEnterDstBin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ButtonClicked(view);
            }
        });
        txtSourceBin.setImeOptions(EditorInfo.IME_ACTION_DONE);
        txtSourceBin.addTextChangedListener(new TextChanged());
        txtBarcode.setImeOptions(EditorInfo.IME_ACTION_DONE);
        txtBarcode.addTextChangedListener(new TextChanged());
        txtDestinationBin.setImeOptions(EditorInfo.IME_ACTION_DONE);
        txtDestinationBin.addTextChangedListener(new TextChanged());


        try {
            mInstance = Barcode1D.getInstance();
            isBarcodeOpened = mInstance.open();

        } catch (SecurityException e) {
            e.printStackTrace();
            today = new java.sql.Timestamp(utilDate.getTime());
            LogEntry log = new LogEntry(1L, ApplicationID, "ActBinProductMain - onCreate", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
            logger.Log(log);
        } catch (ConfigurationException e) {
            Log.d(TAG, "AAA");
            today = new java.sql.Timestamp(utilDate.getTime());
            LogEntry log = new LogEntry(1L, ApplicationID, "ActBinProductMain - onCreate", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
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

        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        errorSoundId = soundPool.load(this, R.raw.serror, 0);
        soundId = soundPool.load(this, R.raw.barcodebeep, 1);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg != null) {
                    if(msg.what == 1) {
                        setScanInput(msg.obj.toString());   //Set object scanned by the hardware
                        if (NAV_TURN == R.integer.TURN_DESTINATION) {
                            if (getScanInput().length() == 5) {
                                //do turn``````````````````````````````````````````````````````````````````````````````````````````````````
                                if (!txtDestinationBin.getText().toString().isEmpty()) {
                                    txtDestinationBin.setText("");     //to counter a weird bug in editText control
                                    txtDestinationBin.setText(getScanInput());
                                    //By now the nav turn state has changed
                                    //lockDestinationControls();
                                    lockAllControls();
                                } else {
                                    txtDestinationBin.setText(getScanInput());
                                    //By now the nav turn state has changed
                                    //lockDestinationControls();
                                    lockAllControls();
                                }
                            } else {
                                //Scanned wrong item, bin code etc...
                                Log.e("A bad scan has occured", "Please scan again");
                                soundPool.play(errorSoundId, 1, 1, 0, 0, 1);
                                //soundPool.play(errorSoundId, Float.valueOf("0.1"), Float.valueOf("0.1"), 0, 0, 1);
                                String mMsg = "Bad scan occured \nThis bin code is invalid";
                                AlertDialog.Builder builder = new AlertDialog.Builder(ActBinProductMain.this);
                                builder.setMessage(mMsg)
                                        .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                //do nothing
                                            }
                                        });
                                builder.show();
                                unlockDestinationControls();
                                txtDestinationBin.setText("");
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
                                    //lockBarcodeControls();
                                    lockAllControls();
                                    if (!btnEnterDstBin.isEnabled()) {
                                        btnEnterDstBin.setEnabled(true);
                                        btnEnterDstBin.setPaintFlags(btnEnterDstBin.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                                    }
                                } else {
                                    txtBarcode.setText(getScanInput());
                                    //By now the nav turn state has changed
                                    //lockBarcodeControls();
                                    lockAllControls();
                                    if (!btnEnterDstBin.isEnabled()) {
                                        btnEnterDstBin.setEnabled(true);
                                        btnEnterDstBin.setPaintFlags(btnEnterDstBin.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                                    }
                                }
                            } else {
                                //Scanned wrong item, barcode etc...
                                Log.e("A bad scan has occured", "Please scan again");
                                soundPool.play(errorSoundId, 1, 1, 0, 0, 1);
                                //soundPool.play(errorSoundId, Float.valueOf("0.1"), Float.valueOf("0.1"), 0, 0, 1);
                                String mMsg = "Bad scan occured \nThis barcode is invalid";
                                AlertDialog.Builder builder = new AlertDialog.Builder(ActBinProductMain.this);
                                builder.setMessage(mMsg)
                                        .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                //do nothing
                                            }
                                        });
                                builder.show();
                                lockAllControls();
                                unlockBarcodeControls();
                                txtBarcode.setText("");
                                //refreshActivity();
                            }
                        }
                        if (NAV_TURN == R.integer.TURN_SOURCE) {
                            if (getScanInput().length() == 5) {
                                //do barcode```````````````````````````````````````````````````````````````````````````````````````````````
                                if (!txtSourceBin.getText().toString().isEmpty()) {
                                    txtSourceBin.setText(""); //to counter a weird bug in editText control
                                    txtSourceBin.setText(getScanInput());
                                    //By now the nav turn state has changed
                                    //lockSourceControls();
                                    lockAllControls();
                                    if (!btnEnterBarcode.isEnabled()) {
                                        btnEnterBarcode.setEnabled(true);
                                        btnEnterBarcode.setPaintFlags(btnEnterBarcode.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                                    }
                                } else {
                                    txtSourceBin.setText(getScanInput());
                                    //By now the nav turn state has changed
                                    //lockSourceControls();
                                    lockAllControls();
                                    if (!btnEnterBarcode.isEnabled()) {
                                        btnEnterBarcode.setEnabled(true);
                                        btnEnterBarcode.setPaintFlags(btnEnterBarcode.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                                    }
                                }
                            } else {
                                //Scanned wrong item, barcode etc...
                                Log.e("A bad scan has occured", "Please scan again");
                                soundPool.play(errorSoundId, 1, 1, 0, 0, 1);
                                //soundPool.play(errorSoundId, Float.valueOf("0.1"), Float.valueOf("0.1"), 0, 0, 1);
                                String mMsg = "Bad scan occured \nThis source bin is invalid";
                                AlertDialog.Builder builder = new AlertDialog.Builder(ActBinProductMain.this);
                                builder.setMessage(mMsg)
                                        .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                //do nothing
                                            }
                                        });
                                builder.show();
                                txtSourceBin.setText("");
                                refreshActivity();
                            }
                        }
                        AudioManager audioMgr = (AudioManager) getSystemService(AUDIO_SERVICE);
                        float volumeLow = audioMgr.getStreamVolume(AudioManager.STREAM_SYSTEM);
                        float volumeLevel = volumeLow / audioMgr.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
                        soundPool.play(soundId, volumeLevel, volumeLevel, 0, 0, 1);
                        unlockScanControls();           //unlocks scan
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
        NAV_TURN = R.integer.TURN_SOURCE;
        PaintButtonText();
        refreshActivity();

        //Finally Authenticate User
        UserAuthenticator auth = new UserAuthenticator(ActBinProductMain.this);
        currentUser = auth.getCurrentUser();   //Gets currently authenticated user
        //showDialog(R.integer.MSG_NEUTRAL, R.integer.MSG_TYPE_NOTIFICATION, "If you can see this message then the activity has loaded successfully!", "Activity Loaded");
    }

    private void PaintButtonText() {
        if (NAV_TURN == R.integer.TURN_DESTINATION) {
            //do turn``````````````````````````````````````````````````````````````````````````````````````````````````
            btnScan.setText(R.string.action_destBin);
            btnScan.setBackgroundResource(R.drawable.button_yellow);
            if (!btnScan.isEnabled()) {
                btnScan.setEnabled(true);
                btnScan.setPaintFlags(btnScan.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }
        }
        if (NAV_TURN == R.integer.TURN_BARCODE) {
            //do barcode``````````````````````````````````````````````````````````````````````````````````````````````````
            btnScan.setText(R.string.but_startbarcode);
            btnScan.setBackgroundResource(R.drawable.button_blue);
            if (!btnScan.isEnabled()) {
                btnScan.setEnabled(true);
                btnScan.setPaintFlags(btnScan.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }
        }
        if (NAV_TURN == R.integer.TURN_SOURCE) {
            //do barcode``````````````````````````````````````````````````````````````````````````````````````````````````
            btnScan.setText(R.string.action_srcBin);
            btnScan.setBackgroundResource(R.drawable.button_green);
            if (!btnScan.isEnabled()) {
                btnScan.setEnabled(true);
                btnScan.setPaintFlags(btnScan.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }
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
        switch (NAV_TURN) {
            case R.integer.TURN_DESTINATION:
                //1st time = turn on, 2nd FINISH
                if (inputByHand == 0) {
                    turnOnInputByHand();
                    showSoftKeyboard();
                    paintByHandButtons();
                } else {
                    turnOffInputByHand();
                    setScanInput(txtDestinationBin.getText().toString());
                    if (!getScanInput().isEmpty()) {
                        fullTurnCount ++;
                        txtDestinationBin.setText(getScanInput());     // just to trigger text changed
                        paintByHandButtons();
                        lockAllControls();
                    }
                }
                break;
            case R.integer.TURN_BARCODE:
                //1st time = turn on, 2nd change NAV-Turn to Destination
                if (inputByHand == 0) {
                    turnOnInputByHand();
                    showSoftKeyboard();
                    paintByHandButtons();
                } else {
                    turnOffInputByHand();
//                    if (!btnEnterDstBin.isEnabled()) {
//                        btnEnterDstBin.setEnabled(true);
//                        btnEnterDstBin.setPaintFlags(btnEnterDstBin.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
//                    }
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
                        lockBarcodeControls();
                        unlockDestinationControls();
                        txtDestinationBin.requestFocus();
                    }
                }
                break;
            case R.integer.TURN_SOURCE:
                //1st time = turn on, 2nd change NAV-Turn
                if (inputByHand == 0) {
                    turnOnInputByHand();
                    showSoftKeyboard();
                    paintByHandButtons();
                } else {
                    turnOffInputByHand();
//                    if (!btnEnterDstBin.isEnabled()) {
//                        btnEnterDstBin.setEnabled(true);
//                        btnEnterDstBin.setPaintFlags(btnEnterDstBin.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
//                    }
                    //Disable Enter Barcode button
                    if (btnEnterSrcBin.isEnabled()) {
                        btnEnterSrcBin.setEnabled(false);
                        btnEnterSrcBin.setPaintFlags(btnEnterSrcBin.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    }
                    setScanInput(txtSourceBin.getText().toString());
                    if (!getScanInput().isEmpty()) {
                        fullTurnCount ++;
                        txtSourceBin.setText(getScanInput());     // just to trigger text changed
                        paintByHandButtons();
                        lockSourceControls();   //lockAllControls();
                        unlockBarcodeControls();
                        txtBarcode.requestFocus();
                    }
                }
                break;
        }
    }

    private void turnOnInputByHand(){
        this.inputByHand = 1;    //Turn On Input by Hand
    }

    private void turnOffInputByHand(){
        this.inputByHand = 0;    //Turn On Input by Hand
    }

    private void paintByHandButtons() {
        final String byHand = "ByHand";
        final String finish = "Finish";
        switch (NAV_TURN) {
            case R.integer.TURN_SOURCE:
                //1st time = turn on, 2nd change NAV-Turn
                if (inputByHand == 0) {
                    btnEnterSrcBin.setText(byHand);
                } else {
                    btnEnterSrcBin.setText(finish);
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
            case R.integer.TURN_DESTINATION:
                //1st time = turn on, 2nd FINISH
                if (inputByHand == 0) {
                    btnEnterDstBin.setText(byHand);
                } else {
                    btnEnterDstBin.setText(finish);
                }
        }
    }

    private void lockSourceControls() {
        if (lblSourceBin.isEnabled()) lblSourceBin.setEnabled(false);
        if (txtSourceBin.isEnabled()) txtSourceBin.setEnabled(false);
        if (btnEnterSrcBin.isEnabled()) {
            btnEnterSrcBin.setEnabled(false);
            btnEnterSrcBin.setPaintFlags(btnEnterSrcBin.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }

    private void unlockSourceControls() {
        if (!lblSourceBin.isEnabled()) lblSourceBin.setEnabled(true);
        if (!txtSourceBin.isEnabled()) txtSourceBin.setEnabled(true);
        if (!btnEnterSrcBin.isEnabled()) {
            btnEnterSrcBin.setEnabled(true);
            btnEnterSrcBin.setPaintFlags(btnEnterSrcBin.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
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

    private void unlockBarcodeControls() {
        if (!lblBarcode.isEnabled()) lblBarcode.setEnabled(true);
        if (!txtBarcode.isEnabled()) txtBarcode.setEnabled(true);
        if (!btnEnterBarcode.isEnabled()) {
            btnEnterBarcode.setEnabled(true);
            btnEnterBarcode.setPaintFlags(btnEnterBarcode.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    private void lockDestinationControls() {
        if (lblDestinationBin.isEnabled()) lblDestinationBin.setEnabled(false);
        if (txtDestinationBin.isEnabled()) txtDestinationBin.setEnabled(false);
        if (btnEnterDstBin.isEnabled()) {
            btnEnterDstBin.setEnabled(false);
            btnEnterDstBin.setPaintFlags(btnEnterDstBin.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }

    private void unlockDestinationControls() {
        if (!lblDestinationBin.isEnabled()) lblDestinationBin.setEnabled(true);
        if (!txtDestinationBin.isEnabled()) txtDestinationBin.setEnabled(true);
        if (!btnEnterDstBin.isEnabled()) {
            btnEnterDstBin.setEnabled(true);
            btnEnterDstBin.setPaintFlags(btnEnterDstBin.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    private void lockScanControl() {
        if (btnScan.isEnabled()) {
            btnScan.setEnabled(false);
            btnScan.setPaintFlags(btnScan.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }

    private void unlockScanControls() {
        if (!btnScan.isEnabled()) {
            btnScan.setEnabled(true);
            btnScan.setPaintFlags(btnScan.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    private void lockAllControls() {
        lockSourceControls();
        lockBarcodeControls();
        lockDestinationControls();
        lockScanControl();
    }

    private void refreshActivity() {
        switch (NAV_TURN) {
            case R.integer.TURN_SOURCE:
                lockAllControls();
                unlockSourceControls();
                break;
            case R.integer.TURN_BARCODE:
                lockAllControls();
                unlockBarcodeControls();
//                if (lblBarcode.getVisibility() != View.VISIBLE) lblBarcode.setVisibility(View.VISIBLE);
//                if (txtBarcode.getVisibility() != View.VISIBLE) txtBarcode.setVisibility(View.VISIBLE);
                break;
            case R.integer.TURN_DESTINATION:
                lockAllControls();
                unlockDestinationControls();
//                if (txtDestinationBin.getVisibility() != View.VISIBLE) txtDestinationBin.setVisibility(View.VISIBLE);
//                if (txtDestinationBin.getVisibility() != View.VISIBLE) txtDestinationBin.setVisibility(View.VISIBLE);
                break;
        }
        unlockScanControls();
        PaintButtonText();
    }

    private void restartActivity() {
        currentSource = "";
        currentBarcode = "";
        currentDestination = "";
        NAV_TURN = R.integer.TURN_SOURCE;
        clearFields();
        PaintButtonText();
        refreshActivity();

    }

    private void clearFields() {
        if (!txtDestinationBin.getText().toString().isEmpty()) {
            txtDestinationBin.setText("");
        }
        if (!txtBarcode.getText().toString().isEmpty()) {
            txtBarcode.setText("");
        }
        if (!txtSourceBin.getText().toString().isEmpty()) {
            txtSourceBin.setText("");
        }
    }

    private void showQuantityDialog() {
        FragmentManager fm = getSupportFragmentManager();
        QuantityDialogFragment dialog = new QuantityDialogFragment();
        dialog.show(fm, "QuantityDialog");
    }

    private void showDialog(int severity, int dialogType, String message, String title) {
        FragmentManager fm = getSupportFragmentManager();
        //DialogHelper dialog = new DialogHelper(subjectReferenceType, dialogType, message, title);
        DialogHelper dialog = new DialogHelper();
        Bundle args = new Bundle();
        args.putInt("DialogType_ARG", dialogType);
        args.putInt("Severity_ARG", severity);
        args.putString("Message_ARG", message);
        args.putString("Title_ARG", title);
        dialog.setArguments(args);
        dialog.show(fm, "Dialog");
    }

    @Override
    protected void onPause() {
        threadStop = true;
        if (mReadThread != null && mReadThread.isInterrupted() == false) {
            mReadThread.interrupt();
        }
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (isBarcodeOpened) {
            if (mReadThread != null && mReadThread.isInterrupted() == false) {
                mReadThread.interrupt();
            }
            soundPool.release();
            mInstance.close();
        }
        //android.os.Process.killProcess(android.os.Process.myPid()); Since it's not longer main entry then we're not killing app *LEBEL*
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {
            //tidyControls();  //clear all controls
            if (wsTask != null && wsTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
                wsTask.cancel(true);
            }
            restartActivity();
        }

        if (resultCode == RESULT_FIRST_USER) {
            //New Code Direct the activity to just close itself
            Intent resultIntent = new Intent();
            if (backPressedParameter != null && !backPressedParameter.equalsIgnoreCase("")) {
                setResult(1, resultIntent);
            } else {
                setResult(RESULT_OK, resultIntent);
            }
            com.chainway.ht.ui.ActBinProductMain.this.finish();
        }

        if (requestCode == RESULT_OK && resultCode == RESULT_OK) {
            //tidyControls();  //clear all controls
            if (wsTask != null && wsTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
                wsTask.cancel(true);
            }
            //Just do nothing and return to sequence as normal
        }
    }

    private void ButtonClicked(View view) {
        boolean bContinuous = true;
        int iBetween = 0;
        switch (view.getId()) {
            case R.id.bnBPMScan:
                if (NAV_TURN == R.integer.TURN_DESTINATION) {
                    //do destination bin ``````````````````````````````````````````````````````````````````````````````````````````````````
                    lockAllControls();
                    unlockDestinationControls();
                    unlockScanControls();
                    if (!txtDestinationBin.isEnabled()) txtDestinationBin.setEnabled(true);
                    txtDestinationBin.requestFocus();
                    if (threadStop) {
                        Log.i("Reading", "[Destination Bin Scan] BinProductMain " + readerStatus);
                        //init_barcode = et_init_barcode.getText().toString();
                        mReadThread = new Thread(new GetBarcode(bContinuous, iBetween));
                        mReadThread.setName("[Destination Bin Scan] Query ReadThread");
                        mReadThread.start();
                    }else {
                        threadStop = true;
                    }
                    fullTurnCount ++;
                }
                if (NAV_TURN == R.integer.TURN_BARCODE) {
                    //do barcode ``````````````````````````````````````````````````````````````````````````````````````````````````
                    //fullTurnCount = 0;      //set to default if it's not so already
                    lockAllControls();
                    unlockBarcodeControls();
                    unlockScanControls();
                    if (!txtBarcode.isEnabled()) txtBarcode.setEnabled(true);
                    txtBarcode.requestFocus();
                    if (threadStop) {
                        Log.i("Reading", "[Barcode Scan] BinProductMain " + readerStatus);
                        //init_barcode = et_init_barcode.getText().toString();
                        mReadThread = new Thread(new GetBarcode(bContinuous, iBetween));
                        mReadThread.setName("[Barcode Scan] Query ReadThread");
                        mReadThread.start();
                    }else {
                        threadStop = true;
                    }
                    fullTurnCount ++;
                }
                if (NAV_TURN == R.integer.TURN_SOURCE) {
                    //do source bin ``````````````````````````````````````````````````````````````````````````````````````````````````
                    lockAllControls();
                    unlockSourceControls();
                    unlockScanControls();
                    fullTurnCount = 0;      //set to default if it's not so already
                    if (!txtSourceBin.isEnabled()) txtSourceBin.setEnabled(true);
                    txtSourceBin.requestFocus();
                    if (threadStop) {
                        Log.i("Reading", "[Source Bin Scan] BinProductMain " + readerStatus);
                        //init_barcode = et_init_barcode.getText().toString();
                        mReadThread = new Thread(new GetBarcode(bContinuous, iBetween));
                        mReadThread.setName("[Source Bin Scan] Query ReadThread");
                        mReadThread.start();
                    }else {
                        threadStop = true;
                    }
                    fullTurnCount ++;
                }
                break;
            case R.id.bnExitActBinProductMain:
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
            default:
                manageInputByHand();
                break;
        }
    }

    @Override
    public void onDialogMessage(int buttonClicked) {
        switch (buttonClicked) {
            case R.integer.MSG_CANCEL:
                break;
            case R.integer.MSG_YES:
                break;
            case R.integer.MSG_OK:
                break;
            case R.integer.MSG_NO:
                break;
        }
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
                    if (inputByHand == 0) {

                        if (NAV_TURN == R.integer.TURN_DESTINATION) {
                            //do bin``````````````````````````````````````````````````````````````````````````````````````````````````
                            //lockBarcodeControls();
                            if (eanCode.length() > 0 && (eanCode.length() == 5)) {
                                if (eanCode.equalsIgnoreCase(currentSource)) {
                                    soundPool.play(errorSoundId, 1, 1, 0, 0, 1);
                                    Vibrator vib = (Vibrator) ActBinProductMain.this.getSystemService(Context.VIBRATOR_SERVICE);
                                    // Vibrate for 500 milliseconds
                                    vib.vibrate(2000);
                                    String mMsg = "Incorrect Combination \nSource & Destination Bin must NOT be the same";
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ActBinProductMain.this);
                                    builder.setMessage(mMsg)
                                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    //do nothing
                                                }
                                            });
                                    builder.show();
                                    txtDestinationBin.setText("");
                                    if (txtDestinationBin.isEnabled()) txtDestinationBin.setEnabled(false); //disable the control
                                } else {
//                                    lockBarcodeControls();
//                                    lockDestinationControls();
                                    lockAllControls();
                                    unlockScanControls();
                                    currentDestination = ""; //set to default
                                    //Authenticate current user, Build Message only when all these conditions are right then proceed with asyncTask
                                    UserAuthenticator auth = new UserAuthenticator(ActBinProductMain.this);
                                    currentUser = currentUser != null ? currentUser : auth.getCurrentUser();   //Gets currently authenticated user
                                    if (currentUser != null  && currentProduct != null && currentBinSelection != null) {

                                        currentDestination = eanCode;

                                        MoveRequest req = new MoveRequest();
                                        List<MoveRequestItem> list = new ArrayList<MoveRequestItem>();
                                        req.setUserCode(currentUser.getUserCode());
                                        req.setUserId(String.format("%s", currentUser.getUserId()));
                                        req.setSrcBin(currentSource);
                                        req.setDstBin(currentDestination);
                                        MoveRequestItem item = new MoveRequestItem();
                                        item.setProductID(getCurrentBinSelection().getProductId());
                                        item.setSuppliercat(getCurrentBinSelection().getSupplierCat());
                                        item.setQty(getCurrentBinSelection().getQtyToMove());
                                        list.add(item);

                                        //Build message request
                                        req.setProducts(list);
                                        ObjectMapper mapper = new ObjectMapper();
                                        String msg = null;
                                        try {
                                            msg = mapper.writeValueAsString(req);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            today = new java.sql.Timestamp(utilDate.getTime());
                                            LogEntry log = new LogEntry(1L, ApplicationID, "ActBinProductMain - afterTextChanged", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                                            logger.Log(log);
                                        }
                                        today = new java.sql.Timestamp(utilDate.getTime());
                                        com.proper.MessageQueue.Message thisMessage = new com.proper.MessageQueue.Message();

                                        thisMessage.setSource(deviceIMEI);
                                        thisMessage.setMessageType("CreateMovelist");
                                        thisMessage.setIncomingStatus(1); //default value
                                        thisMessage.setIncomingMessage(msg);
                                        thisMessage.setOutgoingStatus(0);   //default value
                                        thisMessage.setOutgoingMessage("");
                                        thisMessage.setInsertedTimeStamp(today);
                                        thisMessage.setTTL(100);    //default value
                                        AbstractMap.SimpleEntry<Integer, AbstractMap.SimpleEntry<String, com.proper.MessageQueue.Message>> entry = new
                                                AbstractMap.SimpleEntry<Integer, AbstractMap.SimpleEntry<String, com.proper.MessageQueue.Message>>
                                                (R.integer.TURN_DESTINATION, new AbstractMap.SimpleEntry<String, com.proper.MessageQueue.Message>(currentDestination, thisMessage));
                                        wsTask = new WebServiceTask(R.integer.TURN_DESTINATION);
                                        wsTask.execute(entry);

                                        //Finally end turn by locking all input controls
                                        //lockAllControls();
                                    } else {
                                        soundPool.play(errorSoundId, 1, 1, 0, 0, 1);
                                        Vibrator vib = (Vibrator) ActBinProductMain.this.getSystemService(Context.VIBRATOR_SERVICE);
                                        // Vibrate for 500 milliseconds
                                        vib.vibrate(2000);
                                        String mMsg = "User not Authenticated \nPlease login";
                                        AlertDialog.Builder builder = new AlertDialog.Builder(ActBinProductMain.this);
                                        builder.setMessage(mMsg)
                                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        //do nothing
                                                    }
                                                });
                                        builder.show();
                                    }
                                }
                            } else {
                                //Check to see if we're making entry by hand, display error and continue nav order
                                if (inputByHand == 0) {
                                    new AlertDialog.Builder(ActBinProductMain.this).setTitle(R.string.DIA_ALERT).setMessage(R.string.DEV_BIN_ERR).setPositiveButton(R.string.DIA_CHECK, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            //refreshActivity();
                                        }
                                    }).show();
                                } else {
                                    //Warn then Refresh Activity
                                    new AlertDialog.Builder(ActBinProductMain.this).setTitle(R.string.DIA_ALERT).setMessage(R.string.DEV_BIN_ERR).setPositiveButton(R.string.DIA_CHECK, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            refreshActivity();
                                        }
                                    }).show();
                                }
                            }
                            //---
                        }
                        if (NAV_TURN == R.integer.TURN_BARCODE) {
                            //do barcode ``````````````````````````````````````````````````````````````````````````````````````````````````
                            //lockDestinationControls();
                            int acceptable[] = {12,13,14};
                            if (eanCode.length() > 0 && !(Arrays.binarySearch(acceptable, eanCode.length()) == -1)) {

                                //Authenticate current user, Build Message only when all these conditions are right then proceed with asyncTask
                                UserAuthenticator auth = new UserAuthenticator(ActBinProductMain.this);
                                currentUser = currentUser != null ? currentUser : auth.getCurrentUser();   //Gets currently authenticated user
                                if (currentUser != null) {
                                    //Check if product exist in Bin
                                    if (currentBinResponse != null) {
                                        if (currentBinResponse.getMatchedProducts() > 0) {
                                            int totalProd = currentBinResponse.getMatchedProducts();
                                            boolean found = false;
                                            for (ProductBinResponse prod : currentBinResponse.getProducts()) {
                                                if (prod.getBarcode().equalsIgnoreCase(eanCode)) {
                                                    found = true;
                                                    currentProduct = prod;  // <<< set Current Product >>>
                                                    currentBinSelection = new ProductBinSelection(prod);  //<<< set Current Selection >>>
                                                    currentBarcode = eanCode;

                                                    //Finally switch turn (destination)
                                                    NAV_TURN = R.integer.TURN_DESTINATION;
                                                    if (!btnEnterDstBin.isEnabled()) {
                                                        btnEnterDstBin.setEnabled(true);
                                                        btnEnterDstBin.setPaintFlags(btnEnterDstBin.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                                                    }
                                                    PaintButtonText();
                                                    // TODO - Navigate to request quantity
                                                    showQuantityDialog();
                                                }
                                                totalProd --;
                                                if (found == false && totalProd == 0) {
                                                    //Report no products in Bin
                                                    soundPool.play(errorSoundId, 1, 1, 0, 0, 1);
                                                    Vibrator vib = (Vibrator) ActBinProductMain.this.getSystemService(Context.VIBRATOR_SERVICE);
                                                    // Vibrate for 500 milliseconds
                                                    vib.vibrate(2000);
                                                    String mMsg = "There is no such product in this Bin \nPlease re-scan";
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(ActBinProductMain.this);
                                                    builder.setMessage(mMsg)
                                                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                                                public void onClick(DialogInterface dialog, int id) {
                                                                    //do nothing
                                                                }
                                                            });
                                                    builder.show();
                                                }
                                            }
                                        }else {
                                            //Report no products in Bin
                                            soundPool.play(errorSoundId, 1, 1, 0, 0, 1);
                                            Vibrator vib = (Vibrator) ActBinProductMain.this.getSystemService(Context.VIBRATOR_SERVICE);
                                            // Vibrate for 500 milliseconds
                                            vib.vibrate(2000);
                                            String mMsg = "There is no such product in this Bin \nPlease re-scan";
                                            AlertDialog.Builder builder = new AlertDialog.Builder(ActBinProductMain.this);
                                            builder.setMessage(mMsg)
                                                    .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            //do nothing
                                                        }
                                                    });
                                            builder.show();
                                            unlockBarcodeControls();    //New
                                            txtBarcode.setText("");     //New
                                        }

                                    }else {
                                        //Report no products in Bin
                                        soundPool.play(errorSoundId, 1, 1, 0, 0, 1);
                                        Vibrator vib = (Vibrator) ActBinProductMain.this.getSystemService(Context.VIBRATOR_SERVICE);
                                        // Vibrate for 500 milliseconds
                                        vib.vibrate(2000);
                                        String mMsg = "There is no such product in Bin specified \nPlease re-scan";
                                        AlertDialog.Builder builder = new AlertDialog.Builder(ActBinProductMain.this);
                                        builder.setMessage(mMsg)
                                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        //do nothing
                                                    }
                                                });
                                        builder.show();
                                        unlockBarcodeControls();    //New
                                        txtBarcode.setText("");     //New
                                    }
                                } else {
                                    soundPool.play(errorSoundId, 1, 1, 0, 0, 1);
                                    Vibrator vib = (Vibrator) ActBinProductMain.this.getSystemService(Context.VIBRATOR_SERVICE);
                                    // Vibrate for 500 milliseconds
                                    vib.vibrate(2000);
                                    String mMsg = "User not Authenticated \nPlease login";
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ActBinProductMain.this);
                                    builder.setMessage(mMsg)
                                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    //do nothing
                                                }
                                            });
                                    builder.show();
                                }
                            } else {
                                //Check to see if we're making entry by hand, display error and continue nav order
                                if (inputByHand == 0) {
                                    new AlertDialog.Builder(ActBinProductMain.this).setTitle(R.string.DIA_ALERT).setMessage(R.string.DEV_EAN_ERR).setPositiveButton(R.string.DIA_CHECK, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            refreshActivity();
                                        }
                                    }).show();
                                }
                            }
                        }
                        if (NAV_TURN == R.integer.TURN_SOURCE) {
                            //do source bin ``````````````````````````````````````````````````````````````````````````````````````````````````
                            //lockDestinationControls();        --removed old code
                            if ((eanCode.length() > 0 && (eanCode.length() == 5))) {

                                //Authenticate current user, Build Message only when all these conditions are right then proceed with asyncTask
                                UserAuthenticator auth = new UserAuthenticator(ActBinProductMain.this);
                                currentUser = currentUser != null ? currentUser : auth.getCurrentUser();   //Gets currently authenticated user
                                if (currentUser != null) {
                                    currentSource = eanCode;

                                    //TODO - Query Bin :

                                    today = new java.sql.Timestamp(utilDate.getTime());
                                    String msg = String.format("{\"UserId\":\"%s\", \"UserCode\":\"%s\",\"BinCode\":\"%s\"}",
                                            currentUser.getUserId(), currentUser.getUserCode(), currentSource);
                                    thisMessage.setSource(deviceIMEI);
                                    thisMessage.setIncomingStatus(1); //default value
                                    thisMessage.setOutgoingStatus(0);   //default value
                                    thisMessage.setOutgoingMessage("");
                                    thisMessage.setInsertedTimeStamp(today);
                                    thisMessage.setTTL(100);    //default value
                                    thisMessage.setMessageType("BinQuery");
                                    thisMessage.setIncomingMessage(msg);
                                    AbstractMap.SimpleEntry<Integer, AbstractMap.SimpleEntry<String, com.proper.MessageQueue.Message>> entry = new
                                            AbstractMap.SimpleEntry<Integer, AbstractMap.SimpleEntry<String, com.proper.MessageQueue.Message>>
                                            (R.integer.TURN_SOURCE, new AbstractMap.SimpleEntry<String, com.proper.MessageQueue.Message>(currentSource, thisMessage));
                                    wsTask = new WebServiceTask(R.integer.TURN_SOURCE);
                                    wsTask.execute(entry);

                                    //Finally switch turn (barcode)
                                    NAV_TURN = R.integer.TURN_BARCODE;
                                    unlockScanControls();
                                    PaintButtonText();
                                } else {
                                    soundPool.play(errorSoundId, 1, 1, 0, 0, 1);
                                    Vibrator vib = (Vibrator) ActBinProductMain.this.getSystemService(Context.VIBRATOR_SERVICE);
                                    // Vibrate for 500 milliseconds
                                    vib.vibrate(2000);
                                    String mMsg = "User not Authenticated \nPlease login";
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ActBinProductMain.this);
                                    builder.setMessage(mMsg)
                                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    //do nothing
                                                }
                                            });
                                    builder.show();
                                }
                            } else {
                                //Check to see if we're making entry by hand, display error and continue nav order
                                if (inputByHand == 0) {
                                    new AlertDialog.Builder(ActBinProductMain.this).setTitle(R.string.DIA_ALERT).setMessage(R.string.DEV_EAN_ERR).setPositiveButton(R.string.DIA_CHECK, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            refreshActivity();
                                        }
                                    }).show();
                                }
                            }
                        }
                        //End full turn
                        fullTurnCount = 0;
                    }

                }
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KEY_SCAN) {
            if (event.getRepeatCount() == 0) {
                boolean bContinuous = true;
                int iBetween = 0;
                if (NAV_TURN == R.integer.TURN_DESTINATION) {
                    //do destination bin ``````````````````````````````````````````````````````````````````````````````````````````````````
                    lockAllControls();
                    unlockDestinationControls();
                    unlockScanControls();
                    if (!txtDestinationBin.isEnabled()) txtDestinationBin.setEnabled(true);
                    txtDestinationBin.requestFocus();
                    if (threadStop) {
                        Log.i("Reading", "[Destination Bin Scan] BinProductMain " + readerStatus);
                        //init_barcode = et_init_barcode.getText().toString();
                        mReadThread = new Thread(new GetBarcode(bContinuous, iBetween));
                        mReadThread.setName("[Destination Bin Scan] Query ReadThread");
                        mReadThread.start();
                    }else {
                        threadStop = true;
                    }
                    fullTurnCount ++;
                }
                if (NAV_TURN == R.integer.TURN_BARCODE) {
                    //do barcode ``````````````````````````````````````````````````````````````````````````````````````````````````
                    //fullTurnCount = 0;      //set to default if it's not so already
                    lockAllControls();
                    unlockBarcodeControls();
                    unlockScanControls();
                    if (!txtBarcode.isEnabled()) txtBarcode.setEnabled(true);
                    txtBarcode.requestFocus();
                    if (threadStop) {
                        Log.i("Reading", "[Barcode Scan] BinProductMain " + readerStatus);
                        //init_barcode = et_init_barcode.getText().toString();
                        mReadThread = new Thread(new GetBarcode(bContinuous, iBetween));
                        mReadThread.setName("[Barcode Scan] Query ReadThread");
                        mReadThread.start();
                    }else {
                        threadStop = true;
                    }
                    fullTurnCount ++;
                }
                if (NAV_TURN == R.integer.TURN_SOURCE) {
                    //do source bin ``````````````````````````````````````````````````````````````````````````````````````````````````
                    lockAllControls();
                    unlockSourceControls();
                    unlockScanControls();
                    fullTurnCount = 0;      //set to default if it's not so already
                    if (!txtDestinationBin.isEnabled()) txtDestinationBin.setEnabled(true);
                    txtDestinationBin.requestFocus();
                    if (threadStop) {
                        Log.i("Reading", "[Source Bin Scan] BinProductMain " + readerStatus);
                        //init_barcode = et_init_barcode.getText().toString();
                        mReadThread = new Thread(new GetBarcode(bContinuous, iBetween));
                        mReadThread.setName("[Source Bin Scan] Query ReadThread");
                        mReadThread.start();
                    }else {
                        threadStop = true;
                    }
                    fullTurnCount ++;
                }
            }
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
        ActBinProductMain.this.finish();
    }

    private class GetBarcode implements Runnable {

        private boolean isContinuous = false;
        String barCode = "";
        private long sleepTime = 1000;
        Message msg = null;

        public GetBarcode(boolean isContinuous) {
            this.isContinuous = isContinuous;
        }

        public GetBarcode(boolean isContinuous, int sleep) {
            this.isContinuous = isContinuous;
            this.sleepTime = sleep;
        }

        @Override
        public void run() {

            do {
                barCode = mInstance.scan();

                Log.i("MY", "barCode " + barCode.trim());

                msg = new Message();

                if (barCode == null || barCode.isEmpty()) {
                    msg.what = 0;
                    msg.obj = "";
                } else {
                    msg.what = 1;
                    msg.obj = barCode;
                }

                handler.sendMessage(msg);

                if (isContinuous) {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            } while (isContinuous && !threadStop);

        }

    }

    private class WebServiceTask extends AsyncTask<AbstractMap.SimpleEntry<Integer, AbstractMap.SimpleEntry<String, com.proper.MessageQueue.Message>>, Void, AbstractMap.SimpleEntry<Integer, Object>> {
        protected ProgressDialog xDialog;
        protected int instruction;

        private WebServiceTask(int instruction) {
            this.instruction = instruction;
        }

        @Override
        protected AbstractMap.SimpleEntry<Integer, Object> doInBackground(AbstractMap.SimpleEntry<Integer, AbstractMap.SimpleEntry<String, com.proper.MessageQueue.Message>>... input) {
            int instruction = input[0].getKey();
            AbstractMap.SimpleEntry<Integer, Object> retObject = null;

            if (instruction == R.integer.TURN_DESTINATION) {
                PartialBinMoveResponse qryResponse = new PartialBinMoveResponse();

                HttpMessageResolver resolver = new HttpMessageResolver();
                String response = resolver.resolveMessageQuery(input[0].getValue().getValue());
                if (response != null && !response.equalsIgnoreCase("")) {
                    if (response.contains("not recognised")) {
                        //manually error trap this error
                        String iMsg = "The Response object return null due to msg queue not recognising your improper request.";
                        today = new java.sql.Timestamp(utilDate.getTime());
                        LogEntry log = new LogEntry(1L, ApplicationID, "ActBinProductMain - WebServiceTask - Line:1298", deviceIMEI, RuntimeException.class.getSimpleName(), iMsg, today);
                        logger.Log(log);
                        throw new RuntimeException("Warehouse Support webservice is currently down. Please contact the IT department");
                    }else {
                        //Manually process this response
                        try {
                            JSONObject resp = new JSONObject(response);
                            JSONArray messages = resp.getJSONArray("Messages");
                            JSONArray actions = resp.getJSONArray("MessageObjects");
                            String RequestedSrcBin = resp.getString("RequestedSrcBin");
                            String RequestedDstBin = resp.getString("RequestedDstBin");
                            //String Result = resp.getString("Result");
                            List<BinMoveMessage> messageList = new ArrayList<BinMoveMessage>();
                            List<BinMoveObject> actionList = new ArrayList<BinMoveObject>();
                            //get messages
                            for (int i = 0; i < messages.length(); i++) {
                                JSONObject message = messages.getJSONObject(i);
                                String name = message.getString("MessageName");
                                String text = message.getString("MessageText");
                                Timestamp time = Timestamp.valueOf(message.getString("MessageTimeStamp"));

                                messageList.add(new BinMoveMessage(name, text, time));
                            }
                            //get actions
                            for (int i = 0; i < actions.length(); i++) {
                                JSONObject action = actions.getJSONObject(i);
                                String act = action.getString("Action");
                                int prodId = Integer.parseInt(action.getString("ProductId"));
                                String cat = action.getString("SupplierCat");
                                String ean = action.getString("EAN");
                                int qty = Integer.parseInt(action.getString("Qty"));
                                actionList.add(new BinMoveObject(act, prodId, cat, ean, qty));
                            }
                            qryResponse.setRequestedSrcBin(RequestedSrcBin);
                            qryResponse.setRequestedDstBin(RequestedDstBin);
                            //qryResponse.setResult(Result);
                            qryResponse.setMessages(messageList);
                            qryResponse.setMessageObjects(actionList);
                            retObject = new AbstractMap.SimpleEntry<Integer, Object>(instruction, qryResponse);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            today = new java.sql.Timestamp(utilDate.getTime());
                            LogEntry log = new LogEntry(1L, ApplicationID, this.getClass().getSimpleName() + " - WebServiceTask - doInBackground", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
                            logger.Log(log);
                        }
                    }
                }
            }


            //  Do Source HTTP Method
            if (instruction == R.integer.TURN_SOURCE) {
                try {
                    String response = resolver.resolveMessageQuery(input[0].getValue().getValue());
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
                        ObjectMapper mapper = new ObjectMapper();
                        BinResponse msgResponse = mapper.readValue(response, BinResponse.class);
                        retObject = new AbstractMap.SimpleEntry<Integer, Object>(instruction, msgResponse);
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
            }

            return retObject;
        }

        @Override
        protected void onPreExecute() {
            startTime = new Date().getTime(); //get start time
            xDialog = new ProgressDialog(ActBinProductMain.this);
            CharSequence message = "Working hard...contacting webservice...";
            if (instruction == R.integer.TURN_DESTINATION) {
                message = "Working hard...Moving Product...";
            }
            if (instruction == R.integer.TURN_SOURCE) {
                message = "Working hard...Searching Bin...";
            }

            CharSequence title = "Please Wait";
            xDialog.setCancelable(true);
            xDialog.setCanceledOnTouchOutside(false);
            xDialog.setMessage(message);
            xDialog.setTitle(title);
            xDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            xDialog.show();
        }

        @Override
        protected void onPostExecute(AbstractMap.SimpleEntry<Integer, Object> responseObject) {
            if (xDialog != null && xDialog.isShowing()) xDialog.dismiss();
            //Finally end turn by locking all input controls
            //lockAllControls();

            int instruction = responseObject.getKey();

            switch (instruction) {
                case R.integer.TURN_DESTINATION:
                    PartialBinMoveResponse response = (PartialBinMoveResponse) responseObject.getValue();
                    String pos = "Success: BinMove completed!";
                    String neg = "Failure: BinMove has failed!";
                    if (response != null) {
                        //ShowDialog:
                        showDialog(R.integer.MSG_TYPE_NOTIFICATION, R.integer.MSG_POSITIVE, pos, "Move Result");
                    } else {
                        //Response is null the disable Yes button:
                        showDialog(R.integer.MSG_TYPE_NOTIFICATION, R.integer.MSG_FAILURE, neg, "Move Result");
                    }
                    refreshActivity();
                    break;
                case R.integer.TURN_SOURCE:
                    currentBinResponse = (BinResponse) responseObject.getValue();
                    break;
                default:
                    //yell foul and exit !
                    String error = "Success: BinMove completed!";
                    showDialog(R.integer.MSG_TYPE_NOTIFICATION, R.integer.MSG_FAILURE, error, "Move Result");
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    ActBinProductMain.this.finish();
            }
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