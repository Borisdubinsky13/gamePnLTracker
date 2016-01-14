/**
 *
 */
package com.gamesPnL;

import android.app.Application;
import android.content.Context;

/**
 * @author boris
 */
public class GamesPnL extends Application {
    private static Context context;

    public void onCreate() {
        super.onCreate();
        GamesPnL.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return GamesPnL.context;
    }
}
