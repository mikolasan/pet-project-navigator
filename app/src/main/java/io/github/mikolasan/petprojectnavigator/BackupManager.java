package io.github.mikolasan.petprojectnavigator;

import android.content.Context;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

class BackupManager {
    private static final int bufferSize = 1024;
    private static final String charsetName = StandardCharsets.UTF_8.name();

    private static String inputToString(InputStream inputStream) {
        String result = "";
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[bufferSize];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, length);
            }
            inputStream.close();
            result = byteArrayOutputStream.toString(charsetName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    static String readBackupFile(Context context, Uri uri) throws FileNotFoundException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        return inputToString(inputStream);
    }
}
