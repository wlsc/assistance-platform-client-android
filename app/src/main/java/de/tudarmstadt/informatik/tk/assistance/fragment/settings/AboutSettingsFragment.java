package de.tudarmstadt.informatik.tk.assistance.fragment.settings;

import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import butterknife.ButterKnife;
import de.tudarmstadt.informatik.tk.assistance.BuildConfig;
import de.tudarmstadt.informatik.tk.assistance.Constants;
import de.tudarmstadt.informatik.tk.assistance.R;
import de.tudarmstadt.informatik.tk.assistance.activity.SettingsActivity;
import de.tudarmstadt.informatik.tk.assistance.notification.Toaster;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.logger.Log;
import de.tudarmstadt.informatik.tk.assistance.util.PreferenceUtils;
import de.tudarmstadt.informatik.tk.assistance.util.UserUtils;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 29.06.2015
 */
public class AboutSettingsFragment extends PreferenceFragment {

    private static final String TAG = AboutSettingsFragment.class.getSimpleName();

    private Toolbar mParentToolbar;

    private static Preference.OnPreferenceClickListener aboutClickHandler;
    private static Preference.OnPreferenceClickListener legalClickHandler;
    private static Preference.OnPreferenceClickListener feedbackClickHandler;
    private static Preference.OnPreferenceClickListener buildNumberClickHandler;

    private AlertDialog aboutAppDialog;
    private AlertDialog legalDialog;

    private int beDeveloperCounter;

    private Preference aboutPref;
    private Preference legalPref;
    private Preference feedbackPref;
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
        legalPref = findPreference("pref_legal_information");
        feedbackPref = findPreference("pref_feedback");
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

        if (legalClickHandler == null) {

            legalClickHandler = preference -> {
                showLegalInfo();
                return false;
            };
        }

        if (buildNumberClickHandler == null) {
            buildNumberClickHandler = preference -> processBuildButton();
        }

        if (feedbackClickHandler == null) {
            feedbackClickHandler = preference -> {
                sendEmail();
                return false;
            };
        }

        aboutPref.setOnPreferenceClickListener(aboutClickHandler);
        legalPref.setOnPreferenceClickListener(legalClickHandler);
        feedbackPref.setOnPreferenceClickListener(feedbackClickHandler);
        buildNumberPref.setOnPreferenceClickListener(buildNumberClickHandler);
    }

    /**
     * Shows default
     */
    private void sendEmail() {

        if (UserUtils.isEMailClientExists(getActivity())) {

            ShareCompat.IntentBuilder.from(getActivity())
                    .setType("message/rfc822")
                    .addEmailTo(getString(R.string.user_feedback_url))
                    .setSubject(getString(R.string.user_feedback_subject))
                    .setText(getString(R.string.user_feedback_body))
                            //.setHtmlText(getString(R.string.user_feedback_body));
                    .setChooserTitle("Select an email app")
                    .startChooser();

        } else {
            Toaster.showLong(getActivity().getApplicationContext(), R.string.error_you_have_no_email_app);
        }
    }

    /**
     * Show legal info dialog
     */
    private void showLegalInfo() {

        Log.d(TAG, "User clicked on legal info");

        if (legalDialog == null) {

            AlertDialog.Builder builder = new AlertDialog.Builder(
                    getActivity(),
                    R.style.MyAppCompatAlertDialog);

            builder.setTitle(getString(R.string.settings_legal_title));
            builder.setMessage(getString(R.string.settings_legal_dialog_message));
            builder.setPositiveButton(R.string.button_ok, null);

            legalDialog = builder.create();
        }

        legalDialog.show();
    }

    /**
     * You need to tap N times to be a developer
     *
     * @return
     */
    private boolean processBuildButton() {

        // check that user already a developer
        boolean isDeveloper = PreferenceUtils.isUserDeveloper(getActivity());

        if (isDeveloper) {
            Toaster.showShort(getActivity(), R.string.settings_build_press_already_developer);
            return true;
        }

        beDeveloperCounter++;

        if (beDeveloperCounter > 9) {

            Log.d(TAG, "You are now a developer.");

            Toaster.showLong(getActivity(), R.string.settings_build_press_now_you_developer);
            PreferenceUtils.setDeveloperStatus(getActivity(), true);

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
        aboutAppDialog = null;
    }

    /**
     * Shows about an app information
     */
    private void showAboutInformation() {

        if (aboutAppDialog == null) {

            LayoutInflater inflater = getActivity().getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_about_app, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.MyAppCompatAlertDialog);

            builder.setTitle(getString(R.string.app_name) + " v" + BuildConfig.VERSION_NAME);
            builder.setView(dialogView);
            builder.setPositiveButton(R.string.button_ok, null);

            // DEVELOPERS
            TextView devEmail1 = ButterKnife.findById(dialogView, R.id.settings_about_dialog_developer_email_1);
            devEmail1.setMovementMethod(LinkMovementMethod.getInstance());

            TextView devWebsite1 = ButterKnife.findById(dialogView, R.id.settings_about_dialog_developer_website_1);
            devWebsite1.setMovementMethod(LinkMovementMethod.getInstance());

            // FOOTER
            TextView footer = ButterKnife.findById(dialogView, R.id.settings_about_dialog_tk_lab_footer);
            footer.setMovementMethod(LinkMovementMethod.getInstance());

            aboutAppDialog = builder.create();
        }

        if (!getActivity().isFinishing()) {
            aboutAppDialog.show();
        }
    }
}
