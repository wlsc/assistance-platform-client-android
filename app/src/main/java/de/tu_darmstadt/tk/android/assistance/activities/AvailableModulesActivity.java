package de.tu_darmstadt.tk.android.assistance.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.pkmmte.view.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import de.tu_darmstadt.tk.android.assistance.R;
import de.tu_darmstadt.tk.android.assistance.activities.common.DrawerActivity;
import de.tu_darmstadt.tk.android.assistance.events.ModuleInstallEvent;
import de.tu_darmstadt.tk.android.assistance.events.ModuleShowMoreInfoEvent;
import de.tu_darmstadt.tk.android.assistance.handlers.DrawerHandler;
import de.tu_darmstadt.tk.android.assistance.models.cards.ModuleCard;
import de.tu_darmstadt.tk.android.assistance.models.api.module.AvailableModuleResponse;
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

public class AvailableModulesActivity extends DrawerActivity implements DrawerHandler {

    private String TAG = AvailableModulesActivity.class.getSimpleName();

    protected CardListView mModuleList;

    protected SwipeRefreshLayout mSwipeRefreshLayout;

    private Map<String, AvailableModuleResponse> availableModules;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean userHasModulesInstalled = UserUtils.isUserHasModules(getApplicationContext());

        if (userHasModulesInstalled) {
//            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
//            mDrawerFragment.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
        } else {
//            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
//            mDrawerFragment.getActionBarDrawerToggle().setDrawerIndicatorEnabled(false);
        }

        //Inflate this layout into drawer container
        getLayoutInflater().inflate(R.layout.activity_available_modules, mFrameLayout);

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

        EventBus.getDefault().register(this);

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
             * @param availableModulesResponse
             * @param response
             */
            @Override
            public void success(List<AvailableModuleResponse> availableModulesResponse, Response response) {

                populateAvailableModuleList(availableModulesResponse);

                mSwipeRefreshLayout.setRefreshing(false);

                Log.d(TAG, "successfully received available modules! size: " + availableModulesResponse.size());
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
     * @param availableModulesResponse
     */
    private void populateAvailableModuleList(List<AvailableModuleResponse> availableModulesResponse) {

        if (availableModulesResponse != null && !availableModulesResponse.isEmpty()) {

            availableModules = new ArrayMap<>();
            ArrayList<Card> cards = new ArrayList<>();

            for (AvailableModuleResponse module : availableModulesResponse) {

                String modulePackage = module.getModulePackage();
                String moduleTitle = module.getTitle();
                String moduleDescriptionFull = module.getDescriptionFull();
                String moduleDescriptionShort = module.getDescriptionShort();
                String moduleCopyright = module.getCopyright();
                List<String> moduleReqSensors = module.getSensorsRequired();
                List<String> moduleOptSensors = module.getSensorsOptional();


                ModuleCard card = new ModuleCard(getApplicationContext());
                CardHeader header = new CardHeader(this);

                Log.d(TAG, "Module content");
                Log.d(TAG, "Package: " + modulePackage);
                Log.d(TAG, "Title: " + moduleTitle);
                Log.d(TAG, "Full description: " + moduleDescriptionFull);
                Log.d(TAG, "Short description: " + moduleDescriptionShort);
                Log.d(TAG, "Copyright: " + moduleCopyright);

                if (moduleReqSensors != null && !moduleReqSensors.isEmpty()) {
                    Log.d(TAG, "Req. sensors:");
                    for (String sensor : moduleReqSensors) {
                        Log.d(TAG, sensor);
                    }
                } else {
                    Log.d(TAG, "Empty");
                }

                if (moduleOptSensors != null && !moduleOptSensors.isEmpty()) {
                    Log.d(TAG, "Optional sensors:");
                    for (String sensor : moduleOptSensors) {
                        Log.d(TAG, sensor);
                    }
                } else {
                    Log.d(TAG, "Empty");
                }

                header.setTitle(module.getTitle());
                card.setTitle(module.getDescriptionShort());
                card.addCardHeader(header);
                card.setModuleId(module.getModulePackage());

                CardThumbnail thumb = new CardThumbnail(this);

                String logoUrl = module.getLogo();

                if (logoUrl.isEmpty()) {
                    Log.d(TAG, "Logo URL: NO LOGO supplied");
                    thumb.setDrawableResource(R.drawable.no_image);
                } else {
                    Log.d(TAG, "Logo URL: " + logoUrl);
                    thumb.setUrlResource(logoUrl);
                }

                card.addCardThumbnail(thumb);

                cards.add(card);

                // for easy access later on
                availableModules.put(module.getModulePackage(), module);
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

    /**
     * On module install event
     *
     * @param event
     */
    public void onEvent(ModuleInstallEvent event) {
        Log.d(TAG, "Received installation event. Module id: " + event.getModuleId());

        showPermissionDialog(event.getModuleId());
    }

    /**
     * On module show more info event
     *
     * @param event
     */
    public void onEvent(ModuleShowMoreInfoEvent event) {
        Log.d(TAG, "Received show more info event. Module id: " + event.getModuleId());
    }

    /**
     * Shows a permission dialog to user
     * Each permission is used actually by a module
     *
     * @param moduleId
     */
    private void showPermissionDialog(String moduleId) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setInverseBackgroundForced(true);

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_dialog_permissions, null);
        dialogBuilder.setView(dialogView);

        dialogBuilder.setPositiveButton(R.string.accept_text, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "User accepted module permissions.");
            }
        });

        AvailableModuleResponse selectedModule = availableModules.get(moduleId);

        TextView title = ButterKnife.findById(dialogView, R.id.module_permission_title);
        title.setText(selectedModule.getTitle());

        CircularImageView imageView = ButterKnife.findById(dialogView, R.id.module_permission_icon);

        Picasso.with(this)
                .load(selectedModule.getLogo())
                .placeholder(R.drawable.no_image)
                .into(imageView);

        List<String> requiredSensors = selectedModule.getSensorsRequired();
        List<String> optionalSensors = selectedModule.getSensorsOptional();

        List<String> allModuleSensors = new ArrayList<>();
        allModuleSensors.addAll(requiredSensors);
        allModuleSensors.addAll(optionalSensors);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                this,
                R.layout.permission_item,
                R.id.permission_item_title,
                allModuleSensors);

        ListView listView = ButterKnife.findById(dialogView, R.id.module_permission_list);
        listView.setAdapter(arrayAdapter);

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    @Override
    protected void onDestroy() {
        ButterKnife.unbind(this);
        EventBus.getDefault().unregister(this);
        Log.d(TAG, "onDestroy -> unbound resources");
        super.onDestroy();
    }
}
