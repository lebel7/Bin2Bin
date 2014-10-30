package com.proper.data.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.proper.bin2bin.R;
import com.proper.data.BinMoveMessage;

import java.util.List;

/**
 * Created by Lebel on 02/06/2014.
 */
public class MoveMessageAdapter extends BaseAdapter {
    private Context context;
    private List<BinMoveMessage> theseMsg;

    public MoveMessageAdapter(Context context, List<BinMoveMessage> messages) {
        this.context = context;
        this.theseMsg = messages;
    }

    @Override
    public int getCount() {
        return theseMsg.size();
    }

    @Override
    public BinMoveMessage getItem(int i) {
        return theseMsg.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int pos, View view, ViewGroup viewGroup) {
        View myView = view;
        if (myView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            myView = inflater.inflate(R.layout.list_binmessage_item, viewGroup, false);
        }
        BinMoveMessage msg = theseMsg.get(pos);
        //assign control values and display
        TextView lblMessageName = (TextView) myView.findViewById(R.id.lblMessageName);
        TextView txtMessageName = (TextView) myView.findViewById(R.id.txtvMessageName);
        TextView lblMessageText = (TextView) myView.findViewById(R.id.lblMessageText);
        TextView txtMessageText = (TextView) myView.findViewById(R.id.txtvMessageText);
        TextView lblMessageTimestamp = (TextView) myView.findViewById(R.id.lblMessageTimestamp);
        TextView txtMessageTimeStamp = (TextView) myView.findViewById(R.id.txtvMessageTimeStamp);

        lblMessageName.setText("Name:    ");
        lblMessageText.setText("Text:    ");
        lblMessageTimestamp.setText("TimeStamp:     ");
        txtMessageName.setText(msg.getMessageName());
        txtMessageText.setText(msg.getMessageText());
        txtMessageTimeStamp.setText(String.format("%s", msg.getMessageTimeStamp()));
        return myView;
    }
}
/*
"MessageName":"Warning",
"MessageText":"Destination may be over capacity for product: PULSES (KARMIN)",
"MessageTimeStamp":"2014-05-29 16:24:12"*/
