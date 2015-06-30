package de.tu_darmstadt.tk.android.assistance.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.tu_darmstadt.tk.android.assistance.Config;
import de.tu_darmstadt.tk.android.assistance.R;
import de.tu_darmstadt.tk.android.assistance.fragments.settings.ApplicationAboutSettingsFragment;
import de.tu_darmstadt.tk.android.assistance.fragments.settings.ApplicationSettingsFragment;
import de.tu_darmstadt.tk.android.assistance.fragments.settings.DevelopmentSettingsFragment;
import de.tu_darmstadt.tk.android.assistance.fragments.settings.UserDeviceInfoSettingsFragment;
import de.tu_darmstadt.tk.android.assistance.fragments.settings.UserProfileSettingsFragment;
import de.tu_darmstadt.tk.android.assistance.utils.Constants;

/**
 * Core user settings activity
 */
public class SettingsActivity extends PreferenceActivity {

    @Bind(R.id.toolbar)
    protected Toolbar mToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ViewGroup root = ButterKnife.findById(this, android.R.id.content);
        View content = root.getChildAt(0);
        LinearLayout toolbarContainer = (LinearLayout) View.inflate(this, R.layout.activity_settings, null);

        root.removeAllViews();
        toolbarContainer.addView(content);
        root.addView(toolbarContainer);

        ButterKnife.bind(this, toolbarContainer);

        setTitle(R.string.settings_activity_title);

        mToolBar.setTitle(getTitle());
        mToolBar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
    }

    @OnClick(R.id.toolbar)
    protected void onBackClicked() {
        finish();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onBuildHeaders(List<Header> headers) {
        loadHeadersFromResource(R.xml.preference_headers, headers);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {

        boolean isFragmentKnown = false;

        /**
         * Preventing fragment injection vulnerability
         */
        if (ApplicationAboutSettingsFragment.class.getName().equals(fragmentName) ||
                ApplicationSettingsFragment.class.getName().equals(fragmentName) ||
                DevelopmentSettingsFragment.class.getName().equals(fragmentName) ||
                UserDeviceInfoSettingsFragment.class.getName().equals(fragmentName) ||
                UserProfileSettingsFragment.class.getName().equals(fragmentName)) {

            isFragmentKnown = true;
        }

        return isFragmentKnown;
    }

    @Override
    public void onHeaderClick(Header header, int position) {
        super.onHeaderClick(header, position);

        if (header.id == R.id.logout_settings) {
            doLogout();
        }
    }

    /**
     * Reset user token/email and log him out
     */
    private void doLogout() {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.edit()
                .remove(Constants.PREF_USER_TOKEN)
                .remove(Constants.PREF_USER_EMAIL)
                .apply();

        setResult(R.id.logout_settings);
        finish();
    }

    public Toolbar getToolBar() {
        return mToolBar;
    }

}
