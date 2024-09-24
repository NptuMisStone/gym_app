package com.NPTUMisStone.gym_app.User_And_Coach;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageHandle {

    //將URI轉換成byte[]：https://stackoverflow.com/questions/10296734/image-uri-to-bytesarray
    private static byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024, len;
        byte[] buffer = new byte[bufferSize];
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
    //將byte[]轉換成Bitmap：https://stackoverflow.com/questions/3520019/display-image-from-bytearray
    public static Bitmap getBitmap(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
    public static byte[] convertImageToBytes(Context context, Uri uri) throws IOException {
        InputStream iStream = context.getContentResolver().openInputStream(uri);    //將圖片路徑取得準備轉型
        return iStream != null ? ImageHandle.getBytes(iStream) : null;
    }
    //將Bitmap縮小避免卡頓：Github Copilot
    public static Bitmap resizeBitmap(Bitmap bitmap) {
        int maxWidth = 1000;
        int maxHeight = 1000;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float aspectRatio = (float) width / height;
        if (width > maxWidth || height > maxHeight) {
            if (width > height) {
                width = maxWidth;
                height = (int) (width / aspectRatio);
            } else {
                height = maxHeight;
                width = (int) (height * aspectRatio);
            }
        }
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }
}