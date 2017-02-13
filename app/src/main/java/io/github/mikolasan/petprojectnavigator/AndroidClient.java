package io.github.mikolasan.petprojectnavigator;

import android.app.Application;
import android.content.Context;

/**
 * Created by neupo on 11/7/2016.
 */
public class AndroidClient extends Application {
    //! TODO
    // http://stackoverflow.com/questions/37709918/warning-do-not-place-android-context-classes-in-static-fields-this-is-a-memory
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        AndroidClient.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return AndroidClient.context;
    }
}
