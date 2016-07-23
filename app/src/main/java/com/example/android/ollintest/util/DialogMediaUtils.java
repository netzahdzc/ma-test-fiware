package com.example.android.ollintest.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.ollintest.ConcentratedReportActivity;
import com.example.android.ollintest.R;

public class DialogMediaUtils {

    private Context mDialogMediaActivity;

    private Dialog mDialog;
    private EditText mDialogComment;
    private TextView mOKButton;
    private TextView mCancelButton;
    private RatingBar mRatingBar;

    private long uniqueTestId;

    public DialogMediaUtils(Context context) {
        this.mDialogMediaActivity = context;
    }

    public void showDialog(long uniqueTestId) {
        this.uniqueTestId = uniqueTestId;

        if (mDialog == null) {
            mDialog = new Dialog(mDialogMediaActivity,
                    R.style.CustomDialogTheme);
        }

        mDialog.setContentView(R.layout.test_evaluation);
        mDialog.show();

        mOKButton = (TextView) mDialog.findViewById(R.id.button_submit_test);
        mCancelButton = (TextView) mDialog.findViewById(R.id.button_repeat_test);
        mRatingBar = (RatingBar) mDialog.findViewById(R.id.dialog_media_ratingBar);
        mDialogComment = (EditText) mDialog.findViewById(R.id.dialog_media_comment);

        mRatingBar.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                // TODO Auto-generated method stub
                Toast.makeText(mDialogMediaActivity, Float.toString(rating), Toast.LENGTH_SHORT).show();
            }
        });

        initDialogButtons();
    }

    private void initDialogButtons() {
        mOKButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                saveQualityDataScore(uniqueTestId, mRatingBar.getRating(), mDialogComment.getText().toString());

                Intent finalReportScreen = new Intent(mDialogMediaActivity.getApplicationContext(), ConcentratedReportActivity.class);
                finalReportScreen.putExtra("uniqueTestId", uniqueTestId);
                mDialogMediaActivity.startActivity(finalReportScreen);

                mDialog.dismiss();
            }
        });

        mCancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.dismiss();
            }
        });
    }

    public void dismissDialog() {
        mDialog.dismiss();
    }

    public void saveQualityDataScore(long uniqueTestId, float testScore, String testComments) {
        TestDBHandlerUtils testDBObj = new TestDBHandlerUtils(mDialogMediaActivity);
        testDBObj.openDB();

        testDBObj.updateData(uniqueTestId, 0, 0, 0, "", "", "", "", "", "", "",
                "", "", "", "", testScore, testComments, "testQualityData");

        testDBObj.closeDB();
    }

}
