package de.tudarmstadt.informatik.tk.android.assistance.fragment.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.widget.Toolbar;

import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.activity.SettingsActivity;

/**
 * Created by Wladimir Schmidt on 29.06.2015.
 */
public class ApplicationSettingsFragment extends PreferenceFragment {

    private static final String TAG = ApplicationSettingsFragment.class.getSimpleName();

    private Toolbar mParentToolbar;

    public ApplicationSettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preference_application);

        mParentToolbar = ((SettingsActivity) getActivity()).getToolBar();
        mParentToolbar.setTitle(R.string.settings_header_application_title);
    }
}
