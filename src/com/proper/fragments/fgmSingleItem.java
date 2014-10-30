package com.proper.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;
import com.proper.bin2bin.ActSingleDetails;
import com.proper.data.BarcodeResponse;
import com.proper.data.ProductResponse;
import com.proper.data.adapters.ProductResponseAdapter;
import com.proper.data.core.IOnItemSelectionChanged;

import java.util.List;

/**
 * Created by Lebel on 15/04/2014.
 */
public class fgmSingleItem extends ListFragment {
    private BarcodeResponse response = null;
    private List<ProductResponse> currentProducts;
    private ProductResponse selectedProduct;

    public ProductResponse getSelectedProduct() {
        return selectedProduct;
    }

    public void setSelectedProduct(ProductResponse selectedProduct) {
        this.selectedProduct = selectedProduct;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //load data here
        ActSingleDetails activity = (ActSingleDetails) getActivity();
//        LayoutInflater inflater = (LayoutInflater) activity
//                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        response = activity.getResponse();
        currentProducts = response.getProducts();
        //Get Overall Quantity
//        int totalQty = 0;
//        for (ProductResponse resp : response.getProducts()) {
//            for (Bin bin : resp.getBins()) {
//                totalQty += bin.getQty();
//            }
//        }

        getListView().setCacheColorHint(Color.TRANSPARENT);
        ProductResponseAdapter itemAdapter = new ProductResponseAdapter(getActivity(), currentProducts);
        //ProductResponseAdapter itemAdapter = new ProductResponseAdapter(getActivity(), currentProducts, totalQty);
        setListAdapter(itemAdapter);

        //set selected item to the first in list by default
        if (!currentProducts.isEmpty()) {
            setSelectedProduct(currentProducts.get(0));
            IOnItemSelectionChanged listner = (IOnItemSelectionChanged) getActivity();
            listner.onItemSelectionChanged(0);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        setSelectedProduct(currentProducts.get(position));
        IOnItemSelectionChanged listner = (IOnItemSelectionChanged) getActivity();
        listner.onItemSelectionChanged(position);   //pass position index to the main activity through interface
        getListView().setItemChecked(position, true);
        v.setSelected(true);    //Helps towards highlighting row    ***************
    }
}
