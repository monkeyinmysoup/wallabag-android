package com.pixplicity.wallabag;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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
            e.printStackTrace();
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
