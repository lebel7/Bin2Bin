package com.proper.bin2bin;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.widget.Button;
import android.widget.ListView;
import com.proper.MessageQueue.Message;
import com.proper.data.*;
import com.proper.data.adapters.BinListAdapter;
import com.proper.data.core.IOnDetailSelectionChanged;
import com.proper.data.core.IOnItemSelectionChanged;
import com.proper.fragments.fgmSingleItem;
import com.proper.fragments.fgmSingleItemDetails;

/**
 * Created by Lebel on 10/04/2014.
 */
public class ActSingleDetails extends FragmentActivity implements IOnItemSelectionChanged, IOnDetailSelectionChanged {
    private int NAV_INSTRUCTION = 0;
    private java.util.Date utilDate = java.util.Calendar.getInstance().getTime();
    private java.sql.Timestamp today = null;
    private int transactionNumber = 0;
    private Button btnContinue = null;
    private Message thisMessage = null;
    private String deviceIMEI = "";
    private String ApplicationID = "";
    private String backPressedParameter = "";
    private static final String paramTaskCompleted = "TRANSACTION_COMPLETED";
    private static final String paramTaskIncomplete = "TRANSACTION_INCOMPLETE";
    private BarcodeResponse response;
    private ProductResponse SelectedProductResponse;
    private Bin SelectedBin;
    private int Instruction;
    private BarcodeResponse barcodeResponse;
    private BarcodeBinResponse barcodeBinResponse;
    private BinResponse binResponse;

    public int getInstruction() {
        return Instruction;
    }

    public void setInstruction(int instruction) {
        Instruction = instruction;
    }

    public BarcodeResponse getResponse() {
        return response;
    }

    public void setResponse(BarcodeResponse response) {
        this.response = response;
    }

    public ProductResponse getSelectedProductResponse() {
        return SelectedProductResponse;
    }

    public void setSelectedProductResponse(ProductResponse selectedProductResponse) {
        SelectedProductResponse = selectedProductResponse;
    }

    public Bin getSelectedBin() {
        return SelectedBin;
    }

    public void setSelectedBin(Bin selectedBin) {
        SelectedBin = selectedBin;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Identify where call came from and NAV_instruction
        Bundle extras = getIntent().getExtras();
        NAV_INSTRUCTION = extras.getInt("ACTION_EXTRA"); //  Gives us the inclination of what is going on
        setInstruction(NAV_INSTRUCTION);

        switch (NAV_INSTRUCTION) {
            case R.integer.ACTION_BINQUERY:
                //do
                break;
            case R.integer.ACTION_BARCODEQUERY:
                barcodeResponse = (BarcodeResponse) extras.getSerializable("MESSAGE_RESPONSE_EXTRA");
                setResponse(barcodeResponse);
                this.setTitle(barcodeResponse.getProducts().get(0).getBins() != null ?
                       barcodeResponse.getProducts().get(0).getTitle() : barcodeResponse.getProducts().get(1).getTitle());
                break;
            case R.integer.ACTION_BARCODE_BINQUERY:
                //do
                break;
            case R.integer.ACTION_BINMOVE:
                //do
                break;
            case R.integer.ACTION_SINGLEMOVE:
                barcodeResponse = (BarcodeResponse) extras.getSerializable("MESSAGE_RESPONSE_EXTRA");
                setResponse(barcodeResponse);
                this.setTitle(barcodeResponse.getProducts().get(0).getBins() != null ?
                        barcodeResponse.getProducts().get(0).getFullTitle() : barcodeResponse.getProducts().get(1).getFullTitle());
                break;
        }
        setContentView(R.layout.lyt_singledetailsmain);
    }

    @Override
    public void onDetailselectionChenged(int detailIndex) {
        FragmentManager fm = getSupportFragmentManager();
        fgmSingleItemDetails detailsFragment = (fgmSingleItemDetails) fm.findFragmentById(R.id.ItemDetailsFragment);
        setSelectedBin(detailsFragment.getSelectedBin());   //get currently selected Bin in fragment
    }

    @Override
    public void onItemSelectionChanged(int itemIndex) {
        FragmentManager fm = getSupportFragmentManager();
        fgmSingleItem itemFragment = (fgmSingleItem) fm.findFragmentById(R.id.ItemsFragment);
        fgmSingleItemDetails detailsFragment = (fgmSingleItemDetails) fm.findFragmentById(R.id.ItemDetailsFragment);
        setSelectedProductResponse(itemFragment.getSelectedProduct());  //get currently selected product in fragment
        //Load the new Bins & notify the UI that something has changed - notifyDataSetChanged()
        if (detailsFragment.getDetailsInflater() != null) {
            ListView list = (ListView) detailsFragment.getView().findViewById(R.id.lvDetails);
            BinListAdapter detailsAdapter = new BinListAdapter(this, detailsFragment.getDetailsInflater(), getSelectedProductResponse().getBins());
            list.setAdapter(detailsAdapter);
            detailsAdapter.notifyDataSetChanged();
        }
    }
}