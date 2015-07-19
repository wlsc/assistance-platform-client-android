package de.tu_darmstadt.tk.android.assistance.activities;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import de.tu_darmstadt.tk.android.assistance.R;
import de.tu_darmstadt.tk.android.assistance.activities.common.DrawerActivity;
import de.tu_darmstadt.tk.android.assistance.callbacks.DrawerCallback;
import de.tu_darmstadt.tk.android.assistance.models.http.response.AvailableModuleResponse;
import de.tu_darmstadt.tk.android.assistance.services.AssistanceService;
import de.tu_darmstadt.tk.android.assistance.services.ServiceGenerator;
import de.tu_darmstadt.tk.android.assistance.utils.UserUtils;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.CardThumbnail;
import it.gmariotti.cardslib.library.view.CardListView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class AvailableModulesActivity extends DrawerActivity implements DrawerCallback {

    private String TAG = AvailableModulesActivity.class.getSimpleName();

    protected CardListView mModuleList;

    protected SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Inflate this layout into drawer container
        getLayoutInflater().inflate(R.layout.activity_available_modules, frameLayout);

        mModuleList = ButterKnife.findById(this, R.id.module_list);
        mSwipeRefreshLayout = ButterKnife.findById(this, R.id.module_list_swipe_refresh_layout);

        // hide available modules menu item
        ButterKnife.findById(this, R.id.available_modules).setVisibility(View.GONE);

        setTitle(R.string.module_list_activity_title);

        mSwipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

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

        String userToken = UserUtils.getUserToken(getApplicationContext());

        // calling api service
        AssistanceService service = ServiceGenerator.createService(AssistanceService.class);
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

                if (logoUrl.isEmpty()) {
                    Log.d(TAG, "Logo URL: NO LOGO supplied");
                    thumb.setDrawableResource(R.drawable.no_user_pic);
                } else {
                    Log.d(TAG, "Logo URL: " + logoUrl);
                    thumb.setUrlResource(logoUrl);
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
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
        Log.d(TAG, "onDestroy -> unbound resources");
    }
}
