package com.cityonedriver.utils;

import android.app.Application;
import android.content.Context;
import android.view.View;
import android.widget.Toast;

public class App extends Application {

    private static Toast toast = null;

    public static void showToast(final Context context, String text, int duration) {

        if (toast == null || toast.getView().getWindowVisibility() != View.VISIBLE) {
            toast = Toast.makeText(context, text, duration);
            toast.show();
        } else toast.cancel();

    }


}
