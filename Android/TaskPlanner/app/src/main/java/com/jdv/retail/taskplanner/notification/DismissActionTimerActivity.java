package com.jdv.retail.taskplanner.notification;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.DelayedConfirmationView;
import android.view.View;
import android.widget.TextView;

import com.jdv.retail.taskplanner.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DismissActionTimerActivity extends WearableActivity implements
        DelayedConfirmationView.DelayedConfirmationListener {

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);

    private static final int DECISION_TIME = 5000;
    public static final String TITLE_KEY = "com.jdv.retail.taskplanner.notification.DismissActionTimerActivity.titel_text";
    public static final String CONTENT_KEY = "com.jdv.retail.taskplanner.notification.DismissActionTimerActivity.content_text";

    private BoxInsetLayout mContainerView;
    private TextView mTitleTextView;
    private TextView mContentTextView;

    private DelayedConfirmationView mDelayedView;

    private String titleText;
    private String contentText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dismiss_action_timer);
        setAmbientEnabled();

        titleText = getIntent().getStringExtra(TITLE_KEY);
        contentText = getIntent().getStringExtra(CONTENT_KEY);

        mDelayedView =
                (DelayedConfirmationView) findViewById(R.id.delayed_confirm);
        mDelayedView.setListener(this);

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mTitleTextView = (TextView) findViewById(R.id.titletext);
        mContentTextView = (TextView) findViewById(R.id.contenttext);
        mTitleTextView.setText(titleText);
        mContentTextView.setText(contentText);

        // Two seconds to cancel the action
        mDelayedView.setTotalTimeMs(DECISION_TIME);
        // Start the timer
        mDelayedView.start();
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        updateDisplay();
        super.onExitAmbient();
    }

    private void updateDisplay() {
        if (isAmbient()) {
            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
            mTitleTextView.setTextColor(getResources().getColor(android.R.color.white));
            mContentTextView.setTextColor(getResources().getColor(android.R.color.white));

        } else {
            mContainerView.setBackground(null);
            mTitleTextView.setTextColor(getResources().getColor(android.R.color.black));
            mContentTextView.setTextColor(getResources().getColor(android.R.color.black));
        }
    }

    @Override
    public void onTimerFinished(View view) {
        // User didn't cancel, perform the action
        setResult(RESULT_OK);
        finish();

    }

    @Override
    public void onTimerSelected(View view) {
        // User canceled, abort the action
        setResult(RESULT_CANCELED);
        finish();
    }
}
