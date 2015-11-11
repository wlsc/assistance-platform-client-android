package de.tudarmstadt.informatik.tk.android.assistance.fragment.settings;

import android.Manifest;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import java.io.IOException;

import de.greenrobot.event.EventBus;
import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.activity.SettingsActivity;
import de.tudarmstadt.informatik.tk.android.assistance.event.PermissionGrantedEvent;
import de.tudarmstadt.informatik.tk.android.assistance.util.PreferencesUtils;
import de.tudarmstadt.informatik.tk.android.assistance.notification.Toaster;
import de.tudarmstadt.informatik.tk.android.kraken.Config;
import de.tudarmstadt.informatik.tk.android.kraken.util.PermissionUtils;
import de.tudarmstadt.informatik.tk.android.kraken.util.StorageUtils;

/**
 * Created by Wladimir Schmidt on 29.06.2015.
 */
public class DevelopmentSettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = DevelopmentSettingsFragment.class.getSimpleName();

    private Toolbar mParentToolbar;

    public DevelopmentSettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preference_development);

        mParentToolbar = ((SettingsActivity) getActivity()).getToolBar();

        if (mParentToolbar != null) {
            mParentToolbar.setTitle(R.string.settings_header_development_title);
        }

        boolean isUserDeveloper = PreferencesUtils.isUserDeveloper(getActivity().getApplicationContext());

        SwitchPreference beDevPref = (SwitchPreference) findPreference("pref_be_developer");
        beDevPref.setChecked(isUserDeveloper);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equalsIgnoreCase("pref_be_developer")) {

            boolean isDeveloper = PreferencesUtils.isUserDeveloper(getActivity().getApplicationContext());

            if (isDeveloper) {
                Log.d(TAG, "Developer mode is ENABLED.");
            } else {
                Log.d(TAG, "Developer mode is DISABLED.");
            }

            PreferencesUtils.setDeveloperStatus(getActivity().getApplicationContext(), isDeveloper);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        if (preference.getKey().equals("pref_export_database")) {

            Log.d(TAG, "User clicked export database menu");

            checkWriteExternalStoragePermissionGranted();
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }

        super.onPause();
    }

    /**
     * Checks read contacts permission
     */
    private void checkWriteExternalStoragePermissionGranted() {

        boolean isGranted = PermissionUtils
                .getInstance(getActivity().getApplicationContext())
                .isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (isGranted) {

            Log.d(TAG, "WRITE_EXTERNAL_STORAGE permission was granted.");

            exportDatabase();

        } else {

            Log.d(TAG, "WRITE_EXTERNAL_STORAGE permission NOT granted!");

            // check if explanation is needed for this permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                Toaster.showLong(getActivity().getApplicationContext(), R.string.permission_is_mandatory);
            }

            ActivityCompat.requestPermissions(getActivity(),
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
                    getActivity().getApplicationContext(),
                    Environment
                            .getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_DOWNLOADS).getPath() +
                            "/" +
                            Config.DATABASE_NAME);

            Toaster.showLong(getActivity().getApplicationContext(), R.string.settings_export_database_successful);

        } catch (IOException e) {
            Log.e(TAG, "Cannot export database to public folder. Error: ", e);
            Toaster.showLong(getActivity().getApplicationContext(), R.string.settings_export_database_failed);
        }
    }

    /**
     * On permission granted event
     *
     * @param event
     */
    public void onEvent(PermissionGrantedEvent event) {

        String permission = event.getPermission();

        Log.d(TAG, "Permission granted: " + permission);

        if (permission == null) {
            return;
        }

        if (permission.equals(Manifest.permission.READ_CONTACTS)) {
            exportDatabase();
        }
    }
}
