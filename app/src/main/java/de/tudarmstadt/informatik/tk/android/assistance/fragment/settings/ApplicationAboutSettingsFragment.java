package de.tudarmstadt.informatik.tk.android.assistance.fragment.settings;

import android.app.AlertDialog;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.widget.Toolbar;

import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.activity.SettingsActivity;

/**
 * Created by Wladimir Schmidt on 29.06.2015.
 */
public class ApplicationAboutSettingsFragment extends PreferenceFragment {

    private static final String TAG = ApplicationAboutSettingsFragment.class.getSimpleName();

    private Toolbar mParentToolbar;

    private static Preference.OnPreferenceClickListener aboutClickHandler;

    private AlertDialog.Builder builder;

    public ApplicationAboutSettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preference_app_about);

        mParentToolbar = ((SettingsActivity) getActivity()).getToolBar();
        mParentToolbar.setTitle(R.string.settings_about_title);

        Preference aboutPref = findPreference("pref_about_app");

        if (aboutClickHandler == null) {
            aboutClickHandler = new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    showAboutInformation();
                    return false;
                }
            };
        }

        aboutPref.setOnPreferenceClickListener(aboutClickHandler);
    }

    /**
     * Shows about an app information
     */
    private void showAboutInformation() {

        if (builder == null) {
            builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("This an about dialog!");
            builder.create();
        }

        if (!getActivity().isFinishing()) {
            builder.show();
        }
    }
}
