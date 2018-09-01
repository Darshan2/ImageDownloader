package com.darshan.android.imagedownloader.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;

import java.io.File;
import java.util.Random;


public class DownloaderUtils {
    private static final String TAG = "DownloaderUtils";

    // This is the Notification Channel ID.
    private static final String NOTIFICATION_CHANNEL_ID = "downloader_channel";
    //Channel Name
    private static final String CHANNEL_NAME = "Downloader Notification Channel";


    /* Checks if external storage is available for read and write */
    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * To access downloaded image from any app, we need to save that image in public directory.
     * @param albumName name of subDir under Picture directory in phone memory
     * @return file representing albumName subDir
     */
    private static File getPublicAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
        if(!file.isDirectory()) {
            file.mkdir();
        }

        return file;
    }


    /**
     * Construct and return file representing folder /storage/emulated/0/Pictures/Picsum/
     * @return file
     */
    public static File getImageDirectory() {
        File saveFile = null;
        if(isExternalStorageWritable()) {
            saveFile = getPublicAlbumStorageDir("Picsum");
        }
        return saveFile;
    }


    public static NotificationCompat.Builder getNotificationBuilder(Context context) {
        createNotificationChannel(context);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);

        return builder;
    }


    private static void createNotificationChannel(Context context) {
        //Notification channel should only be created for devices running Android 26
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Importance applicable to all the notifications in this Channel
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel notificationChannel =
                    new NotificationChannel(NOTIFICATION_CHANNEL_ID, CHANNEL_NAME, importance);

            String channelDescription = "This is channel description";
            notificationChannel.setDescription(channelDescription);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            //finally create notification channel
            NotificationManager notificationManager =
                    (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
    }


    public static int getRandomNumBetween(int max, int min) {
        int random = new Random().nextInt((max - min) + 1) + min;
        return random;
    }
}
