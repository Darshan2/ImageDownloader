package com.darshan.android.imagedownloader.adapters;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.darshan.android.imagedownloader.R;
import com.darshan.android.imagedownloader.retrofit.Image;
import com.darshan.android.imagedownloader.utils.DownloaderUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/**
 * Adapter for displaying Image List in MainActivity
 */
public class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.ViewHolder>{
    private static final String TAG = "ImageListAdapter";
    private static final int NOTIFICATION_ID = 1111;

    private Context mContext;
    private List<Image> mImageList;


    public ImageListAdapter(Context mContext, List<Image> mImageList) {
        this.mContext = mContext;
        this.mImageList = mImageList;
    }

    /**
     * Each Recycle Item is represented by item_image layout.
     * Inflate item_image layout here
     * @param parent
     * @param viewType
     * @return ViewHolder for item_image layout
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.item_image, null);

        return new ViewHolder(view);
    }


    /**
     * Views of item_image layout are populated here
     * @param holder ViewHolder object for item_image layout
     * @param position item position in RecyclerView
     */
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Image currentImage = mImageList.get(position);

        holder.tvFileName.setText(currentImage.getFilename());
        holder.tvAuthor.setText(currentImage.getAuthor());

        //Set onclick listener for Download button
        holder.btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //To prevent user from clicking same button again and again
                holder.btnDownload.setClickable(false);
                //Download image in background
                new DownloadAsyncTask(currentImage.getFilename(), holder.btnDownload, holder.sbProgress)
                        .execute(currentImage.getPostUrl());
            }
        });

    }


    /**
     * Return number of items that RecyclerView has to display
     */
    @Override
    public int getItemCount() {
        return mImageList.size();
    }


    /* ViewHolder class for item_image layout */
    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFileName, tvAuthor;
        Button btnDownload, btnRetry;
        SeekBar sbProgress;

        private ViewHolder(View itemView) {
            super(itemView);

            tvFileName = itemView.findViewById(R.id.fileName_TV);
            tvAuthor = itemView.findViewById(R.id.author_TV);
            btnDownload = itemView.findViewById(R.id.download_Btn);
            btnRetry = itemView.findViewById(R.id.retry_Btn);
            sbProgress = itemView.findViewById(R.id.progress_SeekBar);
        }
    }


    //-------------------------------------    AsyncTask ------------------------------------------//
     class DownloadAsyncTask extends AsyncTask<String, Integer, Boolean> {
        private Button mDownloadBtn;
        private SeekBar mProgressSeekBar;
        private String mImageFileName;

        private NotificationCompat.Builder mBuilder;
        private NotificationManagerCompat mNotificationManager;
        private int mPreviousProgress = 0;

        private DownloadAsyncTask(String fileName, Button downloadBtn, SeekBar seekBar) {
            mImageFileName = fileName;
            mDownloadBtn = downloadBtn;
            mProgressSeekBar = seekBar;
        }


        /**
         * This method will download image in background.
         * Runs in Worker-Thread
         *
         * @param urls from .execute(). Values passed when AsyncTask got executed
         * @return null
         */
        @Override
        protected Boolean doInBackground(String... urls) {
            int count;
            //can the selected image be downloaded at the time.
            boolean downloadable = false;
            try {
                //urls[0] have post_url of image, we can get image download url by appending "/download" to it.
                String downloadImage = urls[0] + "/download";
                URL imageUrl = new URL(downloadImage);

                URLConnection urlConnection = imageUrl.openConnection();
                urlConnection.connect();

                // getting file length
                int fileLength = urlConnection.getContentLength();
                Log.d(TAG, "doInBackground: file length " + fileLength);

                if (fileLength > -1) {
                    downloadable = true;

                    // input stream to read file - with 8k buffer
                    InputStream inputStream = new BufferedInputStream(imageUrl.openStream(), 8192);

                /*
                    File saveLocation, where new image will be saved
                    Image will be saved in Phone memory under Pictures/Picsum folder,
                    with name image's file name(provided by urls[1])
                 */
                    File saveLocation = new File(DownloaderUtils.getImageDirectory(), mImageFileName);
                    // Output stream to write file
                    OutputStream outputStream = new FileOutputStream(saveLocation);

                    byte[] data = new byte[1024];
                    long total = 0;
                    //input.read(data) Read upto 1kb of data at a time. If their is no data to read it returns -1
                    while ((count = inputStream.read(data)) != -1) {
                        total += count;
                        // publishing the progress....
                        // After this onProgressUpdate will be called
                        if (fileLength > 0) {
                            publishProgress((int) ((total * 100) / fileLength));
                            // writing data to file
                            outputStream.write(data, 0, count);
                        }
                    }

                    // flushing output
                    outputStream.flush();
                    // closing streams
                    outputStream.close();
                    inputStream.close();
                }

            } catch (IOException e) {
                // closing streams
                e.printStackTrace();
            }

            return downloadable;
        }

        /**
         * This method will get executed before background execution start.
         * Runs in UI-Thread
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            Log.d(TAG, "onPreExecute: ");
            mProgressSeekBar.setVisibility(View.VISIBLE);
            mProgressSeekBar.setMax(100);
            createNotification();
        }

        /**
         * Usually called from doInBackground() method to indicate background task progress.
         * This method runs in UI-Thread.
         *
         * @param values indicates progress back ground task execution.
         */
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            int progress = values[0];
            //for smooth progressbar transition
            if (mPreviousProgress < progress) {
//                Log.d(TAG, "onProgressUpdate: " + progress);
                //Displaying download progress in images list
                mProgressSeekBar.setProgress(progress);

                //Display download progress in notification
                mBuilder.setContentText("Download in progress")
                        .setProgress(100, progress, false);
                mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

                mPreviousProgress = progress;
            }

        }


        /**
         * When back ground task(here downloading file) got completed,
         * this method will be executed in UI-Thread.
         *
         * @param downloadable result from doInBackground() method
         */
        @Override
        protected void onPostExecute(Boolean downloadable) {
            super.onPostExecute(downloadable);

            //Image is downloaded successfully
            if (downloadable) {
                mDownloadBtn.setVisibility(View.GONE);
            /*
                In case of fast image download, some time "Download complete" notification does not shows
                in order to show this notification all the time make Thread sleep for 1sec
             */
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // When done, update the notification one more time to remove the progress bar
                mBuilder.setContentText("Download complete")
                        .setProgress(0, 0, false);
                mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

            } else {
                /* some problem like (file_legth = -1) occurs while downloading
                    so ask the user to retry downloading image */
                mDownloadBtn.setText(R.string.retry);
                // Show download failed notification
                mBuilder.setContentText("Download failed")
                        .setProgress(0, 0, false);
                mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
            }

            //anyway, hide the seek bar showing download progress in image list.
            mProgressSeekBar.setVisibility(View.GONE);
            //make Download button clickable
            mDownloadBtn.setClickable(true);
        }


        /**
         * Shows progress bar notification, which shows progress of image download.
         */
        private void createNotification() {
            mNotificationManager = NotificationManagerCompat.from(mContext);
            mBuilder = DownloaderUtils.getNotificationBuilder(mContext);

            mBuilder.setContentTitle(mImageFileName)
                    .setContentText("Waiting for network")
                    .setSmallIcon(R.drawable.ic_notification)
                    .setOnlyAlertOnce(true)
                    .setPriority(NotificationCompat.PRIORITY_LOW);

            // Issue the initial notification with zero progress
            mBuilder.setProgress(100, 0, false);
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        }
    }

}

