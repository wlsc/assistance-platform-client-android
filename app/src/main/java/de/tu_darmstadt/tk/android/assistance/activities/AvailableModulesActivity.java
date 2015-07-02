package de.tu_darmstadt.tk.android.assistance.activities;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.tu_darmstadt.tk.android.assistance.R;
import de.tu_darmstadt.tk.android.assistance.activities.common.BaseActivity;
import de.tu_darmstadt.tk.android.assistance.callbacks.NavigationDrawerCallbacks;
import de.tu_darmstadt.tk.android.assistance.fragments.NavigationDrawerFragment;
import de.tu_darmstadt.tk.android.assistance.models.http.response.AvailableModuleResponse;
import de.tu_darmstadt.tk.android.assistance.models.http.response.ErrorResponse;
import de.tu_darmstadt.tk.android.assistance.services.AvailableModulesService;
import de.tu_darmstadt.tk.android.assistance.services.ServiceGenerator;
import de.tu_darmstadt.tk.android.assistance.utils.Toaster;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.CardThumbnail;
import it.gmariotti.cardslib.library.view.CardListView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class AvailableModulesActivity extends BaseActivity implements NavigationDrawerCallbacks {

    private String TAG = AvailableModulesActivity.class.getSimpleName();

    @Bind(R.id.module_list)
    protected CardListView mModuleList;

    @Bind(R.id.module_list_swipe_refresh_layout)
    protected SwipeRefreshLayout mSwipeRefreshLayout;

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private Toolbar mToolbar;
    private String mUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_modules);
        setTitle(R.string.module_list_activity_title);

        ButterKnife.bind(this);

        mUserEmail = getUserEmail();

        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);

        DrawerLayout drawerLayout = ButterKnife.findById(this, R.id.drawer);

        mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.fragment_drawer);

        mNavigationDrawerFragment.setup(R.id.fragment_drawer, drawerLayout, mToolbar);
        mNavigationDrawerFragment.setUserData("Wladimir Schmidt", mUserEmail, BitmapFactory.decodeResource(getResources(), R.drawable.no_user_pic));

        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.swipe_refresh_progress_1,
                R.color.swipe_refresh_progress_2,
                R.color.swipe_refresh_progress_3);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                requestAvailableModules();
            }
        });

        mSwipeRefreshLayout.setRefreshing(true);

        requestAvailableModules();
    }

    /**
     * Request available modules service
     */
    private void requestAvailableModules() {

        String userToken = getUserToken();

        // calling api service
        AvailableModulesService service = ServiceGenerator.createService(AvailableModulesService.class);
        service.getAvailableModules(userToken, new Callback<List<AvailableModuleResponse>>() {
            /**
             * Successful HTTP response.
             *
             * @param availableModuleResponses
             * @param response
             */
            @Override
            public void success(List<AvailableModuleResponse> availableModuleResponses, Response response) {

                populateAvailableModuleList(availableModuleResponses);

                mSwipeRefreshLayout.setRefreshing(false);

                Log.d(TAG, "successfully received available modules! size: " + availableModuleResponses.size());
            }

            /**
             * Unsuccessful HTTP response due to network failure, non-2XX status code, or unexpected
             * exception.
             *
             * @param error
             */
            @Override
            public void failure(RetrofitError error) {

                mSwipeRefreshLayout.setRefreshing(false);

                showErrorMessages(TAG, error);
            }
        });
    }

    /**
     * Show available modules to user
     *
     * @param availableModuleResponses
     */
    private void populateAvailableModuleList(List<AvailableModuleResponse> availableModuleResponses) {

        if (availableModuleResponses != null && !availableModuleResponses.isEmpty()) {

            ArrayList<Card> cards = new ArrayList<Card>();

            for (AvailableModuleResponse module : availableModuleResponses) {

                Card card = new Card(this);

                CardHeader header = new CardHeader(this);

                Log.d(TAG, "Module content");
                Log.d(TAG, "Title: " + module.getTitle());
                Log.d(TAG, "Short description: " + module.getDescriptionShort());

                header.setTitle(module.getTitle());
                card.setTitle(module.getDescriptionShort());
                card.addCardHeader(header);

                CardThumbnail thumb = new CardThumbnail(this);

                String logoUrl = module.getLogo();

                if (logoUrl != null && !logoUrl.isEmpty()) {
                    Log.d(TAG, "Logo URL: " + logoUrl);
                    thumb.setUrlResource(logoUrl);
                } else {
                    Log.d(TAG, "Logo URL: NO LOGO");
                    thumb.setDrawableResource(R.drawable.no_user_pic);
                }

                card.addCardThumbnail(thumb);

                cards.add(card);
            }

            CardArrayAdapter mCardArrayAdapter = new CardArrayAdapter(this, cards);

            if (mModuleList != null) {
                mModuleList.setAdapter(mCardArrayAdapter);
            }
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {

    }

    @Override
    protected void onStop() {
        super.onStop();
        ButterKnife.unbind(this);
    }
}
