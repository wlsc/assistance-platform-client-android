package de.tudarmstadt.informatik.tk.android.assistance.activity;

import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceActivity;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.io.IOException;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import de.tudarmstadt.informatik.tk.android.assistance.BuildConfig;
import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.event.PermissionGrantedEvent;
import de.tudarmstadt.informatik.tk.android.assistance.util.Constants;
import de.tudarmstadt.informatik.tk.android.assistance.util.PreferencesUtils;
import de.tudarmstadt.informatik.tk.android.assistance.util.Toaster;
import de.tudarmstadt.informatik.tk.android.assistance.util.UserUtils;
import de.tudarmstadt.informatik.tk.android.kraken.Config;
import de.tudarmstadt.informatik.tk.android.kraken.provider.HarvesterServiceProvider;
import de.tudarmstadt.informatik.tk.android.kraken.util.PermissionUtils;
import de.tudarmstadt.informatik.tk.android.kraken.util.StorageUtils;

/**
 * Core user settings activity
 */
public class SettingsActivity extends PreferenceActivity {

    private static final String TAG = SettingsActivity.class.getSimpleName();

    private final String[] VALID_FRAGMENTS = {
    };

    @Bind(R.id.toolbar)
    protected Toolbar mToolBar;

    public SettingsActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ViewGroup root = ButterKnife.findById(this, android.R.id.content);
        View content = root.getChildAt(0);
        LinearLayout toolbarContainer = (LinearLayout) View.inflate(this, R.layout.activity_settings, null);

        root.removeAllViews();
        toolbarContainer.addView(content);
        root.addView(toolbarContainer);

        ButterKnife.bind(this, toolbarContainer);

        setTitle(R.string.settings_activity_title);

        mToolBar.setTitle(getTitle());
        mToolBar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);

    }

    @OnClick(R.id.toolbar)
    protected void onBackClicked() {
        Log.d(TAG, "On toolbar back pressed");
        setResult(R.id.settings);
        finish();
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "On normal back pressed");
        setResult(R.id.settings);
        finish();
    }

    @Override
    public void onBuildHeaders(List<Header> headers) {

        if (BuildConfig.DEBUG) {
            Header exportDbHeader = new Header();
            exportDbHeader.title = "Dev Export DB";
            exportDbHeader.summary = "Just only in debug mode";
            exportDbHeader.id = 89898;
            headers.add(exportDbHeader);
        }
        loadHeadersFromResource(R.xml.preference_headers, headers);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {

        /**
         * Preventing fragment injection vulnerability
         */
        for (String validFragmentName : VALID_FRAGMENTS) {
            if (validFragmentName.equals(fragmentName)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void onHeaderClick(Header header, int position) {
        super.onHeaderClick(header, position);

        switch ((int) header.id) {
            case 89898:
                if (BuildConfig.DEBUG) {
                    checkWriteExternalStoragePermissionGranted();
                }
                break;
            case R.id.logout_settings:
                UserUtils.doLogout(getApplicationContext());
                setResult(Constants.INTENT_SETTINGS_LOGOUT_RESULT);
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        ButterKnife.unbind(this);
        Log.d(TAG, "onDestroy -> unbound resources");
        super.onDestroy();
    }

    /**
     * Reset user token/email and log him out
     */
    private void doLogout() {

        Log.d(TAG, "Doing logout...");

        PreferencesUtils.clearUserCredentials(getApplicationContext());

        setResult(R.id.logout_settings);

        // stop the kraken
        HarvesterServiceProvider.getInstance(getApplicationContext()).stopSensingService();

        finish();
    }

    public Toolbar getToolBar() {
        return mToolBar;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Config.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                EventBus.getDefault().post(new PermissionGrantedEvent(Manifest.permission.WRITE_EXTERNAL_STORAGE));
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * Checks read contacts permission
     */
    private void checkWriteExternalStoragePermissionGranted() {

        boolean isGranted = PermissionUtils
                .getInstance(getApplicationContext())
                .isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (isGranted) {

            Log.d(TAG, "WRITE_EXTERNAL_STORAGE permission was granted.");

            exportDatabase();
        } else {

            Log.d(TAG, "WRITE_EXTERNAL_STORAGE permission NOT granted!");

            // check if explanation is needed for this permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                Toaster.showLong(getApplicationContext(), R.string.permission_is_mandatory);
            }

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    Config.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    /**
     * Exports DB
     */
    private void exportDatabase() {

        try {
            StorageUtils.exportDatabase(
                    getApplicationContext(),
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/" + Config.DATABASE_NAME);

            Toaster.showLong(getApplicationContext(), R.string.settings_export_database_successful);

        } catch (IOException e) {
            Log.e(TAG, "Cannot export database to public folder. Error: ", e);
            Toaster.showLong(getApplicationContext(), R.string.settings_export_database_failed);
        }
    }
}
