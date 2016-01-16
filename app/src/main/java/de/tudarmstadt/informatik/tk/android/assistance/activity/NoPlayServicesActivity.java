package de.tudarmstadt.informatik.tk.android.assistance.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.view.View;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.activity.base.BaseActivity;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.GcmUtils;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.logger.Log;
import de.tudarmstadt.informatik.tk.android.assistance.util.CommonUtils;

/**
 * Shows information in case of no Google Play Services installed
 *
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 29.12.2015
 */
public class NoPlayServicesActivity extends BaseActivity {

    private static final String TAG = NoPlayServicesActivity.class.getSimpleName();

    private static final int UI_ANIMATION_DELAY = 300;

    private final Handler mHideHandler = new Handler();

    private final Runnable mHidePart2Runnable = () -> CommonUtils.hideSystemUI(getWindow());

    @Bind(R.id.fullscreen_content_controls)
    protected View mControlsView;

    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {

            ActionBar actionBar = getSupportActionBar();

            if (actionBar != null) {
                actionBar.show();
            }

            mControlsView.setVisibility(View.VISIBLE);
        }
    };

    private boolean mVisible;

    private final Runnable mHideRunnable = this::hide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_play_services);
        ButterKnife.bind(this);

        mVisible = true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        delayedHide(150);
    }

    @Override
    protected void onDestroy() {
        ButterKnife.unbind(this);
        super.onDestroy();
    }

    @Override
    protected void subscribeRequests() {

    }

    @Override
    protected void unsubscribeRequests() {

    }

    @Override
    protected void recreateRequests() {

    }

    @OnClick(R.id.fullscreen_content)
    void onContentView() {
        toggleView();
    }

    @OnClick(R.id.check_button)
    void onCheckButtonClick() {

        if (GcmUtils.isPlayServicesInstalled(this)) {
            Log.d(TAG, "Google Play Services are installed.");
            finish();
        } else {
            Log.d(TAG, "Google Play Services NOT installed.");
            // do nothing, present this activity
        }
    }

    private void toggleView() {

        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {

        // Hide UI first
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.hide();
        }

        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private void show() {

        CommonUtils.showSystemUI(getWindow());

        mVisible = true;

        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    private void delayedHide(int delayMillis) {

        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}