package com.proper.data.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.proper.bin2bin.R;
import com.proper.data.BinMoveObject;

import java.util.List;

/**
 * Created by Lebel on 02/06/2014.
 */
public class MoveActionAdapter extends BaseAdapter {
    private Context context;
    private List<BinMoveObject> theseActions;

    public MoveActionAdapter(Context context,  List<BinMoveObject> actions) {
        this.context = context;
        this.theseActions = actions;
    }

    @Override
    public int getCount() {
        return theseActions.size();
    }

    @Override
    public BinMoveObject getItem(int i) {
        return theseActions.get(i);
    }

    @Override
    public long getItemId(int i) {
        return theseActions.get(i).getProductId();
    }

    @Override
    public View getView(int pos, View view, ViewGroup viewGroup) {
        View myView = view;
        if (myView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            myView = inflater.inflate(R.layout.list_binaction_item, viewGroup, false);
        }
        BinMoveObject action = theseActions.get(pos);
        //assign control values and display
        TextView lblAction = (TextView) myView.findViewById(R.id.lblAction);
        TextView txtAction = (TextView) myView.findViewById(R.id.txtvAction);
        TextView lblProductId = (TextView) myView.findViewById(R.id.lblProductId);
        TextView txtProductId = (TextView) myView.findViewById(R.id.txtvProductId);
        TextView lblSupplierCat = (TextView) myView.findViewById(R.id.lblSupplierCat);
        TextView txtSupplierCat = (TextView) myView.findViewById(R.id.txtvSupplierCat);
        TextView lblEAN = (TextView) myView.findViewById(R.id.lblEAN);
        TextView txtEAN = (TextView) myView.findViewById(R.id.txtvEAN);
        TextView lblQty = (TextView) myView.findViewById(R.id.lblQty);
        TextView txtQty = (TextView) myView.findViewById(R.id.txtvQty);

        lblAction.setText("Action:  "); lblProductId.setText("ProductID:    ");
        lblSupplierCat.setText("Catalog");  lblEAN.setText("EAN:    ");
        lblQty.setText("Quantity:    ");

        txtAction.setText(action.getAction());  txtProductId.setText(String.format("%s", action.getProductId()));
        txtSupplierCat.setText(action.getSupplierCat());    txtEAN.setText(action.getEAN());
        txtQty.setText(String.format("%s", action.getQty()));
        return myView;
    }
}
/*
"Action":"ItemMoved",
        "ProductId":"2980749",
        "SupplierCat":"POR5479002PMI",
        "EAN":"0731454790024",
        "Qty":"1"*/
