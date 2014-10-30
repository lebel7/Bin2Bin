package com.proper.bin2bin;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.proper.Logger.LogHelper;
import com.proper.MessageQueue.HttpMessageResolver;
import com.proper.MessageQueue.Message;
import com.proper.data.Contact;
import com.proper.data.LogEntry;
import com.proper.data.UserAuthenticator;
import com.proper.data.UserLoginResponse;
import org.codehaus.jackson.map.ObjectMapper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by Lebel on 09/04/2014.
 */
public class ActChooser extends Activity {
    private SharedPreferences prefs = null;
    private String deviceID = "";
    private String deviceIMEI = "";
    private static final String ApplicationID = "Bin2Bin";
    private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private java.util.Date utilDate = java.util.Calendar.getInstance().getTime();
    private java.sql.Timestamp today = null;
    private Button btnSingleMove;
    private Button btnBinMove;
    private Button btnExit;
    private Button btnQueries;
    //private ViewFlipper flipper;
    //private Spinner cmbUsers;
    //private EditText txtInitials;
    //private Button btnLogIn;
    //private Button btnLogOff;
    //private List<Contact> contactList;
    private SoundPool soundPool;
    //private	int soundId;
    private int errorSoundId;
    //private UserLoginTask loginTask;
    private UserLoginResponse currentUser;
    private String currentUserToken = "";
    //private String initials = "";
    //private int loginAttempt = 0;
    //private ArrayAdapter<Contact> adapter;
    private UserAuthenticator authenticator = null;
    private LogHelper logger = new LogHelper();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyt_chooser);

        authenticator = new UserAuthenticator(this);
        TelephonyManager mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        deviceIMEI = mngr.getDeviceId();
        deviceID = Build.MANUFACTURER;
        currentUser = authenticator.getCurrentUser();

        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        //soundId = soundPool.load("/system/media/audio/ui/VideoRecord.ogg", 0);
        errorSoundId = soundPool.load(getString(R.string.SOUND_ERROR), 0);

        //setupControls();

        //flipper = (ViewFlipper) this.findViewById(R.id.loginFlipper);
        //btnLogIn = (Button) this.findViewById(R.id.bnLogIn);
        //btnLogOff = (Button) this.findViewById(R.id.bnLogOut);
        //cmbUsers = (Spinner) this.findViewById(R.id.spnUsers);
        //TextView lblInitials = (TextView) this.findViewById(R.id.lblInitials);
        //txtInitials = (EditText) this.findViewById(R.id.etxtInitials);
        btnSingleMove = (Button) this.findViewById(R.id.bnSingleMove);
        btnBinMove = (Button) this.findViewById(R.id.bnBinMove);
        btnQueries = (Button) this.findViewById(R.id.bnQueries);
        btnExit = (Button) this.findViewById(R.id.bnExitActChooser);

        //txtInitials.addTextChangedListener(new TextChanged());
//        btnLogIn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                OnButtonClicked(view);
//            }
//        });
//        btnLogOff.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                OnButtonClicked(view);
//            }
//        });
        btnSingleMove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnButtonClicked(view);
            }
        });
        btnBinMove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnButtonClicked(view);
            }
        });
        btnQueries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnButtonClicked(view);
            }
        });
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnButtonClicked(view);
            }
        });
    }

//    private void saveAuthentication() {
//        // Save to the sharedPreference
//        prefs = getSharedPreferences(getString(R.string.preference_credentials), Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.putString("ApplicationID", ApplicationID);
//        editor.putString("IMEI", deviceIMEI);
//        editor.putString("Device", deviceID);
//        editor.putString("UserToken", currentUserToken);
//        editor.commit();
//    }
//
//    private void removeAuthentication() {
//        // Save to the sharedPreference
//        prefs = getSharedPreferences(getString(R.string.preference_credentials), Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.remove("UserToken");
//        editor.remove("IMEI");
//        editor.remove("Device");
//        editor.commit();
//    }

//    private void logOn(String initials) {
//
//        if (currentUserToken.isEmpty() && !initials.isEmpty()) {
//            if (flipper.getDisplayedChild() == 0) {
//                hideSoftKeyboard(ActChooser.this);  //Hide the default software Keyboard
//                loginTask = new UserLoginTask();
//                loginTask.execute(initials);
//            }
//        }else {
//            ActChooser.this.setTitle(getResources().getString(R.string.currentUser));
//            soundPool.play(errorSoundId, 1, 1, 0, 0, 1);
//            Vibrator vib = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
//            vib.vibrate(2000);  // Vibrate for 500 milliseconds
//            String mMsg = "Login Error - Wrong initials \nPlease try again";
//            AlertDialog.Builder builder = new AlertDialog.Builder(ActChooser.this);
//            builder.setMessage(mMsg)
//                    .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                            //do nothing
//                        }
//                    });
//            builder.show();
//        }
//    }

//    private void logOut() {
//        if (currentUser != null) {
//            currentUser = null;
//            currentUserToken = "";
//            txtInitials.setText("");
//            initials = "";
//            removeAuthentication();
//        }
//    }

//    public static void hideSoftKeyboard(Activity activity) {
//        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
//        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
//    }

    private void OnButtonClicked(View v) {
        //do
        switch(v.getId()) {
            case R.id.bnBinMove:
                if (currentUser != null) {
                    Intent frmMoveChooser = new Intent(com.proper.bin2bin.ActChooser.this, com.proper.bin2bin.ActMoveChooser.class);
                    frmMoveChooser.putExtra("INSTRUCTION", 1);
                    startActivityForResult(frmMoveChooser, 1);
                } else {
                    new SoundPool(1, AudioManager.STREAM_MUSIC, 0).play(errorSoundId, 1, 1, 0, 0, 1);
                    Vibrator vib = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
                    vib.vibrate(2000);  // Vibrate for 500 milliseconds
                    String mMsg = "User not Authenticated \nPlease login";
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActChooser.this);
                    builder.setMessage(mMsg)
                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //do nothing
                                }
                            });
                    builder.show();
                }
                break;
            case R.id.bnSingleMove:
                if (currentUser != null) {
                    if (deviceID.equalsIgnoreCase(getString(R.string.SmallDevice))) {
                        //Intent frmSingle = new Intent(com.proper.bin2bin.ActChooser.this, com.android.barcode.ActSingleMain.class);
                        Intent frmSingle = new Intent(com.proper.bin2bin.ActChooser.this, com.android.barcode.ActSingleMove.class);
                        frmSingle.putExtra("INSTRUCTION", 0);
                        startActivityForResult(frmSingle, 0);
                    }
                    if (deviceID.equalsIgnoreCase(getString(R.string.LargeDevice))) {
                        //Intent frmSingle = new Intent(com.proper.bin2bin.ActChooser.this, com.chainway.ht.ui.ActSingleMain.class);
                        Intent frmSingle = new Intent(com.proper.bin2bin.ActChooser.this, com.chainway.ht.ui.ActBinProductMain.class);
                        frmSingle.putExtra("INSTRUCTION", 0);
                        startActivityForResult(frmSingle, 0);
                    }

                } else {
                    soundPool.play(errorSoundId, 1, 1, 0, 0, 1);
                    Vibrator vib = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
                    vib.vibrate(2000);  // Vibrate for 500 milliseconds
                    String mMsg = "User not Authenticated \nPlease login";
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActChooser.this);
                    builder.setMessage(mMsg)
                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //do nothing
                                }
                            });
                    builder.show();
                }
                break;
            case R.id.bnExitActChooser:
                finish();
                break;
//            case R.id.bnLogIn:
//                logOn(initials);
//                break;
//            case R.id.bnLogOut:
//                if (flipper.getDisplayedChild() == 1) {
//                    //do Log out
//                    ActChooser.this.setTitle(getResources().getString(R.string.currentUser));
//                    logOut();
//                    flipper.setDisplayedChild(0);
//                }
//                break;
            case R.id.bnQueries:
                if (currentUser != null) {
                    Intent frmQueryChooser = new Intent(com.proper.bin2bin.ActChooser.this, com.proper.bin2bin.ActQueryChooser.class);
                    frmQueryChooser.putExtra("INSTRUCTION", 0);
                    startActivityForResult(frmQueryChooser, 0);
                } else {
                    soundPool.play(errorSoundId, 1, 1, 0, 0, 1);
                    Vibrator vib = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
                    // Vibrate for 500 milliseconds
                    vib.vibrate(2000);
                    String mMsg = "User not Authenticated \nPlease login";
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActChooser.this);
                    builder.setMessage(mMsg)
                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //do nothing
                                }
                            });
                    builder.show();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //do nothing...
        switch (resultCode) {
            case RESULT_OK:
                //do
                break;
        }
    }

    @Override
    protected void onDestroy() {
        try {
            //logOut();
            authenticator.logOffUser();
        } catch (Exception e) {
            e.printStackTrace();
            today = new java.sql.Timestamp(utilDate.getTime());
            LogEntry log = new LogEntry(1L, ApplicationID, "ActChooser - Attempting Logout - onDestroy", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
            logger.Log(log);
        }
        super.onDestroy();
        android.os.Process.killProcess(android.os.Process.myPid()); //kill it!
    }

//    class TextChanged implements TextWatcher {
//        @Override
//        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
//        }
//
//        @Override
//        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
//        }
//
//        @Override
//        public void afterTextChanged(Editable editable) {
//            initials = editable.toString().toUpperCase(Locale.getDefault());
//        }
//    }

//    private class UserLoginTask extends AsyncTask<String, Void, UserLoginResponse> {
//        private ProgressDialog lDialog;
//
//        @Override
//        protected void onPreExecute() {
//            lDialog = new ProgressDialog(ActChooser.this);
//            CharSequence message = "Working hard...checking credentials...";
//            CharSequence title = "Please Wait";
//            lDialog.setCancelable(true);
//            lDialog.setCanceledOnTouchOutside(false);
//            lDialog.setMessage(message);
//            lDialog.setTitle(title);
//            lDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//            lDialog.show();
//        }
//
//        @Override
//        protected UserLoginResponse doInBackground(String... input) {
//
//            try {
//                String msg = String.format("{\"UserInitials\":\"%s\"}", input);
//                ObjectMapper mapper = new ObjectMapper();
//                Message thisMessage = new Message();
//                HttpMessageResolver httpResolver = new HttpMessageResolver();
//                thisMessage.setSource(deviceIMEI);
//                thisMessage.setMessageType("UserLogin");
//                thisMessage.setIncomingStatus(1); //default value
//                thisMessage.setIncomingMessage(msg);
//                thisMessage.setOutgoingStatus(0);   //default value
//                thisMessage.setOutgoingMessage("");
//                thisMessage.setInsertedTimeStamp(today);
//                thisMessage.setTTL(100);    //default value
//
//                //currentUserToken = "{\"RequestedInitials\" : \"LF \",\"UserId\" : \"348\",\"UserFirstName\" : \"Lebel\",\"UserLastName\" : \"Fuayuku\",\"UserCode\" : \"D1CE48\",\"Response\" : \"Success\"}";
//                currentUserToken = httpResolver.resolveMessageQueue(thisMessage);
//                currentUser = mapper.readValue(currentUserToken, UserLoginResponse.class);
//            } catch (Exception e) {
//                e.printStackTrace();
//                today = new java.sql.Timestamp(utilDate.getTime());
//                LogEntry log = new LogEntry(1L, ApplicationID, "ActChooser - UserLoginTask - doInBackground", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
//                logger.Log(log);
//            }
//            return currentUser;
//        }
//
//        @Override
//        protected void onPostExecute(UserLoginResponse userLoginResponse) {
//            if (lDialog != null && lDialog.isShowing()) lDialog.dismiss();
//            loginAttempt ++;
//            if (currentUser != null) {
//                loginAttempt = 0;
//                saveAuthentication();
//                ActChooser.this.setTitle(String.format("Hi %s", currentUser.getUserFirstName()));
//                flipper.setDisplayedChild(1);
//            } else {
//                if (loginAttempt < 2 && !currentUserToken.contains("Failure")) {
//                    logOn(initials);    // second automated attempt to counter that weird server anomaly
//                } else {
//                    // Refresh Activity to default
//                    currentUserToken = "";
//
//                    ActChooser.this.setTitle(getResources().getString(R.string.currentUser));
//                    soundPool.play(errorSoundId, 1, 1, 0, 0, 1);
//                    Vibrator vib = (Vibrator) ActChooser.this.getSystemService(Context.VIBRATOR_SERVICE);
//                    vib.vibrate(2000);  // Vibrate for 500 milliseconds
//                    String mMsg = "Login Error - Wrong initials \nPlease try again";
//                    AlertDialog.Builder builder = new AlertDialog.Builder(ActChooser.this);
//                    builder.setMessage(mMsg)
//                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int id) {
//                                    //do nothing
//                                }
//                            });
//                    builder.show();
//                }
//            }
//        }
//
//        @Override
//        protected void onCancelled() {
//            loginAttempt = 0;
//            loginTask = null;
//            if (lDialog != null && lDialog.isShowing()) lDialog.dismiss();
//            //refreshActivity();
//            //show the activity with user not authenticated - default
//        }
//    }
}