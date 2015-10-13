package de.tudarmstadt.informatik.tk.android.assistance.activity;

import android.Manifest;
import android.app.FragmentManager;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.event.PermissionGrantedEvent;
import de.tudarmstadt.informatik.tk.android.assistance.fragment.settings.ApplicationAboutSettingsFragment;
import de.tudarmstadt.informatik.tk.android.assistance.fragment.settings.ApplicationSettingsFragment;
import de.tudarmstadt.informatik.tk.android.assistance.fragment.settings.DevelopmentSettingsFragment;
import de.tudarmstadt.informatik.tk.android.assistance.fragment.settings.SensorsListFragment;
import de.tudarmstadt.informatik.tk.android.assistance.fragment.settings.UserDeviceInfoSettingsFragment;
import de.tudarmstadt.informatik.tk.android.assistance.fragment.settings.UserProfileFragment;
import de.tudarmstadt.informatik.tk.android.assistance.util.PreferencesUtils;
import de.tudarmstadt.informatik.tk.android.assistance.util.UserUtils;
import de.tudarmstadt.informatik.tk.android.kraken.Config;
import de.tudarmstadt.informatik.tk.android.kraken.provider.HarvesterServiceProvider;

/**
 * Core user settings activity
 */
public class SettingsActivity extends PreferenceActivity {

    private static final String TAG = SettingsActivity.class.getSimpleName();

    private final String[] VALID_FRAGMENTS = {
            ApplicationAboutSettingsFragment.class.getName(),
            ApplicationSettingsFragment.class.getName(),
            DevelopmentSettingsFragment.class.getName(),
            UserDeviceInfoSettingsFragment.class.getName(),
            UserProfileFragment.class.getName(),
            SensorsListFragment.class.getName()
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


        FragmentManager fm = getFragmentManager();
        fm.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {

            @Override
            public void onBackStackChanged() {
                if (getFragmentManager().getBackStackEntryCount() == 0) {
                    setResult(R.id.settings);
                    finish();
                }
            }
        });
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

        loadHeadersFromResource(R.xml.preference_headers, headers);

        boolean isUserDeveloper = UserUtils.isUserDeveloper(getApplicationContext());

        for (Header header : headers) {
            if (header.id == R.id.development_settings) {

                // user not developer -> remove developer menu entry
                if (!isUserDeveloper) {
                    headers.remove(header);
                }

                break;
            }
        }
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
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
    }

    @Override
    public void onHeaderClick(Header header, int position) {
        super.onHeaderClick(header, position);

        switch ((int) header.id) {

            case R.id.logout_settings:
                doLogout();
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

        PreferencesUtils.clearUserCredentials(this);

        setResult(R.id.logout_settings);

        // stop the kraken
        stopSensingService();

        finish();
    }

    /**
     * Calms down the Kraken.
     */
    private void stopSensingService() {

        HarvesterServiceProvider service = HarvesterServiceProvider.getInstance(getApplicationContext());
        service.stopSensingService();
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
}
