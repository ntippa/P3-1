package com.ntippa.android.myApp3;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ntippa.android.myApp3.data.AlexandriaContract;
import com.ntippa.android.myApp3.services.BookService;
import com.ntippa.android.myApp3.services.DownloadImage;

/*
*modified: Nalini @udacity project
* Fragment to display Book details that were fetched vie Google Book API
*/

public class BookDetail extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EAN_KEY = "EAN";
    private static final String TAG = BookDetail.class.getSimpleName() ;
    private final int LOADER_ID = 10;
    private View rootView;
    private String ean;
    private String bookTitle,subTitle,description,authors,categories;
    private ShareActionProvider shareActionProvider;
    TextView titleView,subtitleView,authorsView,categoriesView,descView;
    private final String BOOK_TITLE = "title";
    private final String BOOK_SUBTITLE = "subtitle";
    private final String AUTHORS = "authors";
    private final String CATEGORIES = "categories";
    private final String DESCRIPTION = "description";

    public BookDetail(){
    }

    /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
//    public static BookDetail newInstance(int index) {
//        BookDetail f = new BookDetail();
//
//        // Supply index input as an argument.
//        Bundle args = new Bundle();
//        args.putInt("index", index);
//        f.setArguments(args);
//
//        return f;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
       // setRetainInstance(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSavedInstanceState");
        super.onSaveInstanceState(outState);

        outState.putString(BOOK_TITLE, bookTitle);//todo: saving data
        outState.putString(BOOK_SUBTITLE,subTitle);
        outState.putString(AUTHORS,authors);
        outState.putString(CATEGORIES,categories);
        outState.putString(DESCRIPTION,description);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            ean = arguments.getString(BookDetail.EAN_KEY);
            getLoaderManager().restartLoader(LOADER_ID, null, this);
        }

        rootView = inflater.inflate(R.layout.fragment_full_book, container, false);

        titleView = (TextView) rootView.findViewById(R.id.fullBookTitle);
        subtitleView = (TextView) rootView.findViewById(R.id.fullBookSubTitle);
        descView = (TextView) rootView.findViewById(R.id.fullBookDesc);
        authorsView = (TextView) rootView.findViewById(R.id.authors);
        categoriesView = (TextView) rootView.findViewById(R.id.categories);
        rootView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, ean);
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        if(savedInstanceState != null){

            titleView.setText(savedInstanceState.getString(BOOK_TITLE));
            subtitleView.setText(savedInstanceState.getString(BOOK_SUBTITLE));

            String authors = savedInstanceState.getString(AUTHORS);
            String[] authorsArr = authors.split(",");
            authorsView.setLines(authorsArr.length);
            authorsView.setText(authors.replace(",", "\n"));

            descView.setText(savedInstanceState.getString(DESCRIPTION));
            categoriesView.setText(savedInstanceState.getString(CATEGORIES));
        }
        return rootView;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.book_detail, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

//        //Attach an intent to this share action provider.
//        if(shareActionProvider != null){
//            shareActionProvider.setShareIntent(createShareBookInfo());
//        }else{
//            Log.d(TAG,"share action provider is null");
//        }
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(ean)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        titleView.setText(bookTitle);

//        Intent shareIntent = new Intent(Intent.ACTION_SEND);
//        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
//        shareIntent.setType("text/plain");
//        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text) + bookTitle);
//        shareActionProvider.setShareIntent(shareIntent);
        //Attach an intent to this share action provider.
        if(shareActionProvider != null){
            Log.d(TAG,"shareActionProvider not null");//todo:
            shareActionProvider.setShareIntent(createShareBookInfo());
        }else{
            Log.d(TAG,"share action provider is null");
        }


        subTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        subtitleView.setText(subTitle);

        description = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.DESC));
        descView.setText(description);

        authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        String[] authorsArr = authors.split(",");
        authorsView.setLines(authorsArr.length);
        authorsView.setText(authors.replace(",", "\n"));
        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if(Patterns.WEB_URL.matcher(imgUrl).matches()){
            new DownloadImage((ImageView) rootView.findViewById(R.id.fullBookCover)).execute(imgUrl);
            rootView.findViewById(R.id.fullBookCover).setVisibility(View.VISIBLE);
        }

        categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        categoriesView.setText(categories);

        if(rootView.findViewById(R.id.right_container)!=null){
           // rootView.findViewById(R.id.backButton).setVisibility(View.INVISIBLE);//todo: remove back button.
        }

    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

//    @Override
//    public void onPause() {
//        Log.d(TAG, "onPause");//todo: commenting this code
//        super.onDestroyView();
////        if(MainActivity.IS_TABLET && rootView.findViewById(R.id.right_container)==null){
////            getActivity().getSupportFragmentManager().popBackStack();
////        }
//
//    }


    private Intent createShareBookInfo(){
        Log.d(TAG,"createShareBookInfo");
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        //shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text) + bookTitle);
        //shareActionProvider.setShareIntent(shareIntent);
        return shareIntent;
    }
}