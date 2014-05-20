package com.pixplicity.wallabag;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Common tasks on images and image urls
 */
public final class ImageUtils {

    private static final String TAG = ImageUtils.class.getSimpleName();

    /**
     * Finds the url of the first image and returns it
     * @param html
     * @return
     */
    public static String getFirstImageUrl(String url, String html) {
        int openTagPosition = html.indexOf("<img");
        if (openTagPosition == -1) {
            return null;
        }

        int closeTagPosition = html.indexOf('>', openTagPosition);
        if (closeTagPosition == -1) {
            throw new RuntimeException("Error while parsing html");
        }

        String tagContent = html.substring(openTagPosition, closeTagPosition + 1);
        String[] tagParams = tagContent.split(" ");
        String imageSource = "";
        for (String param : tagParams) {
            if (param.startsWith("src")) {
                imageSource = param;
                break;
            }
        }

        imageSource = imageSource.replaceAll("src=", "")
            .replaceAll("\"", "")
            .trim();
        if (imageSource.startsWith("http://") || imageSource.startsWith("https://")) {
            // Complete url is complete
            return imageSource;
        } else if (imageSource.startsWith("//")) {
            // Protocol missing? Add protocol.
            return (url.startsWith("http:") ? "http:" : "https:") + imageSource;
        } else {
            int lastSlash = url.lastIndexOf('/');
            if (lastSlash > 0) {
                // Strip filename (e.g. index.html) and attach image url to path:
                return url.substring(0, lastSlash + 1) + imageSource;
            } else {
                return url + "/" + imageSource;
            }
        }
    }

    /**
     * Replaces all image urls in the source html to local urls.
     *
     * @param ctx
     * @param html
     * @return
     */
    public static String changeImagesUrl(Context ctx, String html) {
        int lastImageTag = 0;

        while (true) {
            int openTagPosition = html.indexOf("<img", lastImageTag);
            if (openTagPosition == -1) {
                break;
            }

            int closeTagPosition = html.indexOf('>', openTagPosition);
            if (closeTagPosition == -1) {
                throw new RuntimeException("Error while parsing html");
            }

            lastImageTag = closeTagPosition + 1;

            String tagContent = html.substring(openTagPosition,
                    closeTagPosition + 1);
            String[] tagParams = tagContent.split(" ");
            String imageSource = "";
            int sourceIndex = 0;
            for (String param : tagParams) {
                if (param.startsWith("src")) {
                    imageSource = param;
                    break;
                }
                sourceIndex++;
            }

            imageSource = imageSource.replaceAll("src=", "");
            imageSource = imageSource.replaceAll("\"", "");
            imageSource = imageSource.trim();

            File imageFileDestination = getImageFileDestination(ctx, "" + imageSource.hashCode());
            if (!imageFileDestination.exists()) {
                Bitmap bitmap = getBitmapFromURL(imageSource);
                if (bitmap == null) {
                    continue;
                }
                if (!saveBitmap(bitmap, imageFileDestination)) {
                    continue;
                }
            }

            tagParams[sourceIndex] = "src=\"file://" + imageFileDestination.getAbsolutePath()
                    + "\"";

            String newTag = recreateTag(tagParams);
            html = html.replace(tagContent, newTag);
        }

        return html;
    }

    /**
     * Joins the given array with spaces.
     * @param tagParams
     * @return
     */
    private static String recreateTag(String[] tagParams) {
        String tag = "";
        for (String param : tagParams) {
            tag += param + " ";
        }
        tag = tag.trim();
        return tag;
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (Exception e) {
            Log.w(TAG, "Error downloading image " + src + "; " + e.getMessage());
            return null;
        }
    }

    public static boolean saveBitmap(Bitmap bitmap, File saveLocation) {
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(saveLocation);
            bitmap.compress(Bitmap.CompressFormat.JPEG, Constants.JPEG_QUALITY, outputStream);
            outputStream.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static File getImageFileDestination(Context ctx, String imageUrl) {
        File saveFolder = Utils.getSaveDir(ctx);
        if (!saveFolder.exists()) {
            saveFolder.mkdirs();
        }

        return new File(saveFolder, imageUrl);
    }


}
