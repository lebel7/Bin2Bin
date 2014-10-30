package com.proper.bin2bin;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import com.proper.Logger.LogHelper;
import com.proper.MessageQueue.Message;
import com.proper.data.Bin;
import com.proper.data.ProductResponse;
import com.proper.data.UserAuthenticator;
import com.proper.data.UserLoginResponse;
import com.proper.data.helpers.ResponseHelper;

/**
 * Created by Lebel on 06/06/2014.
 */
public class ActProductMoveDialog extends Activity {
    private String deviceIMEI = "";
    private String ApplicationID = "";
    private SoundPool soundPool;
    private	int errorSoundId;
    private java.util.Date utilDate = java.util.Calendar.getInstance().getTime();
    private java.sql.Timestamp today = null;
    private UserLoginResponse currentUser = null;
    private ResponseHelper responseHelper = new ResponseHelper();
    private LogHelper logger = new LogHelper();
    private ProductResponse thisProduct = new ProductResponse();
    private Bin thisBin = new Bin();
    private Message thisMessage = null;
    private Button btnContinue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyt_productmovedialog);
        TelephonyManager mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        deviceIMEI = mngr.getDeviceId();
        //define controls
        btnContinue = (Button) this.findViewById(R.id.bnContinueProductMoveDialog);
        Button btnExit = (Button) this.findViewById(R.id.bnCloseProductMoveDialog);
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button_clicked(view);
            }
        });
        Bundle extras = getIntent().getExtras();
        SharedPreferences prefs = getSharedPreferences("devicePreferences", Context.MODE_PRIVATE);
        thisProduct = (ProductResponse) extras.getSerializable("PRODUCT_EXTRA");
        thisBin = (Bin) extras.getSerializable("BIN_EXTRA");
        //
        thisMessage =  new Message();   // instantiate at least once on every load
        if (!btnContinue.isEnabled()) {
            btnContinue.setEnabled(true);
            btnContinue.setPaintFlags(btnContinue.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        errorSoundId = soundPool.load(getString(R.string.SOUND_ERROR), 0);

        UserAuthenticator authenticator = new UserAuthenticator(this);
        currentUser = authenticator.getCurrentUser();   //Gets current user
    }

    private void Button_clicked(View view) {
        switch (view.getId()) {
            case R.id.bnCloseProductMoveDialog:
                this.finish();
                break;
            case R.id.bnContinueProductMoveDialog:
                //do
                break;
        }
    }
}