package de.tudarmstadt.informatik.tk.assistance.fragment.settings;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v7.widget.Toolbar;

import de.tudarmstadt.informatik.tk.assistance.R.string;
import de.tudarmstadt.informatik.tk.assistance.R.xml;
import de.tudarmstadt.informatik.tk.assistance.activity.ModuleRunningSensorsActivity;
import de.tudarmstadt.informatik.tk.assistance.activity.SettingsActivity;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.logger.Log;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 29.06.2015
 */
public class AppSettingsFragment extends PreferenceFragment {

    private static final String TAG = AppSettingsFragment.class.getSimpleName();

    private Toolbar mParentToolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(xml.preference_application);

        mParentToolbar = ((SettingsActivity) getActivity()).getToolBar();

        if (mParentToolbar != null) {
            mParentToolbar.setTitle(string.settings_header_application_title);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        if ("pref_module_types_permissions".equals(preference.getKey())) {

            Log.d(TAG, "User clicked launch module types permission list view");

            Intent intent = new Intent(getActivity(), ModuleRunningSensorsActivity.class);
            getActivity().startActivity(intent);
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}