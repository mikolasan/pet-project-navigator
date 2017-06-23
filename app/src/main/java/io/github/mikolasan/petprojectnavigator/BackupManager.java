package io.github.mikolasan.petprojectnavigator;

import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

class BackupManager {
    private static final String OPEN_FILE_TAG = "open-file-dialog";

    private static String inputToString(FileInputStream inputStream) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int i;
        try {
            i = inputStream.read();
            while (i != -1) {
                byteArrayOutputStream.write(i);
                i = inputStream.read();
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        return byteArrayOutputStream.toString();
    }

    static String getFilePath(Uri uri) {
        Log.i(OPEN_FILE_TAG, "Uri = " + uri.toString());
        final String path = "";
        return path;
    }

    static String readBackupFile(Uri uri) throws IOException {
        String result = "";
        // Get the file path from the URI
        final String path = getFilePath(uri);
        Log.i(OPEN_FILE_TAG, "File Selected: " + path);

        File selectedFile = new File(path);
        if (selectedFile.exists()) {
            FileInputStream inputStream;
            try {
                inputStream = new FileInputStream(selectedFile);
                result = inputToString(inputStream);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            CharSequence status = "File " + path + " doesn't exist";
            Log.i(OPEN_FILE_TAG, status.toString());
        }
        return result;
    }
}
