package com.ntippa.android.myApp3;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import com.ntippa.android.myApp3.data.AlexandriaContract;
import com.ntippa.android.myApp3.services.BookService;
import com.ntippa.android.myApp3.services.DownloadImage;


public class AddBook extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "INTENT_TO_SCAN_ACTIVITY";
    private EditText ean;
    private final int LOADER_ID = 1;
    private View rootView;

    private final String EAN_CONTENT="eanContent";
    private final String AUTO_FOCUS_STATE="autoFocus";
    private final String USE_FLASH_STATE="useFlash";

    private static final String SCAN_FORMAT = "scanFormat";
    private static final String SCAN_CONTENTS = "scanContents";

    private String mBarcode;
    private String mScanFormat = "Format:";
    private String mScanContents = "Contents:";

    ////private CompoundButton autoFocus;
    private CompoundButton useFlash;
    private TextView statusMessage;
    private TextView barcodeValue;

    private String mBookTitle, mBookSubtitle,mAuthors,mCategories;
    private final String BOOK_TITLE = "title";
    private final String BOOK_SUBTITLE = "subtitle";
    private final String AUTHORS = "authors";
    private final String CATEGORIES = "categories";
    private final String STATUS_MESSAGE = "status";

    private Button scan_button,save_button,delete_button;
    private TextView titleView,subTtitleView,authorsView,categoriesView;

    private boolean IS_NETWORK_AVAILABLE = true;

    private static final int RC_BARCODE_CAPTURE = 9001;



    public AddBook(){
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        if(!Utility.isNetworkAvailable(getActivity()))// todo: check if this spot is ok to check network status.onResume??
        {
            IS_NETWORK_AVAILABLE = false;
            Log.d(TAG,"no network");
            Toast.makeText(getActivity(),"No Network available",Toast.LENGTH_SHORT).show();
        }else
            Log.d(TAG,"Network available");

        setRetainInstance(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        if(mBarcode != null && mBarcode.length() != 0){
            Log.d(TAG,"saving barcode to instance state");
            outState.putString(EAN_CONTENT,mBarcode);
        }
       //todo: i dont check if book details exist or not.just dump in outstate.If data exists, it will be displayed
        //todo:if(mBookTitle.length() != 0)
        outState.putString(BOOK_TITLE,mBookTitle);
        outState.putString(BOOK_SUBTITLE, mBookSubtitle);
        outState.putString(AUTHORS, mAuthors);
        outState.putString(CATEGORIES, mCategories);


    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_add_book, container, false);
        ean = (EditText) rootView.findViewById(R.id.ean);
        titleView = (TextView) rootView.findViewById(R.id.bookTitle);
        subTtitleView = (TextView) rootView.findViewById(R.id.bookSubTitle);
        authorsView = (TextView) rootView.findViewById(R.id.authors);
        categoriesView = (TextView) rootView.findViewById(R.id.categories);

        scan_button = (Button) rootView.findViewById(R.id.scan_button);
        save_button = (Button) rootView.findViewById(R.id.save_button);
        delete_button = (Button) rootView.findViewById(R.id.delete_button);


        statusMessage = (TextView)rootView.findViewById(R.id.status_message);

       // autoFocus = (CompoundButton) rootView.findViewById(R.id.auto_focus);
       // useFlash = (CompoundButton) rootView.findViewById(R.id.use_flash);


        ean.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //no need
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //no need
            }

            @Override
            public void afterTextChanged(Editable s) {
                mBarcode = s.toString();
                //String dummy = "9781936277285";
                Log.d(TAG, "barcode received:" + ean);
                //catch isbn10 numbers
                if (mBarcode.length() == 10 && !mBarcode.startsWith("978")) {
                    mBarcode = "978" + mBarcode;
                }
                if ( mBarcode.length() < 13){
                    Log.d(TAG, "barcode less than 13 digits");
                    if(mBarcode.length() > 0) {//todo:DONE:if there is any input and it isnt valid, only then its invalid barcode
                        clearFields();
//                    ean.setHint(R.string.input_hint);
                        //todo: capture key events to accurately display invalid barcode message.
                       // Toast.makeText(getActivity(), getString(R.string.invalid_barcode), Toast.LENGTH_LONG).show();

                    }
                    return;
                }

//                if(!Utility.isNetworkAvailable(getActivity())){
//                    Log.d(TAG,"No network available");//todo: No network available.
//                    showScanDialog(getResources().getString(R.string.no_network));
//                }else {
                Log.d(TAG, "Network good");
                Toast.makeText(getActivity(), getString(R.string.status), Toast.LENGTH_LONG).show();
                //todo: barcode OK && Network available, trigger bookservice
                //Once we have an ISBN, start a book intent
                Intent bookIntent = new Intent(getActivity(), BookService.class);

                bookIntent.putExtra(BookService.EAN, mBarcode);
                // bookIntent.putExtra(BookService.EAN, dummy);

                bookIntent.setAction(BookService.FETCH_BOOK);
                Log.d(TAG, "Starting Book Service");
                getActivity().startService(bookIntent);


                //once we receive barcode, we disable SCAN button till user makes a choice
                scan_button.setEnabled(false);
                Log.d(TAG, "disbale SCAN button");

                Log.d(TAG, "save/delete visible");
                save_button.setVisibility(View.VISIBLE);
                delete_button.setVisibility(View.VISIBLE);

                AddBook.this.restartLoader();
                // }
            }
        });

        rootView.findViewById(R.id.scan_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This is the callback method that the system will invoke when your button is
                // clicked. You might do this by launching another app or by including the
                //functionality directly in this app.
                // Hint: Use a Try/Catch block to handle the Intent dispatch gracefully, if you
                // are using an external app.
                //when you're done, remove the toast below.
                Context context = getActivity();
                CharSequence text = "This button should let you scan a book for its barcode!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

                // launch barcode activity.
                Intent intent = new Intent(context, BarcodeCaptureActivity.class);
                intent.putExtra(BarcodeCaptureActivity.AutoFocus, false);//todo:have to figure out these settings
                intent.putExtra(BarcodeCaptureActivity.UseFlash, false);//todo:
//                intent.putExtra(BarcodeCaptureActivity.AutoFocus, autoFocus.isChecked());
//                intent.putExtra(BarcodeCaptureActivity.UseFlash, useFlash.isChecked());
                startActivityForResult(intent, RC_BARCODE_CAPTURE);

            }
        });

        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ean.setText("");//todo: what do we do here
                //statusMessage.setText("Book saved!!");
                Toast.makeText(getActivity(),getString(R.string.book_saved_status),Toast.LENGTH_LONG).show();
                scan_button.setEnabled(true);
                titleView.setText("");//todo:
                getView().findViewById(R.id.save_button).setEnabled(false);//todo: once clicked, disbale it. when NullPointerException??


            }

        });

        delete_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, mBarcode);
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);
                //todo: ean.setText("");
               // statusMessage.setText("Book deleted");
                Toast.makeText(getActivity(),getString(R.string.book_deleted),Toast.LENGTH_LONG).show();
                scan_button.setEnabled(true);
                getView().findViewById(R.id.delete_button).setEnabled(false);//todo: disable button once clicked.NullPOinter??

            }
        });

        if(savedInstanceState!=null){
            ean.setText(savedInstanceState.getString(EAN_CONTENT));
            ean.setHint("");
            titleView.setText(savedInstanceState.getString(BOOK_TITLE));//todo: book data after orientation changes
            subTtitleView.setText(savedInstanceState.getString(BOOK_SUBTITLE));
            authorsView.setText(savedInstanceState.getString(AUTHORS));
            categoriesView.setText(savedInstanceState.getString(CATEGORIES));
           // statusMessage.setText(savedInstanceState.getString(STATUS_MESSAGE));

        }
        save_button.setVisibility(View.INVISIBLE);// todo:INVISIBLE untill data available for saving
        delete_button.setVisibility(View.INVISIBLE);
        return rootView;
    }

    @Override
    public void onResume() {
        Log.d(TAG,"onResume");
        super.onResume();

        if(!Utility.isNetworkAvailable(getActivity()))// todo: check if this spot is ok to check network status.??
        {
            IS_NETWORK_AVAILABLE = false;
            Log.d(TAG,"no network");
            Toast.makeText(getActivity(),"No Network available",Toast.LENGTH_SHORT).show();
        }else {
            Log.d(TAG, "Network available");
        }
        scan_button.setEnabled(true);//todo: on orientation change, scan button always enabled??
        statusMessage.setText(getString(R.string.intial_status));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG,"onActivityResult in Fragment");
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {

                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                   // statusMessage.setText(R.string.barcode_success);
                    Toast.makeText(getActivity(),getResources().getString(R.string.barcode_success),Toast.LENGTH_LONG);
                    mBarcode = barcode.displayValue;
                    Log.d(TAG, "BARCODE value:" + ean);
                    Log.d(TAG, "displayValue length" + ean.length());

                    Log.d(TAG, "Setting barcode to Text field");
                    Toast.makeText(getActivity(), getString(R.string.barcode_success), Toast.LENGTH_LONG).show();

                    if (IS_NETWORK_AVAILABLE){

                        if (mBarcode.length() == 10 && !mBarcode.startsWith("978")) {
                            mBarcode = "978" + mBarcode;
                        }
                        if (mBarcode.length() < 13 && mBarcode.length() > 1) {
                            clearFields();
                            Log.d(TAG, "barcode less than 13 digits");
                            Toast.makeText(getActivity(), getString(R.string.invalid_barcode), Toast.LENGTH_LONG).show();
                           // statusMessage.setText("Invalid barcode");
                            return;
                        }

                      // if()
                        Toast.makeText(getActivity(), getString(R.string.barcode) + mBarcode, Toast.LENGTH_LONG).show();
                       // statusMessage.setText("Barcode:" + mBarcode);
//                if(!Utility.isNetworkAvailable(getActivity())){
//                    Log.d(TAG,"No network available");//todo: No network available.
//                    showScanDialog(getResources().getString(R.string.no_network));
//                }else {
                    Log.d(TAG, "Network good");
                    Toast.makeText(getActivity(), getString(R.string.status), Toast.LENGTH_LONG).show();
                    //todo: barcode OK && Network available, trigger bookservice
                    //Once we have an ISBN, start a book intent
                    Intent bookIntent = new Intent(getActivity(), BookService.class);

                    bookIntent.putExtra(BookService.EAN, mBarcode);
                    // bookIntent.putExtra(BookService.EAN, dummy);

                    bookIntent.setAction(BookService.FETCH_BOOK);
                    Log.d(TAG, "Starting Book Service");
                    getActivity().startService(bookIntent);


                    //once we receive barcode, we disable SCAN button till user makes a choice
                    scan_button.setEnabled(false);
                    Log.d(TAG, "disbale SCAN button");

                    Log.d(TAG, "save/delete visible");
                    save_button.setVisibility(View.VISIBLE);
                    delete_button.setVisibility(View.VISIBLE);

                    AddBook.this.restartLoader();
                    // ean.setText(barcode.displayValue);
                }
                    else
                        showScanDialog("No Network available");


                } else {
                    Toast.makeText(getActivity(), getString(R.string.intent_data_null), Toast.LENGTH_LONG).show();
                    //statusMessage.setText(R.string.barcode_failure);
                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            } else {
                Log.d(TAG, "Not found status:::" + CommonStatusCodes.getStatusCodeString(resultCode));
                Toast.makeText(getActivity(), getString(R.string.no_books), Toast.LENGTH_LONG).show();
               // statusMessage.setText(String.format(getString(R.string.barcode_error),
                       // CommonStatusCodes.getStatusCodeString(resultCode)));

            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
        scan_button.setEnabled(true);//todo:
    }

    public void showScanDialog(String status) {
        Log.d(TAG, "showing scan status");
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = ScanDialogFragment.newInstance(status);
        // dialog.setA
        dialog.show(getActivity().getSupportFragmentManager(), getString(R.string.dialog_scan_status));
    }

    private void restartLoader(){
        Log.d(TAG,"restar loader");
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG,"onCreateLoader");

        if(mBarcode.length() == 0){
            return null;
        }

        if(mBarcode.length()==10 && !mBarcode.startsWith("978")){
            mBarcode="978"+mBarcode;
        }

        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(mBarcode)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        Log.d(TAG,"onLoadFinished");
        if (!data.moveToFirst()) {//no matching data in db
//            if(!Utility.isNetworkAvailable(getActivity())){
//                Log.d(TAG,"No network connectivity");
//                showScanDialog(getResources().getString(R.string.no_network));// todo:no network.Y check here??
//                return;
//            }
            Log.d(TAG, "no matching data in db");
          //  statusMessage.setText("No book found");//todo: barcode scanned good, book service call good but no book data .
            save_button.setEnabled(false);         //todo: means no book found."No book message" is braodcast and activity is receiving it.
            delete_button.setEnabled(false); //todo: should fragment also receive it, if so where?? onCreate?? or setArguemnts from activity?? ?How
            return;
        }

        mBookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        ((TextView) rootView.findViewById(R.id.bookTitle)).setText(mBookTitle);

        mBookSubtitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText(mBookSubtitle);

        mAuthors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        String[] authorsArr = mAuthors.split(",");
        ((TextView) rootView.findViewById(R.id.authors)).setLines(authorsArr.length);
        ((TextView) rootView.findViewById(R.id.authors)).setText(mAuthors.replace(",","\n"));
        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if(Patterns.WEB_URL.matcher(imgUrl).matches()){
            new DownloadImage((ImageView) rootView.findViewById(R.id.bookCover)).execute(imgUrl);
            rootView.findViewById(R.id.bookCover).setVisibility(View.VISIBLE);
        }

        mCategories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        ((TextView) rootView.findViewById(R.id.categories)).setText(mCategories);

        rootView.findViewById(R.id.save_button).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.delete_button).setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    private void clearFields(){

        ((TextView) rootView.findViewById(R.id.bookTitle)).setText("");
        ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText("");
        ((TextView) rootView.findViewById(R.id.authors)).setText("");
        ((TextView) rootView.findViewById(R.id.categories)).setText("");
        rootView.findViewById(R.id.bookCover).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.save_button).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.delete_button).setVisibility(View.INVISIBLE);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle(R.string.scan);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG,"onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
    }



}
