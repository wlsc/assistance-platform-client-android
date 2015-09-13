package de.tudarmstadt.informatik.tk.android.assistance.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pkmmte.view.CircularImageView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.activity.AvailableModulesActivity;
import de.tudarmstadt.informatik.tk.android.assistance.activity.LoginActivity;
import de.tudarmstadt.informatik.tk.android.assistance.activity.SettingsActivity;
import de.tudarmstadt.informatik.tk.android.assistance.adapter.DrawerAdapter;
import de.tudarmstadt.informatik.tk.android.assistance.event.DrawerUpdateEvent;
import de.tudarmstadt.informatik.tk.android.assistance.handler.DrawerHandler;
import de.tudarmstadt.informatik.tk.android.assistance.model.item.DrawerItem;
import de.tudarmstadt.informatik.tk.android.assistance.util.Constants;
import de.tudarmstadt.informatik.tk.android.assistance.util.UserUtils;
import de.tudarmstadt.informatik.tk.android.kraken.db.DatabaseManager;
import de.tudarmstadt.informatik.tk.android.kraken.db.Module;
import de.tudarmstadt.informatik.tk.android.kraken.db.ModuleInstallation;
import de.tudarmstadt.informatik.tk.android.kraken.db.User;
import de.tudarmstadt.informatik.tk.android.kraken.db.UserDao;

/**
 * Fragment used for managing interactions and presentation of a navigation drawer
 *
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 28.06.2015
 */
public class DrawerFragment extends Fragment implements DrawerHandler {

    private static final String TAG = DrawerFragment.class.getSimpleName();

    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private DrawerHandler mCallbacks;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mActionBarDrawerToggle;

    private DrawerLayout mDrawerLayout;

    @Bind(R.id.drawerList)
    protected RecyclerView mDrawerList;

    @Bind(R.id.drawer_no_modules)
    protected TextView mDrawerNoModules;

    private View mFragmentContainerView;

    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    @Bind(R.id.imgAvatar)
    protected CircularImageView userPicView;

    @Bind(R.id.txtUserEmail)
    protected TextView userEmailView;

    @Bind(R.id.txtUsername)
    protected TextView usernameView;

    private static List<DrawerItem> navigationItems;

    private UserDao userDao;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        mUserLearnedDrawer = UserUtils.getUserHasLearnedDrawer(getActivity().getApplicationContext());

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(Constants.STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }

        EventBus.getDefault().register(this);
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

        if (navigationItems == null) {
            navigationItems = new ArrayList<>();
        }

        if (navigationItems.size() == 0) {
            // show no modules selected
            mDrawerList.setVisibility(View.GONE);
            mDrawerNoModules.setVisibility(View.VISIBLE);
        } else {
            mDrawerList.setVisibility(View.VISIBLE);
            mDrawerNoModules.setVisibility(View.GONE);
        }

        Log.d(TAG, "Drawer items size: " + navigationItems.size());

        DrawerAdapter adapter = new DrawerAdapter(getActivity().getApplicationContext(), navigationItems);
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

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param drawerLayout
     * @param toolbar      The Toolbar of the activity.
     */
    public void setup(DrawerLayout drawerLayout, final Toolbar toolbar) {

        ButterKnife.bind(getActivity());

        mDrawerLayout = drawerLayout;
        mFragmentContainerView = getActivity().findViewById(R.id.drawer_fragment);

        mDrawerLayout.setStatusBarBackgroundColor(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.myPrimaryColor700));

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
                    UserUtils.saveUserHasLearnedDrawer(getActivity().getApplicationContext(), true);
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
            this.mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state.
        this.mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mActionBarDrawerToggle.syncState();
            }
        });

        this.mDrawerLayout.setDrawerListener(mActionBarDrawerToggle);

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
                Log.d(TAG, "User logged out");

                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                getActivity().finish();
                break;
            case R.id.settings:
                Log.d(TAG, "User left settings activity");
                updateDrawer();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    /**
     * Updates complete navigation drawer information
     */
    public void updateDrawer() {

        updateDrawerHeader();
        updateDrawerBody();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallbacks = (DrawerHandler) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement DrawerHandler!");
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
        startActivity(intent);
    }

    @OnClick(R.id.settings)
    protected void onSettingsClicked() {
        launchSettings();
    }

    /**
     * Starts settings activity
     */
    private void launchSettings() {
        Log.d(TAG, "Launched settings");

        Intent intent = new Intent(getActivity(), SettingsActivity.class);
        startActivityForResult(intent, R.id.settings);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (userDao == null) {
            userDao = DatabaseManager.getInstance(getActivity().getApplicationContext()).getDaoSession().getUserDao();
        }
    }

    /**
     * Process drawer update procedure and refresh the drawer with new information
     *
     * @param event
     */
    public void onEvent(DrawerUpdateEvent event) {
        Log.d(TAG, "Received drawer update event");

        UserUtils.saveUserEmail(getActivity().getApplicationContext(), event.getUserEmail());

        updateDrawerBody();

        Log.d(TAG, "Finished processing drawer update event!");
    }

    /**
     * Updates navigation drawer list of active modules
     */
    public void updateDrawerBody() {

        String userEmail = UserUtils.getUserEmail(getActivity().getApplicationContext());

        if (userEmail.isEmpty()) {
            return;
        }

        if (userDao == null) {
            userDao = DatabaseManager.getInstance(getActivity().getApplicationContext()).getDaoSession().getUserDao();
        }

        User user = userDao
                .queryBuilder()
                .where(UserDao.Properties.PrimaryEmail.eq(userEmail))
                .limit(1)
                .build()
                .unique();

        if (user == null) {
            navigationItems = new ArrayList<>(0);
            return;
        }

        List<ModuleInstallation> userModules = user.getModuleInstallationList();

        if (userModules == null || userModules.isEmpty()) {
            navigationItems = new ArrayList<>(0);
            return;
        } else {
            navigationItems = new ArrayList<>();

            for (ModuleInstallation moduleInstalled : userModules) {

                if (!moduleInstalled.getActive()) {
                    userModules.remove(moduleInstalled);
                } else {

                    Module module = moduleInstalled.getModule();

                    if (module != null) {

                        navigationItems.add(new DrawerItem(module.getTitle(), module.getLogoUrl()));
                    }
                }
            }

            if (!navigationItems.isEmpty()) {

                DrawerAdapter adapter = new DrawerAdapter(getActivity().getApplicationContext(), navigationItems);
                adapter.setNavigationDrawerCallbacks(this);

                mDrawerList.setAdapter(adapter);
                mDrawerList.getAdapter().notifyDataSetChanged();

                mDrawerList.setVisibility(View.VISIBLE);
                mDrawerNoModules.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Updates navigation drawer header layout
     */
    public void updateDrawerHeader() {

        Context context = getActivity().getApplicationContext();

        String firstname = UserUtils.getUserFirstname(context);
        String lastname = UserUtils.getUserLastname(context);
        String email = UserUtils.getUserEmail(context);
        String userPicFilename = UserUtils.getUserPicFilename(context);

        // check for imports
        if (usernameView == null || userEmailView == null) {
            ButterKnife.bind(this, mFragmentContainerView);
        }

        usernameView.setText(firstname + " " + lastname);
        userEmailView.setText(email);
        userEmailView.setMovementMethod(LinkMovementMethod.getInstance());

        if (userPicFilename.isEmpty()) {

            Picasso.with(context)
                    .load(R.drawable.no_image)
                    .placeholder(R.drawable.no_image)
                    .into(userPicView);

        } else {

            File file = UserUtils.getUserPicture(context, userPicFilename);

            if (file != null && file.exists()) {

                Picasso.with(getActivity())
                        .load(file)
                        .placeholder(R.drawable.no_image)
                        .into(userPicView);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        EventBus.getDefault().unregister(this);
        userDao = null;
    }
}
