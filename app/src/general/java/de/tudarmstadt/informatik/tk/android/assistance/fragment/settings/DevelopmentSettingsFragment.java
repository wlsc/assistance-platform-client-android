package de.tudarmstadt.informatik.tk.android.assistance.fragment.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import java.io.IOException;

import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.activity.SettingsActivity;
import de.tudarmstadt.informatik.tk.android.assistance.util.Toaster;
import de.tudarmstadt.informatik.tk.android.assistance.util.UserUtils;
import de.tudarmstadt.informatik.tk.android.kraken.Config;
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

        boolean isUserDeveloper = UserUtils.isUserDeveloper(getActivity().getApplicationContext());

        SwitchPreference beDevPref = (SwitchPreference) findPreference("pref_be_developer");
        beDevPref.setChecked(isUserDeveloper);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equalsIgnoreCase("pref_be_developer")) {

            boolean isDeveloper = UserUtils.isUserDeveloper(getActivity().getApplicationContext());

            if (isDeveloper) {
                Log.d(TAG, "Developer mode is ENABLED.");
            } else {
                Log.d(TAG, "Developer mode is DISABLED.");
            }

            UserUtils.saveDeveloperStatus(getActivity().getApplicationContext(), isDeveloper);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        if (preference.getKey().equals("pref_export_database")) {

            Log.d(TAG, "User tapped export database menu");

            try {
                StorageUtils.exportDatabase(
                        getActivity().getApplicationContext(),
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/" + Config.DATABASE_NAME);

                Toaster.showLong(getActivity().getApplicationContext(), R.string.settings_export_database_successful);

            } catch (IOException e) {
                Log.e(TAG, "Cannot export database to public folder. Error: ", e);
                Toaster.showLong(getActivity().getApplicationContext(), R.string.settings_export_database_failed);
            }
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }
}
