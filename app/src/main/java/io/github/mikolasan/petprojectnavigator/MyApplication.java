package io.github.mikolasan.petprojectnavigator;

import android.app.Application;
import android.content.Context;

/**
 * Created by neupo on 11/7/2016.
 */
public class MyApplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        MyApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }
}
