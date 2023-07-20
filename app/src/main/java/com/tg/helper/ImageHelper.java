package com.tg.helper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.tg.derdoapp.R;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageHelper {
    public static Bitmap getBitmapFromURL(String src) {
        try {

            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap mybitmap = BitmapFactory.decodeStream(input);

            return mybitmap;

        } catch (Exception ex) {

            return null;
        }
    }
}
