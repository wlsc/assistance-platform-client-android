package de.tudarmstadt.informatik.tk.android.assistance.fragments.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.widget.Toolbar;

import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.activities.SettingsActivity;

/**
 * Created by Wladimir Schmidt on 29.06.2015.
 */
public class UserDeviceInfoSettingsFragment extends PreferenceFragment {

    private static final String TAG = UserDeviceInfoSettingsFragment.class.getSimpleName();

    private Toolbar mParentToolbar;

    public UserDeviceInfoSettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preference_user_device_info);

        mParentToolbar = ((SettingsActivity) getActivity()).getToolBar();
        mParentToolbar.setTitle(R.string.settings_header_user_device_title);
    }
}
