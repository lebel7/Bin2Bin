package com.proper.data.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.proper.bin2bin.R;
import com.proper.data.ProductBinResponse;
import com.proper.data.helpers.LetterSpacingTextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Lebel on 25/07/2014.
 */
public class ProductBinAdapterOptimized extends BaseAdapter {
    private static final String ApplicationID = "Bin2Bin";
    private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private java.util.Date utilDate = java.util.Calendar.getInstance().getTime();
    private java.sql.Timestamp today = null;
    private String deviceIMEI;
    private Context kontext;
    //protected LayoutInflater inflater;
    protected List<ProductBinResponse> products;
    private static final int MSG_BCODE_STARTING = 22;
    private static final int MSG_DONE = 11;
    //private Handler codeImageHandler = null;
    protected ProgressDialog bcDialog;
    private boolean hasBcRan = false;
    private int bcRunCount = 0;
    private ImageView barcodePic;
    private ImageView albumPic;
    private Bitmap mBitmap = null;
    private LetterSpacingTextView txtBarcode;

    public ProductBinAdapterOptimized(Context context, List<ProductBinResponse> products, String deviceIMEI) {
        this.kontext = context;
        this.products = products;
        this.deviceIMEI = deviceIMEI;
    }

    @Override
    public int getCount() {
        return products.size();
    }

    @Override
    public ProductBinResponse getItem(int i) {
        return products.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int pos, View view, ViewGroup viewGroup) {


        View myView = view;
        if (myView == null) {
            LayoutInflater inflater = (LayoutInflater) kontext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            myView = inflater.inflate(R.layout.list_qryview_item1, viewGroup, false);
        }

        ProductBinResponse prod = products.get(pos);
        albumPic = (ImageView) myView.findViewById(R.id.imgAlbum);
        TextView txtArtist = (TextView) myView.findViewById(R.id.txtv_Artist);
        TextView txtTitle = (TextView) myView.findViewById(R.id.txtv_Title);
        //barcodePic = (ImageView) myView.findViewById(R.id.imgBarcode);
        txtBarcode = (LetterSpacingTextView) myView.findViewById(R.id.lblBarcode);
        TextView lblFormat = (TextView) myView.findViewById(R.id.lblFormat);
        TextView txtFormat = (TextView) myView.findViewById(R.id.txtvFormat);
        TextView lblSupplierCat = (TextView) myView.findViewById(R.id.lblSupplierCat);
        TextView txtSupplierCat = (TextView) myView.findViewById(R.id.txtvSupplierCat);
        TextView lblQuantity = (TextView) myView.findViewById(R.id.lblQtyInBin);
        TextView txtQuantity = (TextView) myView.findViewById(R.id.txtvQtyInBin);

        txtArtist.setText(prod.getArtist());
        txtTitle.setText(prod.getTitle());
        txtBarcode.setText(prod.getBarcode());
        txtBarcode.setLetterSpacing(21);
        lblFormat.setText("Format:  ");
        txtFormat.setText(prod.getFormat());
        lblSupplierCat.setText("Supplier Catalog:");
        txtSupplierCat.setText(prod.getSupplierCat());
        lblQuantity.setText("Qty In Bin");
        txtQuantity.setText(String.format("%s", prod.getQtyInBin()));

        // retrieve album pictures here, if url is present
        //barcodePic.setImageBitmap(generateBarCode(prod.getBarcode()));
        //barcodePic.setScaleType(ImageView.ScaleType.FIT_XY);
        return myView;
    }

//    public Bitmap generateBarCode(String data) {
//
//        switch (data.length()) {
//            case 12:    //UPC-A
//                com.google.zxing.oned.UPCAWriter upc = new com.google.zxing.oned.UPCAWriter();
//                try {
//                    BitMatrix bm = upc.encode(data, BarcodeFormat.UPC_A, 360, 108);
//                    mBitmap = Bitmap.createBitmap(360, 108, Bitmap.Config.ARGB_8888);
//                    for (int i = 0; i < 360; i++) {
//                        for (int j = 0; j < 108; j++) {
//
//                            mBitmap.setPixel(i, j, bm.get(i, j) ? Color.BLACK : Color.WHITE);
//                        }
//                    }
//                } catch (WriterException e) {
//                    e.printStackTrace();
//                }
//                break;
//            case 8:     //EAN-8
//                com.google.zxing.oned.EAN8Writer ean8 = new com.google.zxing.oned.EAN8Writer();
//                try {
//                    BitMatrix bm = ean8.encode(data, BarcodeFormat.EAN_8, 360, 108);
//                    mBitmap = Bitmap.createBitmap(360, 108, Bitmap.Config.ARGB_8888);
//                    for (int i = 0; i < 360; i++) {
//                        for (int j = 0; j < 108; j++) {
//
//                            mBitmap.setPixel(i, j, bm.get(i, j) ? Color.BLACK : Color.WHITE);
//                        }
//                    }
//                } catch (WriterException e) {
//                    e.printStackTrace();
//                }
//                break;
//            case 14:    //UPC-14
//                //BitMatrix bm = c9.encode(data,BarcodeFormat.CODE_128,380, 168);
//                com.google.zxing.oned.ITFWriter itf = new com.google.zxing.oned.ITFWriter();
//                try {
//                    //BitMatrix bm = c9.encode(data,BarcodeFormat.CODE_128,380, 168);
//                    BitMatrix bm = itf.encode(data, BarcodeFormat.ITF, 360, 108);
//                    mBitmap = Bitmap.createBitmap(360, 108, Bitmap.Config.ARGB_8888);
//                    for (int i = 0; i < 360; i++) {
//                        for (int j = 0; j < 108; j++) {
//
//                            mBitmap.setPixel(i, j, bm.get(i, j) ? Color.BLACK : Color.WHITE);
//                        }
//                    }
//                } catch (WriterException e) {
//                    e.printStackTrace();
//                }
//                break;
//            case 13:    //EAN-13
//                com.google.zxing.oned.EAN13Writer ean13 = new com.google.zxing.oned.EAN13Writer();
//                try {
//                    BitMatrix bm = ean13.encode(data, BarcodeFormat.EAN_13, 360, 108);
//                    mBitmap = Bitmap.createBitmap(360, 108, Bitmap.Config.ARGB_8888);
//                    for (int i = 0; i < 360; i++) {
//                        for (int j = 0; j < 108; j++) {
//
//                            mBitmap.setPixel(i, j, bm.get(i, j) ? Color.BLACK : Color.WHITE);
//                        }
//                    }
//                } catch (WriterException e) {
//                    e.printStackTrace();
//                }
//                break;
//            default:    //Error - throw dead kittens
//                LogHelper logger = new LogHelper();
//                String iMsg = "The Response object return null due to msg queue not recognising your improper request.";
//                today = new java.sql.Timestamp(utilDate.getTime());
//                LogEntry log = new LogEntry(1L, ApplicationID, "QueryView - generateBarcode - Line:306", deviceIMEI, RuntimeException.class.getSimpleName(), iMsg, today);
//                logger.Log(log);
//                WriterException ex = new WriterException(iMsg);
//                ex.printStackTrace();
//                throw new RuntimeException(ex.getMessage());
//        }
//        return mBitmap;
//    }

//    public Drawable getAlbumPicture(String url) {
//        try {
//            InputStream is = (InputStream) new URL(url).getContent();
//            Drawable d = Drawable.createFromStream(is, "src name");
//            return d;
//        } catch (Exception e) {
//            return null;
//        }
//    }
}
