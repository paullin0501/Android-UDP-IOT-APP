package com.rexlite.rexlitebasicnew;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

class LoadingDialog2 {
    private Activity activity;
    private AlertDialog dialog;

    LoadingDialog2(Activity myActivity){
        activity = myActivity;
    }
    void startLoadingDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.custom_dialog_2,null));
        builder.setCancelable(false); //是否可以透過點擊畫面取消Loading畫面

        dialog = builder.create();
        dialog.show();
    }
    void dismissDialog() {
        dialog.dismiss();
    }
}
