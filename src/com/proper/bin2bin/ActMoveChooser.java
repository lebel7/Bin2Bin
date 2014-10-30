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
import android.view.View;
import android.widget.Button;

import java.util.AbstractMap;

/**
 * Created by Lebel on 25/06/2014.
 */
public class ActMoveChooser extends Activity {
    private String deviceID = "";
    private int soundId = 0;
    private int errorSoundId = 0;
    private SoundPool soundPool;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyt_movechooser);

        SharedPreferences prefs = getSharedPreferences(getString(R.string.preference_credentials), Context.MODE_PRIVATE);
        //if (prefs != null)
        deviceID = prefs.getString("Device", "");   //give us exception if failed
        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        soundId = soundPool.load(this, R.raw.barcodebeep, 0);
        errorSoundId = soundPool.load(this, R.raw.serror, 0);

        Button btnWholeBin = (Button) this.findViewById(R.id.bnMoveChooserWholeBin);
        Button btnMany = (Button) this.findViewById(R.id.bnMoveChooserMany);
        Button btnExit = (Button) this.findViewById(R.id.bnExitActMoveChooser);
        btnWholeBin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ButtonClicked(view);
            }
        });
        btnMany.setOnClickListener(new View.OnClickListener() {
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
    }

    private void ButtonClicked(View view) {
        switch (view.getId()) {
            case R.id.bnExitActMoveChooser:
                Intent done = new Intent();
                setResult(Activity.RESULT_OK, done);
                this.finish();
                break;
            case R.id.bnMoveChooserMany:
                if (!deviceID.isEmpty()) {
                    if (deviceID.equalsIgnoreCase(getString(R.string.SmallDevice))) {
                        Intent i = new Intent(com.proper.bin2bin.ActMoveChooser.this, com.android.barcode.ActBinMain.class);
                        i.putExtra("INSTRUCTION", R.integer.ACTION_PARTIALMOVE);
                        this.startActivityForResult(i, 1);
//                        NavigationTask nav = new NavigationTask();
//                        nav.execute(new AbstractMap.SimpleEntry<Integer, Class<?>>(R.integer.ACTION_BINMOVE, com.android.barcode.ActBinMain.class));
                    }
                    if (deviceID.equalsIgnoreCase(getString(R.string.LargeDevice))) {
                        Intent i = new Intent(com.proper.bin2bin.ActMoveChooser.this, com.chainway.ht.ui.ActBinMain.class);
                        i.putExtra("INSTRUCTION", R.integer.ACTION_PARTIALMOVE);
                        this.startActivityForResult(i, 1);
                    }
                } else {
                    //prompt deviceID has not been identified
                    soundPool.play(errorSoundId, 1, 1, 0, 0, 1);
                    Vibrator vib = (Vibrator) ActMoveChooser.this.getSystemService(Context.VIBRATOR_SERVICE);
                    // Vibrate for 500 milliseconds
                    vib.vibrate(2000);
                    String mMsg = "User not Authenticated \nPlease login";
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActMoveChooser.this);
                    builder.setMessage(mMsg)
                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //do nothing
                                }
                            });
                    builder.show();
                }
                break;
            case R.id.bnMoveChooserWholeBin:
                if (!deviceID.isEmpty()) {
                    if (deviceID.equalsIgnoreCase(getString(R.string.SmallDevice))) {
                        Intent i2 = new Intent(com.proper.bin2bin.ActMoveChooser.this, com.android.barcode.ActBinMain.class);
                        i2.putExtra("INSTRUCTION", R.integer.ACTION_BINMOVE);
                        this.startActivityForResult(i2, 1);
                }
                    if (deviceID.equalsIgnoreCase(getString(R.string.LargeDevice))) {
                        Intent i2 = new Intent(com.proper.bin2bin.ActMoveChooser.this, com.chainway.ht.ui.ActBinMain.class);
                        i2.putExtra("INSTRUCTION", R.integer.ACTION_BINMOVE);
                        this.startActivityForResult(i2, 1);
                    }
                } else {
                    //prompt deviceID has not been identified
                    soundPool.play(errorSoundId, 1, 1, 0, 0, 1);
                    Vibrator vib = (Vibrator) ActMoveChooser.this.getSystemService(Context.VIBRATOR_SERVICE);
                    // Vibrate for 500 milliseconds
                    vib.vibrate(2000);
                    String mMsg = "User not Authenticated \nPlease login";
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActMoveChooser.this);
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

    private class NavigationTask extends AsyncTask<AbstractMap.SimpleEntry<Integer, Class<?>>, Void, Void> {
        private ProgressDialog lDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            lDialog = new ProgressDialog(ActMoveChooser.this);
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
        protected Void doInBackground(AbstractMap.SimpleEntry<Integer, Class<?>>... nav) {
            Intent i = new Intent(com.proper.bin2bin.ActMoveChooser.this, nav.getClass());
            i.putExtra("INSTRUCTION", nav[0].getKey());
            startActivityForResult(i, 1);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (lDialog != null && lDialog.isShowing()) lDialog.dismiss();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }
}