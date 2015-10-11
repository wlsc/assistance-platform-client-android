package de.tudarmstadt.informatik.tk.android.assistance.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.util.Constants;
import de.tudarmstadt.informatik.tk.android.kraken.provider.PreferenceProvider;

/**
 * A tutorial for accessibility service to enable
 * extended application context sensing
 *
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 10.10.2015
 */
public class AccessibilityTutorialActivity extends AppCompatActivity {

    private static final String TAG = AccessibilityTutorialActivity.class.getSimpleName();

    @Bind(R.id.ignore_button)
    protected Button ignoreButton;

    @Bind(R.id.activate_now_button)
    protected Button activateNowButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accessibility_tutorial);

        ButterKnife.bind(this);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: Unbinding resources...");
        ButterKnife.unbind(this);
        super.onDestroy();
    }

    @OnClick(R.id.ignore_button)
    protected void onIgnoreButton() {
        Log.d(TAG, "User has chosen to ignore accessibility service!");
        setResult(Constants.INTENT_ACCESSIBILITY_SERVICE_IGNORED_RESULT);
        finish();
    }

    @OnClick(R.id.activate_now_button)
    protected void onActivateNowButton() {
        Log.d(TAG, "User has chosen to activate accessibility service!");
        requestAccessibilityPermission();
    }

    /**
     * Shows accessibility settings to user
     */
    private void requestAccessibilityPermission() {

        // request user to switch permission for the service
        Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivityForResult(intent, Constants.INTENT_ACCESSIBILITY_SERVICE_ENABLED_RESULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case Constants.INTENT_ACCESSIBILITY_SERVICE_ENABLED_RESULT:

                boolean isActivated = PreferenceProvider
                        .getInstance(getApplicationContext())
                        .getActivated();

                // if user has activated an accessibility service
                // finish tutorial
                if (isActivated) {
                    setResult(Constants.INTENT_ACCESSIBILITY_SERVICE_ENABLED_RESULT);
                    finish();
                }

                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }
}