package com.proper.bin2bin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.proper.Logger.LogHelper;
import com.proper.data.*;
import com.proper.data.adapters.MoveActionAdapter;
import com.proper.data.adapters.MoveMessageAdapter;

/**
 * Created by Lebel on 16/04/2014.
 */
public class ActInfo extends Activity {
    private String deviceIMEI = "";
    private static final String ApplicationID = "Bin2Bin";
    private java.util.Date utilDate = java.util.Calendar.getInstance().getTime();
    private java.sql.Timestamp today = null;
    private TextView txtHeader;
    private ListView lvActions;
    private ListView lvMessages;
    private BinMoveResponse moveResponse = new BinMoveResponse();
    private LogHelper logger = new LogHelper();

    public BinMoveObject getSelectedAction() {
        return selectedAction;
    }

    public void setSelectedAction(BinMoveObject selectedAction) {
        this.selectedAction = selectedAction;
    }

    public BinMoveMessage getSelectedMessage() {
        return selectedMessage;
    }

    public void setSelectedMessage(BinMoveMessage selectedMessage) {
        this.selectedMessage = selectedMessage;
    }

    private BinMoveObject selectedAction;
    private BinMoveMessage selectedMessage;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyt_info);
        TelephonyManager mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        deviceIMEI = mngr.getDeviceId();

        Intent bundle = getIntent();
        Bundle extras =  bundle.getExtras();
        int action = extras.getInt("ACTION_EXTRA");
        switch (action) {
            case R.integer.ACTION_PARTIALMOVE:
                PartialBinMoveResponse partial = (PartialBinMoveResponse) extras.getSerializable("RESPONSE_EXTRA");
                moveResponse = new BinMoveResponse(partial);
                break;
            case R.integer.ACTION_BINMOVE:
                moveResponse = (BinMoveResponse) extras.getSerializable("RESPONSE_EXTRA");
                break;
        }

        if (moveResponse != null) {// empty for now
        } else {
            //Explain Message and crash App or Kill
            String msg = "The Response object must not be null.";
            today = new java.sql.Timestamp(utilDate.getTime());
            LogEntry log = new LogEntry(1L, ApplicationID, "ActInfo - onCreate - Line:72", deviceIMEI, RuntimeException.class.getSimpleName(), msg, today);
            logger.Log(log);
            throw new RuntimeException(msg + " Please Contact an IT staff");
            //super.onCreate(savedInstanceState);
            //return;
    }
    txtHeader = (TextView) this.findViewById(R.id.txtvInfoHeader);
    lvActions = (ListView) this.findViewById(R.id.lvMessageActions);
    lvMessages = (ListView) this.findViewById(R.id.lvMessages);
    lvActions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
            lvAction_Item_Clicked(adapterView, view, pos, id);
        }
    });
    lvMessages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
            lvMessages_Item_clicked(adapterView, view, pos, id);
        }
    });

//        LayoutInflater inflater = (LayoutInflater) this
//                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

//        MoveActionAdapter actionAdapter = new MoveActionAdapter(this, inflater, moveResponse.getMessageObjects());
//        MoveMessageAdapter msgAdapter = new MoveMessageAdapter(this, inflater, moveResponse.getMessages());
        MoveActionAdapter actionAdapter = new MoveActionAdapter(this, moveResponse.getMessageObjects());
        MoveMessageAdapter msgAdapter = new MoveMessageAdapter(this, moveResponse.getMessages());
        lvActions.setAdapter(actionAdapter);
        lvMessages.setAdapter(msgAdapter);
    }

    private void lvMessages_Item_clicked(AdapterView<?> adapterView, View view, int pos, long id) {
        if (moveResponse != null) {
            setSelectedMessage(moveResponse.getMessages().get(pos));
        }
    }

    private void lvAction_Item_Clicked(AdapterView<?> adapterView, View view, int pos, long id) {
        if (moveResponse != null) {
            setSelectedAction(moveResponse.getMessageObjects().get(pos));
        }
    }

}