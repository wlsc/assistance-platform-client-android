package de.tudarmstadt.informatik.tk.android.assistance.activities;

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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.activities.common.DrawerActivity;
import de.tudarmstadt.informatik.tk.android.assistance.events.ModuleInstallEvent;
import de.tudarmstadt.informatik.tk.android.assistance.events.ModuleShowMoreInfoEvent;
import de.tudarmstadt.informatik.tk.android.assistance.handlers.DrawerHandler;
import de.tudarmstadt.informatik.tk.android.assistance.models.api.module.AvailableModuleResponse;
import de.tudarmstadt.informatik.tk.android.assistance.models.api.module.ModuleCapabilityResponse;
import de.tudarmstadt.informatik.tk.android.assistance.services.AssistanceService;
import de.tudarmstadt.informatik.tk.android.assistance.services.ServiceGenerator;
import de.tudarmstadt.informatik.tk.android.assistance.utils.ConverterUtils;
import de.tudarmstadt.informatik.tk.android.assistance.utils.Toaster;
import de.tudarmstadt.informatik.tk.android.assistance.utils.UserUtils;
import de.tudarmstadt.informatik.tk.android.assistance.views.CardView;
import de.tudarmstadt.informatik.tk.android.kraken.db.Module;
import de.tudarmstadt.informatik.tk.android.kraken.db.ModuleCapability;
import de.tudarmstadt.informatik.tk.android.kraken.db.ModuleCapabilityDao;
import de.tudarmstadt.informatik.tk.android.kraken.db.ModuleDao;
import de.tudarmstadt.informatik.tk.android.kraken.db.ModuleInstallation;
import de.tudarmstadt.informatik.tk.android.kraken.db.ModuleInstallationDao;
import de.tudarmstadt.informatik.tk.android.kraken.db.User;
import de.tudarmstadt.informatik.tk.android.kraken.db.UserDao;
import de.tudarmstadt.informatik.tk.android.kraken.utils.DatabaseManager;
import de.tudarmstadt.informatik.tk.android.kraken.utils.DateUtils;
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

    private Map<String, AvailableModuleResponse> availableModuleResponses;

    private UserDao userDao;

    private ModuleDao moduleDao;

    private ModuleCapabilityDao moduleCapabilityDao;

    private ModuleInstallationDao moduleInstallationDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean userHasModulesInstalled = UserUtils.isUserHasModules(getApplicationContext());

        // locking or unlocking drawer
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
                loadModules();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        mSwipeRefreshLayout.setRefreshing(true);

        // register this activity to events
        EventBus.getDefault().register(this);

        loadModules();
    }

    /**
     * Loads module list from db or in case its empty
     * loads it from server
     */
    private void loadModules() {

        if (userDao == null) {
            userDao = DatabaseManager.getInstance(getApplicationContext()).getDaoSession().getUserDao();
        }

        User user = userDao
                .queryBuilder()
                .where(UserDao.Properties.PrimaryEmail.eq(mUserEmail))
                .limit(1)
                .build()
                .unique();

        if (user == null) {
            requestAvailableModules();
            return;
        }

        UserUtils.saveCurrentUserId(getApplicationContext(), user.getId());

        if (moduleDao == null) {
            moduleDao = DatabaseManager.getInstance(getApplicationContext()).getDaoSession().getModuleDao();
        }

        List<Module> userModules = user.getModuleList();


        // no modules was found -> request from server
        if (userModules.isEmpty()) {
            Log.d(TAG, "Module list not found in db. Requesting from server...");

            requestAvailableModules();
        } else {
            // there are modules were found -> populate a list

            availableModuleResponses = new ArrayMap<>();

            ArrayList<Card> cards = new ArrayList<>();

            for (Module module : userModules) {

                CardView card = new CardView(getApplicationContext());
                CardHeader header = new CardHeader(this);

                header.setTitle(module.getTitle());
                card.setTitle(module.getDescription_short());
                card.addCardHeader(header);
                card.setModuleId(module.getPackage_name());

                CardThumbnail thumb = new CardThumbnail(this);

                String logoUrl = module.getLogo_url();

                if (logoUrl.isEmpty()) {
                    Log.d(TAG, "Logo URL: NO LOGO supplied");
                    thumb.setDrawableResource(R.drawable.no_image);
                } else {
                    Log.d(TAG, "Logo URL: " + logoUrl);
                    thumb.setUrlResource(logoUrl);
                }

                card.addCardThumbnail(thumb);
                cards.add(card);

                AvailableModuleResponse availableModule = ConverterUtils.convertModule(module);
                List<ModuleCapabilityResponse> reqCaps = new ArrayList<>();
                List<ModuleCapabilityResponse> optCaps = new ArrayList<>();

                List<ModuleCapability> moduleCapabilities = module.getModuleCapabilityList();

                for (ModuleCapability capability : moduleCapabilities) {

                    if (capability.getRequired()) {
                        reqCaps.add(ConverterUtils.convertModuleCapability(capability));
                    } else {
                        optCaps.add(ConverterUtils.convertModuleCapability(capability));
                    }
                }

                availableModule.setSensorsRequired(reqCaps);
                availableModule.setSensorsOptional(optCaps);

                // for easy access later on
                availableModuleResponses.put(availableModule.getModulePackage(), availableModule);
            }

            CardArrayAdapter mCardArrayAdapter = new CardArrayAdapter(this, cards);

            if (mModuleList != null) {
                mModuleList.setAdapter(mCardArrayAdapter);
            }
        }
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

                if (availableModulesResponse != null && !availableModulesResponse.isEmpty()) {

                    // show them to user
                    populateAvailableModuleList(availableModulesResponse);

                    // save module information into db
                    saveModulesIntoDb(availableModulesResponse);

                    mSwipeRefreshLayout.setRefreshing(false);

                } else {
                    // TODO: show no modules available
                }

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
     * Saves module information into db
     *
     * @param availableModulesResponse
     */
    private void saveModulesIntoDb(List<AvailableModuleResponse> availableModulesResponse) {

        Log.d(TAG, "Saving modules into db...");

        if (moduleCapabilityDao == null) {
            moduleCapabilityDao = DatabaseManager.getInstance(getApplicationContext()).getDaoSession().getModuleCapabilityDao();
        }

        User user = userDao
                .queryBuilder()
                .where(UserDao.Properties.PrimaryEmail.eq(mUserEmail))
                .limit(1)
                .build()
                .unique();

        if (moduleDao == null) {
            moduleDao = DatabaseManager.getInstance(getApplicationContext()).getDaoSession().getModuleDao();
        }

        for (AvailableModuleResponse availableModule : availableModulesResponse) {

            Log.d(TAG, "Inserting following...");
            Log.d(TAG, availableModule.toString());

            Module module = ConverterUtils.convertModule(availableModule);
            module.setUser(user);

            long moduleId = moduleDao.insert(module);

            List<ModuleCapabilityResponse> reqCaps = availableModule.getSensorsRequired();
            List<ModuleCapabilityResponse> optCaps = availableModule.getSensorsOptional();

            List<ModuleCapability> modCaps = new ArrayList<>();

            if (reqCaps != null && !reqCaps.isEmpty()) {

                // process required capabilities
                for (ModuleCapabilityResponse cap : reqCaps) {

                    ModuleCapability dbCap = ConverterUtils.convertModuleCapability(cap);

                    dbCap.setRequired(true);
                    dbCap.setModule_id(moduleId);

                    modCaps.add(dbCap);
                }
            }

            if (optCaps != null && !optCaps.isEmpty()) {

                // process optional capabilities
                for (ModuleCapabilityResponse cap : optCaps) {

                    ModuleCapability dbCap = ConverterUtils.convertModuleCapability(cap);

                    dbCap.setRequired(false);
                    dbCap.setModule_id(moduleId);

                    modCaps.add(dbCap);
                }
            }

            // insert entries
            if (!modCaps.isEmpty()) {
                moduleCapabilityDao.insertInTx(modCaps);
            }
        }

        Log.d(TAG, "Finished saving modules into db!");
    }

    /**
     * Show available modules to user
     *
     * @param availableModulesResponse
     */
    private void populateAvailableModuleList(List<AvailableModuleResponse> availableModulesResponse) {

        this.availableModuleResponses = new ArrayMap<>();
        ArrayList<Card> cards = new ArrayList<>();

        for (AvailableModuleResponse module : availableModulesResponse) {

            List<ModuleCapabilityResponse> moduleReqSensors = module.getSensorsRequired();
            List<ModuleCapabilityResponse> moduleOptSensors = module.getSensorsOptional();


            CardView card = new CardView(getApplicationContext());
            CardHeader header = new CardHeader(this);

            Log.d(TAG, module.toString());

            if (moduleReqSensors != null && !moduleReqSensors.isEmpty()) {

                Log.d(TAG, "Req. sensors:");

                for (ModuleCapabilityResponse capability : moduleReqSensors) {
                    Log.d(TAG, "Type: " + capability.getType());
                    Log.d(TAG, "Frequency: " + capability.getFrequency());
                }
            } else {
                Log.d(TAG, "Empty");
            }

            if (moduleOptSensors != null && !moduleOptSensors.isEmpty()) {

                Log.d(TAG, "Optional sensors:");

                for (ModuleCapabilityResponse capability : moduleOptSensors) {
                    Log.d(TAG, "Type: " + capability.getType());
                    Log.d(TAG, "Frequency: " + capability.getFrequency());
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
            this.availableModuleResponses.put(module.getModulePackage(), module);
        }

        CardArrayAdapter mCardArrayAdapter = new CardArrayAdapter(this, cards);

        if (mModuleList != null) {
            mModuleList.setAdapter(mCardArrayAdapter);
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

        showMoreModuleInformationDialog(event.getModuleId());
    }

    /**
     * Shows more information about an assistance module
     *
     * @param moduleId
     */
    private void showMoreModuleInformationDialog(String moduleId) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setInverseBackgroundForced(true);

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_dialog_more_info_module, null);
        dialogBuilder.setView(dialogView);

        final AvailableModuleResponse selectedModule = availableModuleResponses.get(moduleId);

        dialogBuilder.setPositiveButton(R.string.button_ok_text, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "User tapped more information about the " + selectedModule.getTitle() + " module");
            }
        });

        TextView moreInfoFull = ButterKnife.findById(dialogView, R.id.module_more_info);
        moreInfoFull.setText(selectedModule.getDescriptionFull());

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    /**
     * Shows a permission dialog to user
     * Each permission is used actually by a module
     *
     * @param moduleId
     */
    private void showPermissionDialog(final String moduleId) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setInverseBackgroundForced(true);

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_dialog_permissions, null);
        dialogBuilder.setView(dialogView);

        dialogBuilder.setPositiveButton(R.string.button_accept_text, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "User accepted module permissions.");

                installModule(moduleId);
            }
        });

        AvailableModuleResponse selectedModule = availableModuleResponses.get(moduleId);

        TextView title = ButterKnife.findById(dialogView, R.id.module_permission_title);
        title.setText(selectedModule.getTitle());

        CircularImageView imageView = ButterKnife.findById(dialogView, R.id.module_permission_icon);

        Picasso.with(this)
                .load(selectedModule.getLogo())
                .placeholder(R.drawable.no_image)
                .into(imageView);

        List<ModuleCapabilityResponse> requiredSensors = selectedModule.getSensorsRequired();
        List<ModuleCapabilityResponse> optionalSensors = selectedModule.getSensorsOptional();

        List<String> allModuleSensors = new ArrayList<>();

        if (requiredSensors != null) {
            for (ModuleCapabilityResponse capability : requiredSensors) {
                allModuleSensors.add(capability.getType());
            }
        }

        if (optionalSensors != null) {
            for (ModuleCapabilityResponse capability : optionalSensors) {
                allModuleSensors.add(capability.getType());
            }
        }

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

    /**
     * Saves information into db / install a module for user
     *
     * @param moduleId
     */
    private void installModule(String moduleId) {

        Log.d(TAG, "Installation of a module " + moduleId + " started...");

        User user = userDao
                .queryBuilder()
                .where(UserDao.Properties.PrimaryEmail.eq(mUserEmail))
                .limit(1)
                .build()
                .unique();

        if (moduleInstallationDao == null) {
            moduleInstallationDao = DatabaseManager.getInstance(getApplicationContext()).getDaoSession().getModuleInstallationDao();
        }

        Module module = moduleDao
                .queryBuilder()
                .where(ModuleDao.Properties.Package_name.eq(moduleId))
                .where(ModuleDao.Properties.User_id.eq(user.getId()))
                .limit(1)
                .build()
                .unique();

        ModuleInstallation moduleInstallation = new ModuleInstallation();

        moduleInstallation.setModule_id(module.getId());
        moduleInstallation.setUser_id(user.getId());
        moduleInstallation.setActive(true);
        moduleInstallation.setCreated(DateUtils.dateToISO8601String(new Date(), Locale.getDefault()));

        Long installId = moduleInstallationDao.insert(moduleInstallation);

        if (installId != null) {
            Toaster.showLong(getApplicationContext(), R.string.module_installation_successful);
        } else {
            Toaster.showLong(getApplicationContext(), R.string.module_installation_unsuccessful);
        }

        Log.d(TAG, "Installation of a module " + moduleId + " finished! Installation id: " + installId);
    }

    @Override
    protected void onDestroy() {
        ButterKnife.unbind(this);
        EventBus.getDefault().unregister(this);
        moduleDao = null;
        moduleInstallationDao = null;
        moduleCapabilityDao = null;
        userDao = null;
        Log.d(TAG, "onDestroy -> unbound resources");
        super.onDestroy();
    }
}
