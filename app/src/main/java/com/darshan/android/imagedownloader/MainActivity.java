package com.darshan.android.imagedownloader;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.darshan.android.imagedownloader.adapters.ImageListAdapter;
import com.darshan.android.imagedownloader.retrofit.Image;
import com.darshan.android.imagedownloader.retrofit.PicusumApiEndpoint;
import com.darshan.android.imagedownloader.utils.DownloaderUtils;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    //consts
    public static final String BASE_URL = "https://picsum.photos/";
    private static final int REQUEST_STORAGE_PERMISSION = 1234;

    //widgets
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;


    private ImageListAdapter mRecyclerAdapter;
    private ArrayList<Image> mImageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = findViewById(R.id.imageList_RV);
        mProgressBar = findViewById(R.id.progressBar);

        //making progress bar visible at start of the activity
        mProgressBar.setVisibility(View.VISIBLE);
        mImageList = new ArrayList<>();

        initRecyclerList();
        checkPermissions();

    }


    private void initRecyclerList() {
        //At start no information on images is available(This info later get loaded from web).
        mRecyclerAdapter = new ImageListAdapter(this, mImageList);

        //Layout manger for list view
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mRecyclerAdapter);

        //Adding Divider between Recycler items
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                this,
                linearLayoutManager.getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);

    }


    /**
     * Retrieves JSON data from URL:https://picsum.photos/list
     * then load the appropriate image related data in RecyclerView
     */
    private void loadImagesFromWeb() {
        //initialize Retrofit
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        PicusumApiEndpoint apiEndpoint = retrofit.create(PicusumApiEndpoint.class);

        Call<ArrayList<Image>> call = apiEndpoint.getImages();

        //Trying to get image list from the URL
        call.enqueue(new Callback<ArrayList<Image>>() {
            @Override
            public void onResponse(Call<ArrayList<Image>> call, Response<ArrayList<Image>> response) {
                //HTTP Response code
                int responseCode = response.code();
                Log.d(TAG, "onResponse: code " + responseCode);

                //Retrieve images info from JSON response
                ArrayList<Image> imageList = response.body();

                if(imageList != null && imageList.size() >= 20) {
                    //Get 20 images from the loaded imageList
                    int ranNum = DownloaderUtils.getRandomNumBetween(imageList.size() - 20, 0);
                    Log.d(TAG, "onResponse: random " + ranNum);
                    for (int i = ranNum; i < ranNum + 20; i++) {
                        mImageList.add(imageList.get(i));
                    }

                    mProgressBar.setVisibility(View.GONE);
                    //Displaying 20 images in Recycler view
                    mRecyclerAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Image>> call, Throwable t) {
                Log.e(TAG, "onFailure: Unable to retrieve images" );
                mProgressBar.setVisibility(View.GONE);
                Toast.makeText(
                        MainActivity.this,
                        "Some error occurs, try again later!",
                        Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });

    }


    private void checkPermissions() {
        // Check for the external storage permission
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // If you do not have permission, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);
        } else {
            //Permission is granted proceed
            loadImagesFromWeb();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // Called when you request permission to read and write to external storage
        switch (requestCode) {
            case REQUEST_STORAGE_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // If you get permission, proceed
                    loadImagesFromWeb();

                } else {
                    // If you do not get permission, show a Toast and exit from app.
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            }
        }
    }


}
