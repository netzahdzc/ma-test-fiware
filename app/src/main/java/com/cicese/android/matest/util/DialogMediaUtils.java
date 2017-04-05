package com.cicese.android.matest.util;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.cicese.android.matest.ConcentratedReportActivity;
import com.cicese.android.matest.R;

public class DialogMediaUtils {

    private Context mDialogMediaActivity;

    private Dialog mDialog;
    private EditText mDialogComment;
    private TextView mOKButton;
    private TextView mCancelButton;
    private RatingBar mRatingBar;

    private long uniqueTestId;
    private boolean q1, q2, q3, q4, q5, q6, q7, q8, q9, q10;

    public DialogMediaUtils(Context context) {
        this.mDialogMediaActivity = context;
    }

    public void showDialog(long uniqueTestId, boolean q1, boolean q2, boolean q3, boolean q4,
                           boolean q5, boolean q6, boolean q7, boolean q8, boolean q9, boolean q10) {
        this.uniqueTestId = uniqueTestId;
        this.q1 = q1;
        this.q2 = q2;
        this.q3 = q3;
        this.q4 = q4;
        this.q5 = q5;
        this.q6 = q6;
        this.q7 = q7;
        this.q8 = q8;
        this.q9 = q9;
        this.q10 = q10;

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

        testDBObj.updateData(uniqueTestId, 0, 0, 0, "", String.valueOf(q1), String.valueOf(q2),
                String.valueOf(q3), String.valueOf(q4), String.valueOf(q5), String.valueOf(q6),
                String.valueOf(q7), String.valueOf(q8), String.valueOf(q9), String.valueOf(q10),
                testScore, testComments, "testQualityData");

//        Log.v("XXX zz", "" + String.valueOf(q1) + ", " + String.valueOf(q2) + "," +
//                String.valueOf(q3) + "," + String.valueOf(q4) + "," + String.valueOf(q5) + "," + String.valueOf(q6) + "," +
//                String.valueOf(q7) + "," + String.valueOf(q8) + "," + String.valueOf(q9) + "," + String.valueOf(q10));
        testDBObj.closeDB();
    }

}
