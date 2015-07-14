package de.tu_darmstadt.tk.android.assistance.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pkmmte.view.CircularImageView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.tu_darmstadt.tk.android.assistance.R;
import de.tu_darmstadt.tk.android.assistance.activities.AvailableModulesActivity;
import de.tu_darmstadt.tk.android.assistance.activities.LoginActivity;
import de.tu_darmstadt.tk.android.assistance.activities.SettingsActivity;
import de.tu_darmstadt.tk.android.assistance.adapter.DrawerAdapter;
import de.tu_darmstadt.tk.android.assistance.callbacks.DrawerCallback;
import de.tu_darmstadt.tk.android.assistance.models.items.DrawerItem;
import de.tu_darmstadt.tk.android.assistance.utils.Constants;
import de.tu_darmstadt.tk.android.assistance.utils.PreferencesUtils;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 */
public class DrawerFragment extends Fragment implements DrawerCallback {

    private String TAG = DrawerFragment.class.getSimpleName();

    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private DrawerCallback mCallbacks;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mActionBarDrawerToggle;

    private DrawerLayout mDrawerLayout;

    @Bind(R.id.drawerList)
    protected RecyclerView mDrawerList;

    private View mFragmentContainerView;

    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    @Bind(R.id.imgAvatar)
    protected CircularImageView userAvatar;

    @Bind(R.id.txtUserEmail)
    protected TextView txtUserEmail;

    @Bind(R.id.txtUsername)
    protected TextView txtUsername;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        mUserLearnedDrawer = PreferencesUtils.readFromPreferences(getActivity().getApplicationContext(), Constants.PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(Constants.STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_drawer, container, false);

        ButterKnife.bind(this, view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mDrawerList.setLayoutManager(layoutManager);
        mDrawerList.setHasFixedSize(true);

        List<DrawerItem> navigationItems = getMenu();

        DrawerAdapter adapter = new DrawerAdapter(navigationItems);
        adapter.setNavigationDrawerCallbacks(this);

        mDrawerList.setAdapter(adapter);

        selectItem(mCurrentSelectedPosition);

        return view;
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    public ActionBarDrawerToggle getActionBarDrawerToggle() {
        return mActionBarDrawerToggle;
    }

    public DrawerLayout getDrawerLayout() {
        return mDrawerLayout;
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        selectItem(position);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    public List<DrawerItem> getMenu() {

        List<DrawerItem> items = new ArrayList<DrawerItem>();

        Drawable item1 = null;

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            item1 = getResources().getDrawable(R.drawable.ic_menu_check, null);
        } else {
            item1 = getResources().getDrawable(R.drawable.ic_menu_check);
        }

        items.add(new DrawerItem("item 1", item1));

        return items;
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     * @param toolbar      The Toolbar of the activity.
     */
    public void setup(int fragmentId, DrawerLayout drawerLayout, final Toolbar toolbar) {

        ButterKnife.bind(getActivity());

        mFragmentContainerView = getActivity().findViewById(R.id.drawer_fragment);
        mDrawerLayout = drawerLayout;

        mDrawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.myPrimaryColor700));

        mActionBarDrawerToggle = new ActionBarDrawerToggle(getActivity(), mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

                if (!isAdded()) {
                    return;
                }

                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                if (!isAdded()) {
                    return;
                }

                if (!mUserLearnedDrawer) {
                    mUserLearnedDrawer = true;
                    PreferencesUtils.saveToPreferences(getActivity().getApplicationContext(), Constants.PREF_USER_LEARNED_DRAWER, true);
                }

                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                if (slideOffset < Constants.DRAWER_SLIDER_THRESHOLD) {
                    toolbar.setAlpha(1 - slideOffset);
                }
            }
        };

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mActionBarDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mActionBarDrawerToggle);

        ButterKnife.bind(this, mFragmentContainerView);
    }

    /**
     * Selects item on position
     *
     * @param position
     */
    private void selectItem(int position) {

        mCurrentSelectedPosition = position;
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(position);
        }
        ((DrawerAdapter) mDrawerList.getAdapter()).selectPosition(position);
    }

    public void openDrawer() {
        mDrawerLayout.openDrawer(mFragmentContainerView);
    }

    public void closeDrawer() {
        mDrawerLayout.closeDrawer(mFragmentContainerView);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case R.id.logout_settings:
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                getActivity().finish();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallbacks = (DrawerCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement DrawerCallback!");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(Constants.STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mActionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Updates user data
     *
     * @param user
     * @param email
     * @param userPic
     */
    public void updateUserData(String user, String email, Bitmap userPic) {

        txtUserEmail.setText(email);
        txtUserEmail.setMovementMethod(LinkMovementMethod.getInstance());
        txtUsername.setText(user);
    }

    public View getGoogleDrawer() {
        return ButterKnife.findById(mFragmentContainerView, R.id.googleDrawer);
    }

    @OnClick({R.id.imgAvatar, R.id.txtUserEmail, R.id.txtUsername})
    protected void onUserPicClicked() {
        launchSettings();
    }

    @OnClick(R.id.available_modules)
    protected void onAvailableModulesClicked() {
        Intent intent = new Intent(getActivity(), AvailableModulesActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        getActivity().finish();
    }

    @OnClick(R.id.settings)
    protected void onSettingsClicked() {
        launchSettings();
    }

    /**
     * Starts settings activity
     */
    private void launchSettings() {
        Intent intent = new Intent(getActivity(), SettingsActivity.class);
        startActivityForResult(intent, R.id.logout_settings);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
