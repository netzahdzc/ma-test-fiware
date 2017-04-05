package com.cicese.android.matest.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;

import com.cicese.android.matest.MainActivity;
import com.cicese.android.matest.R;

/**
 * Created by netzahdzc on 7/20/16.
 */
public class DialogMessageUtils {

    private Context mDialogMessageUtils;

    public DialogMessageUtils(Context context) {
        this.mDialogMessageUtils = context;
    }

    public void dialogMessage(String title, String message, String leftButton, String rightButton, int icon) {
        AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(mDialogMessageUtils);

        // Setting Dialog Title
        alertDialog2.setTitle(title);

        // Setting Dialog Message
        alertDialog2.setMessage(message);

        // Setting Icon to Dialog
        alertDialog2.setIcon(icon);

        // Setting Negative "Left-side button" Btn
        alertDialog2.setNegativeButton(leftButton,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to execute after dialog
                        dialog.cancel();
                    }
                });

        // Setting Positive "Right-side button" Btn
        alertDialog2.setPositiveButton(rightButton,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to execute after dialog
                    }
                });

        // Showing Alert Dialog
        alertDialog2.show();
    }

    public void dialogWarningMessage(String title, String message, boolean flag) {
        final boolean mFlag;
        mFlag = flag;

        AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(mDialogMessageUtils);

        // Setting Dialog Title
        alertDialog2.setTitle(title);

        // Setting Dialog Message
        alertDialog2.setMessage(message);

        // Setting Icon to Dialog
        alertDialog2.setIcon(R.mipmap.ic_warning);

        // Setting Negative "Left-side button" Btn
        alertDialog2.setPositiveButton(mDialogMessageUtils.getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to execute after dialog
                        dialog.cancel();
                        if(mFlag) ((Activity) mDialogMessageUtils).finish();
                    }
                });

        // Showing Alert Dialog
        alertDialog2.show();
    }

    public void dialogWarningDeleteMessage(String title, String message, boolean flag) {
        final boolean mFlag;
        mFlag = flag;

        AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(mDialogMessageUtils);

        // Setting Dialog Title
        alertDialog2.setTitle(title);

        // Setting Dialog Message
        alertDialog2.setMessage(message);

        // Setting Icon to Dialog
        alertDialog2.setIcon(R.mipmap.ic_warning);

        // Setting Negative "Left-side button" Btn
        alertDialog2.setPositiveButton(mDialogMessageUtils.getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to execute after dialog
                        dialog.cancel();
                        Intent intent = new Intent(mDialogMessageUtils, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // To clean up all activities
                        mDialogMessageUtils.startActivity(intent);
                        if(mFlag) ((Activity) mDialogMessageUtils).finish();
                    }
                });

        // Showing Alert Dialog
        alertDialog2.show();
    }

}
