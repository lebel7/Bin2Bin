package com.proper.data.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.proper.bin2bin.R;
import com.proper.data.BarcodeBinResponse;
import com.proper.data.ProductBinResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lebel on 30/05/2014.
 */
public class BarcodeBinResponseAdapater extends BaseAdapter {
    private Context context;
    private List<ProductBinResponse> thisResponse = new ArrayList<ProductBinResponse>();

    public BarcodeBinResponseAdapater(Context context, BarcodeBinResponse barcodeBinResponse) {
        this.context = context;
        thisResponse = barcodeBinResponse.getProducts();
    }

    @Override
    public int getCount() {
        return thisResponse.size();
    }

    @Override
    public ProductBinResponse getItem(int i) {
        return thisResponse.get(i);
    }

    @Override
    public long getItemId(int i) {
        return thisResponse.get(i).getProductId();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View thisView = view;
        if (thisView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            thisView = inflater.inflate(R.layout.list_bin_item1, viewGroup, false);
        }
        //TODO - Do stuff - Access data here

        return thisView;
    }
}
