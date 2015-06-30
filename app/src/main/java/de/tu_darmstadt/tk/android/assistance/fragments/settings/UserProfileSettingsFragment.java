package de.tu_darmstadt.tk.android.assistance.fragments.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import de.tu_darmstadt.tk.android.assistance.R;

/**
 * Created by Wladimir Schmidt on 29.06.2015.
 */
public class UserProfileSettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preference_user_profile);
    }
}
