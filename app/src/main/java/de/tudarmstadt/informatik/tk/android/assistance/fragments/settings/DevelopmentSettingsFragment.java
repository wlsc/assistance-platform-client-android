package de.tudarmstadt.informatik.tk.android.assistance.fragments.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.activities.SettingsActivity;
import de.tudarmstadt.informatik.tk.android.assistance.utils.PreferencesUtils;

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
        mParentToolbar.setTitle(R.string.settings_header_development_title);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equalsIgnoreCase("pref_be_developer")) {

            boolean isDeveloper = sharedPreferences.getBoolean(key, false);

            if (isDeveloper) {
                Log.d(TAG, "Developer mode is ENABLED.");
            } else {
                Log.d(TAG, "Developer mode is DISABLED.");
            }

            PreferencesUtils.savePreference(getActivity().getApplicationContext(), key, isDeveloper);
        }
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
