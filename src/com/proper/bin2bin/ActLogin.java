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
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.proper.Logger.LogHelper;
import com.proper.MessageQueue.HttpMessageResolver;
import com.proper.MessageQueue.Message;
import com.proper.data.Contact;
import com.proper.data.LogEntry;
import com.proper.data.UserAuthenticator;
import com.proper.data.UserLoginResponse;
import com.proper.utils.DeviceUtils;
import org.codehaus.jackson.map.ObjectMapper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by Lebel on 18/08/2014.
 */
public class ActLogin extends Activity {
    private SharedPreferences prefs = null;
    private String deviceID = "";
    private String deviceIMEI = "";
    private static final String ApplicationID = "BinMove";
    private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private java.util.Date utilDate = java.util.Calendar.getInstance().getTime();
    private java.sql.Timestamp today = null;
    private Button btnLogin;
    private EditText txtInitials;
    private EditText txtPin;
    private ImageView logo;
    private SoundPool soundPool;
    //private	int soundId;
    private int errorSoundId;
    private UserAuthenticator authenticator = null;
    private UserLoginTask loginTask;
    private UserLoginResponse currentUser;
    private String currentUserToken = "";
    private String initials = "";
    private String pin = "";
    private int loginAttempt = 0;
    private ArrayAdapter<Contact> adapter;
    private LogHelper logger = new LogHelper();
    private DeviceUtils device = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyt_login);
        authenticator = new UserAuthenticator(this);    //set-up simulated authentication service
        device = new DeviceUtils(this);
        deviceIMEI =  device.getIMEI();
        deviceID = device.getDeviceID();

        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);

        //View screen = null;

        if (deviceID.equalsIgnoreCase(getResources().getString(R.string.SmallDevice))) {
            errorSoundId = soundPool.load(getResources().getString(R.string.SOUND_ERROR), 1);
            //screen = (ScrollView) this.findViewById(R.id.screenLogin);
        }
        if (deviceID.equalsIgnoreCase(getResources().getString(R.string.LargeDevice))) {
            errorSoundId = soundPool.load(this, R.raw.serror, 1);
            //screen = (LinearLayout) this.findViewById(R.id.screenLogin);
        }

        LinearLayout screen = (LinearLayout) this.findViewById(R.id.screenLogin);
        txtInitials = (EditText) this.findViewById(R.id.etxtLoginInitials);
        txtPin = (EditText) this.findViewById(R.id.etxtLoginPin);
        btnLogin = (Button) this.findViewById(R.id.bnLoginProceed);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ButtonClicked(v);
            }
        });
        txtInitials.addTextChangedListener(new TextChanged(this.txtInitials));
        txtPin.addTextChangedListener(new TextChanged(this.txtPin));
        TextView lblLoginTitle = (TextView) this.findViewById(R.id.lblLoginTitle);
        logo = (ImageView) this.findViewById(R.id.imgLogo);
        //opening transition animations (opening, closing)
        //overridePendingTransition(R.anim.activity_open_translate,R.anim.activity_close_scale);

//        Animation animRightIn = AnimationUtils.loadAnimation(this, R.anim.right_in);
//        Animation animRotateIn_icon = AnimationUtils.loadAnimation(this, R.anim.rotate);
//
//        screen.startAnimation(animRightIn);
//        txtInitials.startAnimation(animRightIn);
//        txtPin.startAnimation(animRightIn);
//        btnLogin.startAnimation(animRightIn);
//        lblLoginTitle.startAnimation(animRightIn);
//        logo.startAnimation(animRotateIn_icon);
    }

    private void saveAuthentication() {
        // Save to the sharedPreference
        prefs = getSharedPreferences(getString(R.string.preference_credentials), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("ApplicationID", ApplicationID);
        editor.putString("IMEI", deviceIMEI);
        editor.putString("Device", deviceID);
        editor.putString("UserToken", currentUserToken);
        editor.commit();
    }

    private void removeAuthentication() {
        // Save to the sharedPreference
        prefs = getSharedPreferences(getString(R.string.preference_credentials), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("UserToken");
        editor.remove("IMEI");
        editor.remove("Device");
        editor.commit();
    }

    private void logOn(String initials, String pin) {
        //boolean success = false;
        if (currentUserToken.isEmpty() && !initials.isEmpty() && !pin.isEmpty()) {
            hideSoftKeyboard(ActLogin.this);  //Hide the default software Keyboard
            loginTask = new UserLoginTask();
            loginTask.execute(initials, pin);
            //if (currentUser != null) success = true;
        }else {
            ActLogin.this.setTitle(getResources().getString(R.string.currentUser));
            String msgMissingInitials = "Login Error - Missing Initials\nPlease try again";
            String msgMissingPin = "Login Error - Missing Pin\nPlease try again";
            String msgMissingBoth = "Login Error - No Details Entered\nPlease enter initials and pin";
            soundPool.play(errorSoundId, 1, 1, 0, 0, 1);
            Vibrator vib = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
            AlertDialog.Builder builder = new AlertDialog.Builder(ActLogin.this);
            if (!initials.isEmpty() && pin.isEmpty()) {
                //if initials is entered but no pin
                builder.setMessage(msgMissingPin)
                        .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
            } else if (initials.isEmpty() && !pin.isEmpty()) {
                //if pin is entered no initials
                builder.setMessage(msgMissingInitials)
                        .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
            } else {
                //Both missing
                builder.setMessage(msgMissingBoth)
                        .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
            }
            vib.vibrate(2000);  // Vibrate for 500 milliseconds
            builder.show();
        }
        //return success;
    }

    private void logOut() {
        if (currentUser != null) {
            currentUser = null;
            currentUserToken = "";
            txtInitials.setText("");
            txtPin.setText("");
            initials = "";
            pin = "";
            removeAuthentication();
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    private void ButtonClicked(View v) {
        //do
        if (v == btnLogin) {
            logOn(initials, pin);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (authenticator.getCurrentUser() != null) {
                if (authenticator.isAuthenticated()) {
                    logOut();
                }
            } else {
                logOut();
            }
            switch (resultCode) {
                case RESULT_OK:
                    logOut();
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            today = new java.sql.Timestamp(utilDate.getTime());
            LogEntry log = new LogEntry(1L, ApplicationID, "ActLogin - Attempting Logout - onActivityResult", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
            logger.Log(log);
        }
        this.finish();
    }

//    @Override
//    protected void onPause() {
//        overridePendingTransition(R.anim.activity_open_scale,R.anim.activity_close_translate);
//        super.onPause();
//    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        Animation animRotateIn_big = AnimationUtils.loadAnimation(this, R.anim.rotate);
//        logo.startAnimation(animRotateIn_big);
//    }

    @Override
    protected void onDestroy() {
        try {
            logOut();
        } catch (Exception e) {
            e.printStackTrace();
            today = new java.sql.Timestamp(utilDate.getTime());
            LogEntry log = new LogEntry(1L, ApplicationID, "ActChooser - Attempting Logout - onDestroy", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
            logger.Log(log);
        }
        super.onDestroy();
        android.os.Process.killProcess(android.os.Process.myPid()); //kill it!
    }

    class TextChanged implements TextWatcher {
        private EditText thisView;

        private TextChanged(EditText view) {
            this.thisView = view;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (thisView == txtInitials) {
                initials = editable.toString().toUpperCase(Locale.getDefault());
            }
            if (thisView == txtPin) {
                pin = editable.toString();
            }
        }
    }

    private class UserLoginTask extends AsyncTask<String, Void, UserLoginResponse> {
        private ProgressDialog lDialog;

        @Override
        protected void onPreExecute() {
            lDialog = new ProgressDialog(ActLogin.this);
            CharSequence message = "Working hard...checking credentials...";
            CharSequence title = "Please Wait";
            lDialog.setCancelable(true);
            lDialog.setCanceledOnTouchOutside(false);
            lDialog.setMessage(message);
            lDialog.setTitle(title);
            lDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            lDialog.show();
        }

        @Override
        protected UserLoginResponse doInBackground(String... input) {

            try {
                String initials = input[0];
                String pin = input[1];
                //String msg = String.format("{\"UserInitials\":\"%s\"}", input);
                String msg = String.format("{\"UserInitials\":\"%s\", \"UserPin\":\"%s\"}", initials, pin);
                ObjectMapper mapper = new ObjectMapper();
                Message thisMessage = new Message();
                today = new java.sql.Timestamp(utilDate.getTime());
                HttpMessageResolver httpResolver = new HttpMessageResolver();
                thisMessage.setSource(deviceIMEI);
                thisMessage.setMessageType("UserLogin");
                thisMessage.setIncomingStatus(1); //default value
                thisMessage.setIncomingMessage(msg);
                thisMessage.setOutgoingStatus(0);   //default value
                thisMessage.setOutgoingMessage("");
                thisMessage.setInsertedTimeStamp(today);
                thisMessage.setTTL(100);    //default value

                //currentUserToken = "{\"RequestedInitials\" : \"LF \",\"UserId\" : \"348\",\"UserFirstName\" : \"Lebel\",\"UserLastName\" : \"Fuayuku\",\"UserCode\" : \"D1CE48\",\"Response\" : \"Success\"}";
                currentUserToken = httpResolver.resolveMessageQueue(thisMessage);
                currentUser = mapper.readValue(currentUserToken, UserLoginResponse.class);
            } catch (Exception e) {
                if (lDialog != null && lDialog.isShowing()) lDialog.dismiss();
                if (!loginTask.isCancelled()) loginTask.cancel(true);
                e.printStackTrace();
                today = new java.sql.Timestamp(utilDate.getTime());
                LogEntry log = new LogEntry(1L, ApplicationID, "ActChooser - UserLoginTask - doInBackground", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                logger.Log(log);
                if (!loginTask.isCancelled()) loginTask.cancel(true);
            }
            return currentUser;
        }

        @Override
        protected void onPostExecute(UserLoginResponse userLoginResponse) {
            if (lDialog != null && lDialog.isShowing()) lDialog.dismiss();
            loginAttempt ++;
            if (currentUser != null) {
                loginAttempt = 0;
                saveAuthentication();
                ActLogin.this.setTitle(String.format("Hi %s", currentUser.getUserFirstName()));
                //flipper.setDisplayedChild(1);
                //do navigation to chooser
                Intent i = new Intent(ActLogin.this, ActChooser.class);
                startActivityForResult(i, RESULT_FIRST_USER);
            } else {
                if (loginAttempt <= 2 && !currentUserToken.contains("Failure")) {
                    //boolean success = logOn(initials, pin);    // second automated attempt to counter that weird server anomaly
                    logOn(initials, pin);
                } else {
                    // Refresh Activity to default
                    currentUserToken = "";

                    ActLogin.this.setTitle(getResources().getString(R.string.currentUser));
                    soundPool.play(errorSoundId, 1, 1, 0, 0, 1);
                    Vibrator vib = (Vibrator) ActLogin.this.getSystemService(Context.VIBRATOR_SERVICE);
                    vib.vibrate(2000);  // Vibrate for 500 milliseconds
                    String mMsg = "Login Error\nYour Initials/Pin combination is incorrect. Please try again";
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActLogin.this);
                    builder.setMessage(mMsg)
                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //do nothing
                                }
                            });
                    builder.show();
                }
            }
        }

        @Override
        protected void onCancelled() {
            loginAttempt = 0;
            loginTask = null;
            if (lDialog != null && lDialog.isShowing()) lDialog.dismiss();
            //refreshActivity();
            //show the activity with user not authenticated - default
        }
    }
}