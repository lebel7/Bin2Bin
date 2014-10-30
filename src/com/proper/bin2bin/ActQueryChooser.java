package com.proper.bin2bin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by Lebel on 25/04/2014.
 */
public class ActQueryChooser extends Activity {
    private String deviceID = "";
    private String deviceIMEI = "";
    private int soundId = 0;
    private int errorSoundId = 0;
    private SoundPool soundPool;
    private static final String ApplicationID = "Bin2Bin";
    private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private java.util.Date utilDate = java.util.Calendar.getInstance().getTime();
    private java.sql.Timestamp today = null;
    private int NAV_INSTRUCTION = 0;
    private Button btnQryBin;
    private Button btnQryBarcode;
    private Button btnQryBarcodeBin;
    private Button btnExit;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyt_querychooser);
        SharedPreferences prefs = getSharedPreferences(getString(R.string.preference_credentials), Context.MODE_PRIVATE);
        //if (prefs != null)
        deviceID = prefs.getString("Device", "");   //give us exception if failed
        deviceIMEI = prefs.getString("IMEI", "");
        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        soundId = soundPool.load(this, R.raw.barcodebeep, 0);
        errorSoundId = soundPool.load(this, R.raw.serror, 0);

        configureUI(savedInstanceState);
    }

    private void configureUI(Bundle bundle) {
        btnQryBin = (Button) this.findViewById(R.id.bnQryChooserBin);
        btnQryBarcode = (Button) this.findViewById(R.id.bnQryChooserBarcode);
        btnQryBarcodeBin = (Button) this.findViewById(R.id.bnQryChooserBarcodeBin);
        btnExit = (Button) this.findViewById(R.id.bnExitActQryChooser);

        btnQryBin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ButtonClicked(view);
            }
        });
        btnQryBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ButtonClicked(view);
            }
        });
        btnQryBarcodeBin.setOnClickListener(new View.OnClickListener() {
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

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            //Do some assignments here
        } else {
            //Yell Murder !
        }
    }

    private void ButtonClicked(View view) {
        if (view == btnQryBarcode) {
            NAV_INSTRUCTION = R.integer.ACTION_BARCODEQUERY;
            if (!deviceID.isEmpty()) {
                if (deviceID.equalsIgnoreCase(getString(R.string.SmallDevice))) {
                    Intent i = new Intent(com.proper.bin2bin.ActQueryChooser.this, com.android.barcode.ActQueryScan.class);
                    i.putExtra("INSTRUCTION_EXTRA", NAV_INSTRUCTION);
                    startActivityForResult(i, NAV_INSTRUCTION);
                }
                if (deviceID.equalsIgnoreCase(getString(R.string.LargeDevice))) {
                    Intent i = new Intent(com.proper.bin2bin.ActQueryChooser.this, com.chainway.ht.ui.ActQueryScan.class);
                    i.putExtra("INSTRUCTION_EXTRA", NAV_INSTRUCTION);
                    startActivityForResult(i, NAV_INSTRUCTION);
                }
            } else {
                //prompt deviceID has not been identified
                soundPool.play(errorSoundId, 1, 1, 0, 0, 1);
                Vibrator vib = (Vibrator) ActQueryChooser.this.getSystemService(Context.VIBRATOR_SERVICE);
                // Vibrate for 500 milliseconds
                vib.vibrate(2000);
                String mMsg = "User not Authenticated \nPlease login";
                AlertDialog.Builder builder = new AlertDialog.Builder(ActQueryChooser.this);
                builder.setMessage(mMsg)
                        .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do nothing
                            }
                        });
                builder.show();
            }
        } else if (view == btnQryBarcodeBin) {
            NAV_INSTRUCTION = R.integer.ACTION_BARCODE_BINQUERY;
            if (!deviceID.isEmpty()) {
                if (deviceID.equalsIgnoreCase(getString(R.string.SmallDevice))) {
                    Intent i = new Intent(com.proper.bin2bin.ActQueryChooser.this, com.android.barcode.ActQueryScan.class);
                    i.putExtra("INSTRUCTION_EXTRA", NAV_INSTRUCTION);
                    startActivityForResult(i, NAV_INSTRUCTION);
                }
                if (deviceID.equalsIgnoreCase(getString(R.string.LargeDevice))) {
                    Intent i = new Intent(com.proper.bin2bin.ActQueryChooser.this, com.chainway.ht.ui.ActQueryScan.class);
                    i.putExtra("INSTRUCTION_EXTRA", NAV_INSTRUCTION);
                    startActivityForResult(i, NAV_INSTRUCTION);
                }
            } else {
                //prompt deviceID has not been identified
                soundPool.play(errorSoundId, 1, 1, 0, 0, 1);
                Vibrator vib = (Vibrator) ActQueryChooser.this.getSystemService(Context.VIBRATOR_SERVICE);
                // Vibrate for 500 milliseconds
                vib.vibrate(2000);
                String mMsg = "User not Authenticated \nPlease login";
                AlertDialog.Builder builder = new AlertDialog.Builder(ActQueryChooser.this);
                builder.setMessage(mMsg)
                        .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do nothing
                            }
                        });
                builder.show();
            }
        } else if (view == btnQryBin) {
            NAV_INSTRUCTION = R.integer.ACTION_BINQUERY;
            if (!deviceID.isEmpty()) {
                if (deviceID.equalsIgnoreCase(getString(R.string.SmallDevice))) {
                    Intent i = new Intent(com.proper.bin2bin.ActQueryChooser.this, com.android.barcode.ActQueryScan.class);
                    i.putExtra("INSTRUCTION_EXTRA", NAV_INSTRUCTION);
                    startActivityForResult(i, NAV_INSTRUCTION);
                }
                if (deviceID.equalsIgnoreCase(getString(R.string.LargeDevice))) {
                    Intent i = new Intent(com.proper.bin2bin.ActQueryChooser.this, com.chainway.ht.ui.ActQueryScan.class);
                    i.putExtra("INSTRUCTION_EXTRA", NAV_INSTRUCTION);
                    startActivityForResult(i, NAV_INSTRUCTION);
                }
            } else {
                //prompt deviceID has not been identified
                soundPool.play(errorSoundId, 1, 1, 0, 0, 1);
                Vibrator vib = (Vibrator) ActQueryChooser.this.getSystemService(Context.VIBRATOR_SERVICE);
                // Vibrate for 500 milliseconds
                vib.vibrate(2000);
                String mMsg = "User not Authenticated \nPlease login";
                AlertDialog.Builder builder = new AlertDialog.Builder(ActQueryChooser.this);
                builder.setMessage(mMsg)
                        .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do nothing
                            }
                        });
                builder.show();
            }
        } else if (view == btnExit) {
            this.finish();
        } else {
            throw new NullPointerException("Well Done! You have triggered a button that doesn't exist in this reality");
        }
    }
}