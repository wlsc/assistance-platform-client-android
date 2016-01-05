package de.tudarmstadt.informatik.tk.android.assistance.fragment.settings;

import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import de.tudarmstadt.informatik.tk.android.assistance.BuildConfig;
import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.activity.SettingsActivity;
import de.tudarmstadt.informatik.tk.android.assistance.notification.Toaster;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.logger.Log;
import de.tudarmstadt.informatik.tk.android.assistance.Constants;
import de.tudarmstadt.informatik.tk.android.assistance.util.PreferenceUtils;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 29.06.2015
 */
public class AboutSettingsFragment extends PreferenceFragment {

    private static final String TAG = AboutSettingsFragment.class.getSimpleName();

    private Toolbar mParentToolbar;

    private static Preference.OnPreferenceClickListener aboutClickHandler;
    private static Preference.OnPreferenceClickListener buildNumberClickHandler;

    private AlertDialog dialog;

    private int beDeveloperCounter;

    private Toast toast;

    private Preference aboutPref;
    private Preference appVersionPref;
    private Preference buildNumberPref;

    public AboutSettingsFragment() {
        beDeveloperCounter = 0;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preference_app_about);

        mParentToolbar = ((SettingsActivity) getActivity()).getToolBar();
        mParentToolbar.setTitle(R.string.settings_about_title);

        aboutPref = findPreference("pref_about_app");
        appVersionPref = findPreference("pref_app_version");
        buildNumberPref = findPreference("pref_build_number");

        addSettingsActions();

        // set app build number and version
        appVersionPref.setSummary(BuildConfig.VERSION_NAME);

        String buildCodeStr = String.valueOf(BuildConfig.VERSION_CODE);

        if (buildCodeStr.length() == 1) {
            buildCodeStr = "000" + buildCodeStr;
        }

        if (buildCodeStr.length() == 2) {
            buildCodeStr = "00" + buildCodeStr;
        }

        if (buildCodeStr.length() == 3) {
            buildCodeStr = '0' + buildCodeStr;
        }

        buildNumberPref.setSummary(buildCodeStr);
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

        if (aboutClickHandler == null) {

            aboutClickHandler = preference -> {
                showAboutInformation();
                return false;
            };
        }

        if (buildNumberClickHandler == null) {

            buildNumberClickHandler = preference -> processBuildButton();
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

        new Handler().postDelayed(() -> beDeveloperCounter = 0, Constants.BACK_BUTTON_DELAY_MILLIS);

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

            LayoutInflater inflater = getActivity().getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_about_app, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(getString(R.string.app_name) + " v" + BuildConfig.VERSION_NAME);
            builder.setView(dialogView);
            builder.setPositiveButton(R.string.button_ok_text, null);

            // DEVELOPERS
            TextView devEmail1 = ButterKnife.findById(dialogView, R.id.settings_about_dialog_developer_email_1);
            devEmail1.setMovementMethod(LinkMovementMethod.getInstance());

            TextView devWebsite1 = ButterKnife.findById(dialogView, R.id.settings_about_dialog_developer_website_1);
            devWebsite1.setMovementMethod(LinkMovementMethod.getInstance());

            // SUPERVISORS
            TextView supervisorEmail1 = ButterKnife.findById(dialogView, R.id.settings_about_dialog_supervisor_email_1);
            supervisorEmail1.setMovementMethod(LinkMovementMethod.getInstance());

            TextView supervisorWebsite1 = ButterKnife.findById(dialogView, R.id.settings_about_dialog_supervisor_website_1);
            supervisorWebsite1.setMovementMethod(LinkMovementMethod.getInstance());

            TextView supervisorEmail2 = ButterKnife.findById(dialogView, R.id.settings_about_dialog_supervisor_email_2);
            supervisorEmail2.setMovementMethod(LinkMovementMethod.getInstance());

            TextView supervisorWebsite2 = ButterKnife.findById(dialogView, R.id.settings_about_dialog_supervisor_website_2);
            supervisorWebsite2.setMovementMethod(LinkMovementMethod.getInstance());

            TextView supervisorEmail3 = ButterKnife.findById(dialogView, R.id.settings_about_dialog_supervisor_email_3);
            supervisorEmail3.setMovementMethod(LinkMovementMethod.getInstance());

            // FOOTER
            TextView footer = ButterKnife.findById(dialogView, R.id.settings_about_dialog_tk_lab_footer);
            footer.setMovementMethod(LinkMovementMethod.getInstance());

            dialog = builder.create();
        }

        if (!getActivity().isFinishing()) {
            dialog.show();
        }
    }
}
