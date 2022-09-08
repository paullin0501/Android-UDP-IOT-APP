package com.rexlite.rexlitebasicnew;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.TextView;

class LoadingDialog {
   private Activity activity;
   private AlertDialog dialog;

    LoadingDialog(Activity myActivity){
        activity = myActivity;
    }
    void startLoadingDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.custom_dialog,null));
        builder.setCancelable(false); //是否可以透過點擊畫面取消Loading畫面

        dialog = builder.create();
        dialog.show();
    }
    void startLoadingDialog(String settingText){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.custom_dialog,null));
        builder.setCancelable(false); //是否可以透過點擊畫面取消Loading畫面
        dialog = builder.create();
        dialog.show();
        TextView textView;
        textView = (TextView) dialog.findViewById(R.id.loading_text);
        textView.setText(settingText);
    }
    void dismissDialog() {
        dialog.dismiss();
    }

    void setLoadingText(String settingText){

    }
}
