package de.tudarmstadt.informatik.tk.android.assistance.fragment.settings;

import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.activity.SettingsActivity;
import de.tudarmstadt.informatik.tk.android.assistance.util.Constants;
import de.tudarmstadt.informatik.tk.android.assistance.util.PreferenceUtils;
import de.tudarmstadt.informatik.tk.android.assistance.notification.Toaster;

/**
 * Created by Wladimir Schmidt on 29.06.2015.
 */
public class ApplicationAboutSettingsFragment extends PreferenceFragment {

    private static final String TAG = ApplicationAboutSettingsFragment.class.getSimpleName();

    private Toolbar mParentToolbar;

    private static Preference.OnPreferenceClickListener aboutClickHandler;
    private static Preference.OnPreferenceClickListener buildNumberClickHandler;

    private AlertDialog dialog;

    private int beDeveloperCounter;

    private Toast toast;

    public ApplicationAboutSettingsFragment() {
        beDeveloperCounter = 0;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preference_app_about);

        mParentToolbar = ((SettingsActivity) getActivity()).getToolBar();
        mParentToolbar.setTitle(R.string.settings_about_title);

        addSettingsActions();
    }

    @Override
    public void onResume() {
        super.onResume();

        addSettingsActions();
    }

    /**
     * Adds handlers to various settings headers
     */
    private void addSettingsActions() {

        Preference aboutPref = findPreference("pref_about_app");
        Preference buildNumberPref = findPreference("pref_build_number");

        if (aboutClickHandler == null) {

            aboutClickHandler = new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    showAboutInformation();
                    return false;
                }
            };
        }

        if (buildNumberClickHandler == null) {

            buildNumberClickHandler = new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    return processBuildButton();
                }
            };
        }

        aboutPref.setOnPreferenceClickListener(aboutClickHandler);
        buildNumberPref.setOnPreferenceClickListener(buildNumberClickHandler);
    }

    /**
     * You need to tap N times to be a developer
     *
     * @return
     */
    private boolean processBuildButton() {

        // check that user already a developer
        boolean isDeveloper = PreferenceUtils.isUserDeveloper(getActivity().getApplicationContext());

        if (isDeveloper) {
            Toaster.showShort(getActivity().getApplicationContext(), R.string.settings_build_press_already_developer);
            return true;
        }

        beDeveloperCounter++;

        if (toast != null) {
            toast.cancel();
        }

        toast = Toast.makeText(getActivity(), getString(R.string.settings_build_press_some_more, beDeveloperCounter), Toast.LENGTH_SHORT);
        toast.show();

        if (beDeveloperCounter > 9) {

            Log.d(TAG, "You are now a developer.");

            Toaster.showLong(getActivity(), R.string.settings_build_press_now_you_developer);
            PreferenceUtils.setDeveloperStatus(getActivity().getApplicationContext(), true);

            return true;
        }

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                beDeveloperCounter = 0;
            }
        }, Constants.BACK_BUTTON_DELAY_MILLIS);

        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        aboutClickHandler = null;
        buildNumberClickHandler = null;
        dialog = null;
    }

    /**
     * Shows about an app information
     */
    private void showAboutInformation() {

        if (dialog == null) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("This an about dialog!");

            dialog = builder.create();
        }

        if (!getActivity().isFinishing()) {
            dialog.show();
        }
    }
}
