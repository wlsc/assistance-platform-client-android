package de.tu_darmstadt.tk.android.assistance.activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.pkmmte.view.CircularImageView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.tu_darmstadt.tk.android.assistance.R;
import de.tu_darmstadt.tk.android.assistance.fragments.settings.ApplicationAboutSettingsFragment;
import de.tu_darmstadt.tk.android.assistance.fragments.settings.ApplicationSettingsFragment;
import de.tu_darmstadt.tk.android.assistance.fragments.settings.DevelopmentSettingsFragment;
import de.tu_darmstadt.tk.android.assistance.fragments.settings.SensorsListFragment;
import de.tu_darmstadt.tk.android.assistance.fragments.settings.UserDeviceInfoSettingsFragment;
import de.tu_darmstadt.tk.android.assistance.fragments.settings.UserProfileSettingsFragment;
import de.tu_darmstadt.tk.android.assistance.utils.CommonUtils;
import de.tu_darmstadt.tk.android.assistance.utils.PreferencesUtils;
import de.tu_darmstadt.tk.android.assistance.utils.Toaster;
import de.tu_darmstadt.tk.android.assistance.utils.UserUtils;

/**
 * Core user settings activity
 */
public class SettingsActivity extends PreferenceActivity {

    private static final String TAG = SettingsActivity.class.getSimpleName();

    private static final String IMAGE_TYPE_FILTER = "image/*";
    private static final String USER_PIC_NAME = "user_pic";

    private final String[] VALID_FRAGMENTS = {
            ApplicationAboutSettingsFragment.class.getName(),
            ApplicationSettingsFragment.class.getName(),
            DevelopmentSettingsFragment.class.getName(),
            UserDeviceInfoSettingsFragment.class.getName(),
            UserProfileSettingsFragment.class.getName(),
            SensorsListFragment.class.getName()
    };

    @Bind(R.id.toolbar)
    protected Toolbar mToolBar;

    public SettingsActivity() {
    }

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


        FragmentManager fm = getFragmentManager();
        fm.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {

            @Override
            public void onBackStackChanged() {
                if (getFragmentManager().getBackStackEntryCount() == 0) {
                    finish();
                }
            }
        });
    }

    @OnClick(R.id.toolbar)
    protected void onBackClicked() {
        onBackFromFragment();
        finish();
    }

    @Override
    public void onBackPressed() {
        onBackFromFragment();
        finish();
    }

    private void onBackFromFragment() {

    }

    @Override
    public void onBuildHeaders(List<Header> headers) {
        loadHeadersFromResource(R.xml.preference_headers, headers);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {

        /**
         * Preventing fragment injection vulnerability
         */
        for (String validFragmentName : VALID_FRAGMENTS) {
            if (validFragmentName.equals(fragmentName)) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
    }

    @Override
    public void onHeaderClick(Header header, int position) {
        super.onHeaderClick(header, position);

        switch ((int) header.id) {

            case R.id.logout_settings:
                doLogout();
                break;
        }
    }

    /*
    *   Starts intent to pick some image
     */
    public void pickImage() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(IMAGE_TYPE_FILTER);
        startActivityForResult(intent, R.id.userPhoto);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == R.id.userPhoto && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Toaster.showLong(this, R.string.error_select_new_user_photo);
                return;
            }

            String oldFilename = UserUtils.getUserPicFilename(getApplicationContext());
            Log.d(TAG, "old user pic filename: " + oldFilename);

            // process selected image and show it to user
            try {
                Uri uri = data.getData();

                InputStream inputStream = getApplicationContext().getContentResolver().openInputStream(uri);

                CommonUtils.saveFile(getApplicationContext(), uri, oldFilename);

                CircularImageView image = ButterKnife.findById(this, R.id.userPhoto);
                image.setImageDrawable(Drawable.createFromStream(inputStream, USER_PIC_NAME));

            } catch (FileNotFoundException e) {
                Log.e(TAG, "User pic file not found!");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
        Log.d(TAG, "onDestroy -> unbound resources");
    }

    /**
     * Reset user token/email and log him out
     */
    private void doLogout() {

        PreferencesUtils.clearUserCredentials(this);

        setResult(R.id.logout_settings);
        finish();
    }

    public Toolbar getToolBar() {
        return mToolBar;
    }
}
