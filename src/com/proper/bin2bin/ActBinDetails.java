package com.proper.bin2bin;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.proper.Logger.LogHelper;
import com.proper.MessageQueue.HttpMessageResolver;
import com.proper.MessageQueue.Message;
import com.proper.data.*;
import com.proper.data.core.ICommunicator;
import com.proper.data.helpers.DialogHelper;
import com.proper.data.helpers.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lebel on 25/02/14.
 */
public class ActBinDetails extends FragmentActivity implements ICommunicator {
    private static final String QUEUE_NAME = "Bin2Bin";
    private String sourceBin = "";
    private String destinationBin = "";
    private java.util.Date utilDate = java.util.Calendar.getInstance().getTime();
    private java.sql.Timestamp today = null;
    private wsSendQueue sendQueueTask;
    private int transactionNumber = 0;
    private Button btnContinue = null;
    private Message thisMessage = null;
    private String deviceIMEI = "";
    private String ApplicationID = "";
    private SoundPool soundPool;
    private	int errorSoundId;
    private String backPressedParameter = "";
    private static final String paramTaskCompleted = "TRANSACTION_COMPLETED";
    private static final String paramTaskIncomplete = "TRANSACTION_INCOMPLETE";
    private UserLoginResponse currentUser = null;
    private ResponseHelper responseHelper = new ResponseHelper();
    private BinMoveResponse binMoveResponse = new BinMoveResponse();
    private LogHelper logger = new LogHelper();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyt_bindetails);
        TelephonyManager mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        deviceIMEI = mngr.getDeviceId();
        populateUiControls(savedInstanceState);
        thisMessage =  new Message();   // instantiate at least once on every load
        if (!btnContinue.isEnabled()) {
            btnContinue.setEnabled(true);
            btnContinue.setPaintFlags(btnContinue.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        errorSoundId = soundPool.load(getString(R.string.SOUND_ERROR), 0);

        UserAuthenticator authenticator = new UserAuthenticator(this);
        currentUser = authenticator.getCurrentUser();   //Gets current user
        if (!btnContinue.isEnabled()) btnContinue.setEnabled(true);
    }

    private void populateUiControls(Bundle form) {
        Bundle extras = getIntent().getExtras();
        SharedPreferences prefs = getSharedPreferences("devicePreferences", Context.MODE_PRIVATE);
        if (extras == null) {
            //Yell, Blue murder !
            return;
        }
        if (prefs == null) {
            //Yell, murda
            return;
        }
        sourceBin = extras.getString("SOURCE_EXTRA");
        destinationBin = extras.getString("DESTINATION_EXTRA");
        deviceIMEI = extras.getString("DEVICEIMEI_EXTRA");
        ApplicationID = extras.getString("APPLICATIONID_EXTRA");

        //Do we want to display the bin the source bin item content and quantity?
        //If we need to
        String intro = String.format("Are you sure, You want to authorise this BinMove\n" +
                "From:   %s\nTo:   %s", sourceBin, destinationBin);
        TextView lblIntro = (TextView) this.findViewById(R.id.LabelIntro);
        lblIntro.setText(intro);
        lblIntro.setVisibility(View.VISIBLE);
        btnContinue = (Button) this.findViewById(R.id.bnDetContinue);
        Button btnExit = (Button) this.findViewById(R.id.bnDetClose);
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnContinue_Clicked();
            }
        });
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnClose_Clicked();
            }
        });
    }

    private void btnClose_Clicked() {
        Intent resultIntent = new Intent();
        this.setResult(0, resultIntent);
        this.finish();
    }

    private void btnContinue_Clicked() {
        UserAuthenticator authenticator = new UserAuthenticator(this);
        currentUser = currentUser != null ? currentUser : authenticator.getCurrentUser();  //Gets currently authenticated user
        if (currentUser != null) {
            today = new java.sql.Timestamp(utilDate.getTime());
            //BinMove move = new BinMove(1L, sourceBin, destinationBin, 0, null, today);
            String msg = String.format("{\"SrcBin\":\"%s\", \"DstBin\":\"%s\", \"UserId\":\"%s\", \"UserCode\":\"%s\"}",
                    sourceBin, destinationBin, currentUser.getUserId(), currentUser.getUserCode());

            thisMessage.setSource(deviceIMEI);
            thisMessage.setMessageType("BinMove");
            thisMessage.setIncomingStatus(1); //default value
            thisMessage.setIncomingMessage(msg);
            thisMessage.setOutgoingStatus(0);   //default value
            thisMessage.setOutgoingMessage("");
            thisMessage.setInsertedTimeStamp(today);
            thisMessage.setTTL(100);    //default value
            sendQueueTask = new wsSendQueue();
            sendQueueTask.execute(thisMessage);  //executes both -> Send Queue Directly AND Send queue to Service
        } else {
            soundPool.play(errorSoundId, 1, 1, 0, 0, 1);
            Vibrator vib = (Vibrator) ActBinDetails.this.getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 500 milliseconds
            vib.vibrate(2000);
            String mMsg = "User not Authenticated \nPlease login";
            AlertDialog.Builder builder = new AlertDialog.Builder(ActBinDetails.this);
            builder.setMessage(mMsg)
                    .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //do nothing
                        }
                    });
            builder.show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (sendQueueTask != null && sendQueueTask.isCancelled() == false) {
            sendQueueTask.cancel(true);
        }
        Intent resultIntent = new Intent();
        if (backPressedParameter != null && !backPressedParameter.equalsIgnoreCase("")) {
            setResult(1, resultIntent);
        } else {
            setResult(0, resultIntent);
        }
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("HISTORY_EXTRA", new TransactionHistory(1L, transactionNumber, deviceIMEI, today, false));
            ActBinDetails.this.setResult(transactionNumber, resultIntent);
            ActBinDetails.this.finish();
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

    private class wsSendQueue extends AsyncTask<Message, Void, BinMoveResponse> {
        protected ProgressDialog wsDialog;

        @Override
        protected void onPostExecute(final BinMoveResponse response) {
            if (wsDialog != null && wsDialog.isShowing()) {
                wsDialog.dismiss();
            }
            if (response != null) {
                String msg = "";
                AlertDialog.Builder builder = new AlertDialog.Builder(ActBinDetails.this);
                if (response.getResult().equalsIgnoreCase("Success")) {
                    //****************  Report all warnings ********************
                    int warnings = 0;
                    if (response.getMessages() != null) {
                        for (BinMoveMessage m : response.getMessages()) {
                            if (m.getMessageName().equalsIgnoreCase("warning")) {
                                warnings ++;
                            }
                        }
                    }
                    msg = String.format("Success: BinMove Completed with %s warnings", warnings);

                    builder.setMessage(msg)
                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
//                *****************     Requested to be removed by Scott       ***********************
//                                    backPressedParameter = paramTaskCompleted;
//                                    Intent i = new Intent(ActBinDetails.this, ActInfo.class);
//                                    i.putExtra("RESPONSE_EXTRA", response);
//                                    i.putExtra("ACTION_EXTRA", R.integer.ACTION_BINMOVE);
//                                    startActivityForResult(i, 0);
                                Intent resultIntent = new Intent();
                                ActBinDetails.this.setResult(RESULT_OK, resultIntent);
                                ActBinDetails.this.finish();
                                }
                            });
                } else {
                    msg = "Failed: BinMove NOT Completed because it broke many rules";

                    builder.setMessage(msg)
                            .setNegativeButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
//                *****************     Requested to be removed by Scott       ***********************
//                                    backPressedParameter = paramTaskCompleted;
//                                    Intent i = new Intent(ActBinDetails.this, ActInfo.class);
//                                    i.putExtra("RESPONSE_EXTRA", response);
//                                    i.putExtra("ACTION_EXTRA", R.integer.ACTION_BINMOVE);
//                                    startActivityForResult(i, 0);
                                    Intent resultIntent = new Intent();
                                    ActBinDetails.this.setResult(RESULT_CANCELED, resultIntent);
                                    ActBinDetails.this.finish();
                                }
                            });
                }

                builder.show();
                if (ActBinDetails.this.btnContinue.isEnabled()) {
                    ActBinDetails.this.btnContinue.setEnabled(false);
                    btnContinue.setPaintFlags(btnContinue.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                }
            } else {
                //Response is null the disable Yes button:
                AlertDialog.Builder builder = new AlertDialog.Builder(ActBinDetails.this);
                String msg = "Failed: BinMove NOT Completed because of network error, please contact IT for help";
                builder.setMessage(msg)
                        .setNegativeButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Simulate onBackPressed and pass parameter for next task
                                backPressedParameter = paramTaskCompleted;
                                if (btnContinue.isEnabled()) btnContinue.setEnabled(false);
                            }
                        });
            }
        }

        @Override
        protected BinMoveResponse doInBackground(Message... msg) {
            HttpMessageResolver resolver = new HttpMessageResolver();
            String response = resolver.resolveMessageQuery(msg[0]);
            //response = responseHelper.refineOutgoingMessage(response);    Not need to add columns like: PackshotURL etc...

            if (response != null && !response.equalsIgnoreCase("")) {
                //ObjectMapper mapper = new ObjectMapper();
                try {
                    //binMoveResponse = mapper.readValue(response.getBytes(), BinMoveResponse.class);
                    JSONObject resp = new JSONObject(response);
                    JSONArray messages = resp.getJSONArray("Messages");
                    JSONArray actions = resp.getJSONArray("MessageObjects");
                    String RequestedSrcBin = resp.getString("RequestedSrcBin");
                    String RequestedDstBin = resp.getString("RequestedDstBin");
                    String Result = resp.getString("Result");
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
                    binMoveResponse.setRequestedSrcBin(RequestedSrcBin);
                    binMoveResponse.setRequestedDstBin(RequestedDstBin);
                    binMoveResponse.setResult(Result);
                    binMoveResponse.setMessages(messageList);
                    binMoveResponse.setMessageObjects(actionList);
                } catch (JSONException e) {
                    e.printStackTrace();
                    today = new java.sql.Timestamp(utilDate.getTime());
                    LogEntry log = new LogEntry(1L, ApplicationID, "ActBinDetail [sendQueueTask] - doInBackground", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                    logger.Log(log);
                }
            }
            return binMoveResponse;
        }

        @Override
        protected void onPreExecute() {
            wsDialog = new ProgressDialog(ActBinDetails.this);
            CharSequence message = "Working hard...sending queue [directly] [to webservice]...";
            CharSequence title = "Please Wait";
            wsDialog.setCancelable(true);
            wsDialog.setCanceledOnTouchOutside(false);
            wsDialog.setMessage(message);
            wsDialog.setTitle(title);
            wsDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            wsDialog.show();
        }

        @Override
        protected void onCancelled() {
            if (wsDialog != null && wsDialog.isShowing()) {
                wsDialog.dismiss();
            }
            sendQueueTask.cancel(true);
            super.onCancelled();
        }
    }
}