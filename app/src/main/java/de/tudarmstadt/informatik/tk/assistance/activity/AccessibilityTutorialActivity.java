package de.tudarmstadt.informatik.tk.assistance.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import butterknife.ButterKnife;
import butterknife.OnClick;
import de.tudarmstadt.informatik.tk.assistance.Constants;
import de.tudarmstadt.informatik.tk.assistance.R;
import de.tudarmstadt.informatik.tk.assistance.activity.base.BaseActivity;
import de.tudarmstadt.informatik.tk.assistance.sdk.provider.PreferenceProvider;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.logger.Log;

/**
 * A tutorial for accessibility service to enable
 * extended application context sensing
 *
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 10.10.2015
 */
public class AccessibilityTutorialActivity extends BaseActivity {

    private static final String TAG = AccessibilityTutorialActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean wasIgnoredByUser = PreferenceProvider
                .getInstance(getApplicationContext())
                .getAccessibilityServiceIgnoredByUser();

        if (wasIgnoredByUser) {

            setResult(Constants.INTENT_ACCESSIBILITY_SERVICE_ENABLED_RESULT);
            finish();
            return;

        } else {

            setContentView(R.layout.activity_accessibility_tutorial);

            ButterKnife.bind(this);
        }
    }

    @Override
    protected void onDestroy() {
        ButterKnife.unbind(this);
        super.onDestroy();
    }

    @OnClick(R.id.ignore_button)
    protected void onIgnoreButton() {
        Log.d(TAG, "User has chosen to ignore accessibility service!");

        PreferenceProvider.getInstance(getApplicationContext()).setAccessibilityServiceIgnoredByUser(true);

        setResult(Constants.INTENT_ACCESSIBILITY_SERVICE_IGNORED_RESULT);
        finish();
    }

    @OnClick(R.id.activate_now_button)
    protected void onActivateNowButton() {
        Log.d(TAG, "User has chosen to activate accessibility service!");

        // request user to switch permission for the service
        Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
        ActivityCompat.startActivityForResult(this, intent, Constants.INTENT_ACCESSIBILITY_SERVICE_ENABLED_RESULT, null);
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