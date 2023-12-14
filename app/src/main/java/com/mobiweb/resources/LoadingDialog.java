package com.mobiweb.resources;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Handler;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.mobiweb.R;

public class LoadingDialog {
    private Activity activity;
    private AlertDialog dialog;
    private TextView title;

    public LoadingDialog(Activity myActivity) {
        activity = myActivity;
    }

    /**
     *
     */
    @SuppressLint("InflateParams")
    public void startLoadingDialog() {
       try{
           AlertDialog.Builder builder = new AlertDialog.Builder(activity);
           LayoutInflater inflater = activity.getLayoutInflater();
           builder.setView(inflater.inflate(R.layout.loading_screen, null));
           builder.setCancelable(false);

           dialog = builder.create();
           dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
           dialog.show();

           new Handler().postDelayed(() -> {
               try {
                   dialog.dismiss();
               } catch (Exception ignored) {
               }
           }, 15000);

       }catch (Exception ignored){}
    }

    /**
     *
     */
    public void dismissDialog() {
       try{
           dialog.dismiss();
       }catch (Exception ignored){

       }
    }
}
