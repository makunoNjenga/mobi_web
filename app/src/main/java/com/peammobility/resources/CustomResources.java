package com.peammobility.resources;

import android.app.Activity;
import android.view.Window;
import android.view.WindowManager;

import androidx.core.content.ContextCompat;
import com.peammobility.R;

public class CustomResources {
    Activity activity;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public CustomResources(Activity activity) {
        this.activity = activity;
    }
    public CustomResources() {
    }

    public void setStatusBar(){
        Window window = activity.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(activity, R.color.primary));
    }
}
