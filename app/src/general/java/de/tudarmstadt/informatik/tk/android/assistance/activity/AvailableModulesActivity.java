package de.tudarmstadt.informatik.tk.android.assistance.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
import de.tudarmstadt.informatik.tk.android.assistance.event.ModuleInstallEvent;
import de.tudarmstadt.informatik.tk.android.assistance.event.ModuleShowMoreInfoEvent;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.endpoint.ModuleEndpoint;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.module.AvailableModuleResponse;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.module.ModuleCapabilityResponse;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.module.ToggleModuleRequest;
import de.tudarmstadt.informatik.tk.android.assistance.util.ConverterUtils;
import de.tudarmstadt.informatik.tk.android.assistance.util.PreferencesUtils;
import de.tudarmstadt.informatik.tk.android.assistance.util.Toaster;
import de.tudarmstadt.informatik.tk.android.assistance.util.UserUtils;
import de.tudarmstadt.informatik.tk.android.assistance.view.CardView;
import de.tudarmstadt.informatik.tk.android.kraken.db.DbModule;
import de.tudarmstadt.informatik.tk.android.kraken.db.DbModuleCapability;
import de.tudarmstadt.informatik.tk.android.kraken.db.DbModuleInstallation;
import de.tudarmstadt.informatik.tk.android.kraken.db.DbUser;
import de.tudarmstadt.informatik.tk.android.kraken.model.api.endpoint.EndpointGenerator;
import de.tudarmstadt.informatik.tk.android.kraken.model.api.error.ErrorResponse;
import de.tudarmstadt.informatik.tk.android.kraken.provider.DbProvider;
import de.tudarmstadt.informatik.tk.android.kraken.provider.HarvesterServiceProvider;
import de.tudarmstadt.informatik.tk.android.kraken.util.DateUtils;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.CardThumbnail;
import it.gmariotti.cardslib.library.view.CardListView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Shows a list of available assistance modules
 *
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 28.06.2015
 */
public class AvailableModulesActivity extends AppCompatActivity {

    private static final String TAG = AvailableModulesActivity.class.getSimpleName();

    private DbProvider dbProvider;

    protected Toolbar mToolbar;

    protected CardListView mModuleList;

    protected SwipeRefreshLayout mSwipeRefreshLayout;

    private Map<String, AvailableModuleResponse> mAvailableModuleResponses;

    private List<String> mActiveModules;

    private ArrayList<Card> mModuleCards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_modules);

        if (dbProvider == null) {
            dbProvider = DbProvider.getInstance(getApplicationContext());
        }

        mToolbar = ButterKnife.findById(this, R.id.toolbar);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mModuleList = ButterKnife.findById(this, R.id.module_list);
        mSwipeRefreshLayout = ButterKnife.findById(this, R.id.module_list_swipe_refresh_layout);

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
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        loadModules();
    }

    /**
     * Loads module list from db or in case its empty
     * loads it from server
     */
    private void loadModules() {

        String userEmail = UserUtils.getUserEmail(getApplicationContext());

        DbUser user = dbProvider.getUserByEmail(userEmail);

        if (user == null) {
            requestAvailableModules();
            return;
        }

        List<DbModule> userModules = user.getDbModuleList();


        // no modules was found -> request from server
        if (userModules.isEmpty()) {
            Log.d(TAG, "Module list not found in db. Requesting from server...");

            requestAvailableModules();

        } else {
            // there are modules were found -> populate a list

            mAvailableModuleResponses = new ArrayMap<>();

            ArrayList<Card> cards = new ArrayList<>();

            for (DbModule module : userModules) {

                CardView card = new CardView(getApplicationContext());
                CardHeader header = new CardHeader(this);

                header.setTitle(module.getTitle());
                card.setTitle(module.getDescriptionShort());
                card.addCardHeader(header);
                card.setModuleId(module.getPackageName());

                CardThumbnail thumb = new CardThumbnail(this);

                String logoUrl = module.getLogoUrl();

                if (logoUrl.isEmpty()) {
                    thumb.setDrawableResource(R.drawable.no_image);
                } else {
                    thumb.setUrlResource(logoUrl);
                }

                card.addCardThumbnail(thumb);
                cards.add(card);

                AvailableModuleResponse availableModule = ConverterUtils.convertModule(module);

                List<ModuleCapabilityResponse> reqCaps = new ArrayList<>();
                List<ModuleCapabilityResponse> optCaps = new ArrayList<>();

                List<DbModuleCapability> moduleCapabilities = module.getDbModuleCapabilityList();

                for (DbModuleCapability capability : moduleCapabilities) {

                    if (capability.getRequired()) {
                        reqCaps.add(ConverterUtils.convertModuleCapability(capability));
                    } else {
                        optCaps.add(ConverterUtils.convertModuleCapability(capability));
                    }
                }

                availableModule.setSensorsRequired(reqCaps);
                availableModule.setSensorsOptional(optCaps);

                // for easy access later on
                mAvailableModuleResponses.put(availableModule.getModulePackage(), availableModule);
            }

            if (mModuleList != null) {
                mModuleList.setAdapter(new CardArrayAdapter(this, cards));
            }
        }
    }

    /**
     * Request available modules from service
     */
    private void requestAvailableModules() {

        final String userToken = UserUtils.getUserToken(getApplicationContext());

        // calling api service
        final ModuleEndpoint moduleEndpoint = EndpointGenerator.getInstance(getApplicationContext()).create(ModuleEndpoint.class);
        moduleEndpoint.getAvailableModules(userToken, new Callback<List<AvailableModuleResponse>>() {

            /**
             * Successful HTTP response.
             *
             * @param availableModulesResponse
             * @param response
             */
            @Override
            public void success(final List<AvailableModuleResponse> availableModulesResponse, Response response) {

                if (availableModulesResponse != null && !availableModulesResponse.isEmpty()) {

                    Log.d(TAG, availableModulesResponse.toString());

                    // get list of already activated modules
                    moduleEndpoint.getActiveModules(userToken, new Callback<List<String>>() {

                        @Override
                        public void success(List<String> activeModules, Response response) {

                            mSwipeRefreshLayout.setRefreshing(false);

                            if (activeModules != null && !activeModules.isEmpty()) {

                                Log.d(TAG, activeModules.toString());

                                mActiveModules = activeModules;
                            }

                            processAvailableModules(availableModulesResponse);
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            showErrorMessages(TAG, error);
                            processAvailableModules(availableModulesResponse);
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    });

                } else {
                    // TODO: show no modules available
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }

            /**
             * Unsuccessful HTTP response due to network failure, non-2XX status code, or unexpected
             * exception.
             *
             * @param error
             */
            @Override
            public void failure(RetrofitError error) {
                showErrorMessages(TAG, error);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    /**
     * Populates and saves available modules
     *
     * @param availableModulesResponse
     */
    private void processAvailableModules(List<AvailableModuleResponse> availableModulesResponse) {

        // show list of modules to user
        populateAvailableModuleList(availableModulesResponse);

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });

        // save module information into db
        saveModulesIntoDb(availableModulesResponse);

        // start sensing service
        HarvesterServiceProvider.getInstance(getApplicationContext()).startSensingService();
    }

    /**
     * Saves module information into db
     *
     * @param availableModulesResponse
     */
    private void saveModulesIntoDb(List<AvailableModuleResponse> availableModulesResponse) {

        Log.d(TAG, "Saving modules into db...");

        String userEmail = UserUtils.getUserEmail(getApplicationContext());

        DbUser user = dbProvider.getUserByEmail(userEmail);

        for (AvailableModuleResponse availableModule : availableModulesResponse) {

            Log.d(TAG, availableModule.toString());

            DbModule module = ConverterUtils.convertModule(availableModule);
            module.setDbUser(user);

            long moduleId = dbProvider.insertModule(module);

            // check if that module was already installed
            // if so -> insert new module installation into db
            if (mActiveModules != null && !mActiveModules.isEmpty()) {
                createActiveModuleInstallation(user.getId(), moduleId, availableModule.getModulePackage());
            }

            List<ModuleCapabilityResponse> reqCaps = availableModule.getSensorsRequired();
            List<ModuleCapabilityResponse> optCaps = availableModule.getSensorsOptional();

            List<DbModuleCapability> modCaps = new ArrayList<>();

            if (reqCaps != null && !reqCaps.isEmpty()) {

                // process required capabilities
                for (ModuleCapabilityResponse cap : reqCaps) {

                    DbModuleCapability dbCap = ConverterUtils.convertModuleCapability(cap);

                    dbCap.setRequired(true);
                    dbCap.setModuleId(moduleId);

                    modCaps.add(dbCap);
                }
            }

            if (optCaps != null && !optCaps.isEmpty()) {

                // process optional capabilities
                for (ModuleCapabilityResponse cap : optCaps) {

                    DbModuleCapability dbCap = ConverterUtils.convertModuleCapability(cap);

                    dbCap.setRequired(false);
                    dbCap.setModuleId(moduleId);

                    modCaps.add(dbCap);
                }
            }

            // insert entries
            if (!modCaps.isEmpty()) {
                dbProvider.insertModuleCapabilities(modCaps);
            }
        }

        Log.d(TAG, "Finished saving modules into db.");
    }

    /**
     * Inserts new module installation
     *
     * @param userId
     * @param moduleId
     * @param modulePackage
     */
    private void createActiveModuleInstallation(Long userId, long moduleId, String modulePackage) {

        boolean entryWasInserted = false;

        for (String activeModule : mActiveModules) {
            if (activeModule.equals(modulePackage)) {

                // check for existing installation
                // if so -> just activate it
                DbModuleInstallation moduleInstallation = dbProvider
                        .getModuleInstallationForModuleByUserId(userId, moduleId);

                if (moduleInstallation == null) {

                    moduleInstallation = new DbModuleInstallation();

                    moduleInstallation.setActive(true);
                    moduleInstallation.setModuleId(moduleId);
                    moduleInstallation.setUserId(userId);
                    moduleInstallation.setCreated(DateUtils.dateToISO8601String(new Date(), Locale.getDefault()));

                    dbProvider.insertModuleInstallation(moduleInstallation);

                } else {

                    moduleInstallation.setActive(true);

                    dbProvider.updateModuleInstallation(moduleInstallation);
                }

                entryWasInserted = true;

                // start monitoring service
                HarvesterServiceProvider service = HarvesterServiceProvider.getInstance(getApplicationContext());
                service.startSensingService();

                break;
            }
        }

        // immediately finish and proceed to main activity
        if (entryWasInserted) {

            Intent mainIntent = new Intent(this, MainActivity.class);
            startActivity(mainIntent);
            finish();
        }
    }

    /**
     * Show available modules to user
     *
     * @param availableModulesResponse
     */
    private void populateAvailableModuleList(List<AvailableModuleResponse> availableModulesResponse) {

        mAvailableModuleResponses = new ArrayMap<>();

        if (mModuleCards == null) {
            mModuleCards = new ArrayList<>();
        }

        for (AvailableModuleResponse module : availableModulesResponse) {

            Log.d(TAG, module.toString());

            CardView card = new CardView(getApplicationContext());
            CardHeader header = new CardHeader(this);

            header.setTitle(module.getTitle());
            card.setTitle(module.getDescriptionShort());
            card.addCardHeader(header);
            card.setModuleId(module.getModulePackage());

            CardThumbnail thumb = new CardThumbnail(this);

            String logoUrl = module.getLogo();

            if (logoUrl.isEmpty()) {
                thumb.setDrawableResource(R.drawable.no_image);
            } else {
                thumb.setUrlResource(logoUrl);
            }

            card.addCardThumbnail(thumb);

            mModuleCards.add(card);

            // for easy access later on
            mAvailableModuleResponses.put(module.getModulePackage(), module);
        }

        CardArrayAdapter mCardArrayAdapter = new CardArrayAdapter(this, mModuleCards);

        if (mModuleList != null) {
            mModuleList.setAdapter(mCardArrayAdapter);
        }
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

        final AvailableModuleResponse selectedModule = mAvailableModuleResponses.get(moduleId);

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

        AvailableModuleResponse selectedModule = mAvailableModuleResponses.get(moduleId);

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
     * @param modulePackageName
     */
    private void installModule(final String modulePackageName) {

        Log.d(TAG, "Installation of a module " + modulePackageName + " has started...");
        Log.d(TAG, "Requesting service...");

        String userToken = UserUtils.getUserToken(getApplicationContext());

        ToggleModuleRequest toggleModuleRequest = new ToggleModuleRequest();
        toggleModuleRequest.setModuleId(modulePackageName);

        ModuleEndpoint moduleEndpoint = EndpointGenerator.getInstance(getApplicationContext()).create(ModuleEndpoint.class);
        moduleEndpoint.activateModule(userToken, toggleModuleRequest, new Callback<Void>() {

            @Override
            public void success(Void aVoid, Response response) {

                if (response.getStatus() == 200 || response.getStatus() == 204) {
                    Log.d(TAG, "Module is activated!");
                    saveModuleInstallationInDb(modulePackageName);
                    Log.d(TAG, "Installation has finished!");
                } else {
                    Log.d(TAG, "FAIL: service responded with code: " + response.getStatus());
                }
            }

            @Override
            public void failure(RetrofitError error) {
                showErrorMessages(TAG, error);

                Log.d(TAG, "Installation has failed!");
            }
        });
    }

    /**
     * Saves module installations status on device
     */
    private void saveModuleInstallationInDb(final String modulePackageName) {

        String userEmail = UserUtils.getUserEmail(getApplicationContext());

        DbUser user = dbProvider.getUserByEmail(userEmail);

        if (user == null) {
            Log.d(TAG, "Installation cancelled: user is null");
            return;
        }

        DbModule module = dbProvider.getModuleByPackageIdUserId(modulePackageName, user.getId());

        if (module == null) {

            Log.d(TAG, "Installation cancelled: now such module found in db!");
            return;
        }

        // check module is already installed
        DbModuleInstallation moduleInstallation = dbProvider
                .getModuleInstallationForModuleByUserId(user.getId(), module.getId());

        if (moduleInstallation == null) {

            moduleInstallation = new DbModuleInstallation();

            moduleInstallation.setModuleId(module.getId());
            moduleInstallation.setUserId(user.getId());
            moduleInstallation.setActive(true);
            moduleInstallation.setCreated(DateUtils.dateToISO8601String(new Date(), Locale.getDefault()));
        } else {
            // activate existing module installation
            moduleInstallation.setActive(true);
        }

        Long installId = dbProvider.insertModuleInstallation(moduleInstallation);

        if (installId != null) {

            UserUtils.saveUserHasModules(getApplicationContext(), true);
            Toaster.showLong(getApplicationContext(), R.string.module_installation_successful);

        } else {

            Toaster.showLong(getApplicationContext(), R.string.module_installation_unsuccessful);
        }

        Log.d(TAG, "Installation id: " + installId);

        // start monitoring service
        HarvesterServiceProvider service = HarvesterServiceProvider.getInstance(getApplicationContext());
        service.startSensingService();

    }

    @Override
    protected void onDestroy() {
        ButterKnife.unbind(this);

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }

        Log.d(TAG, "onDestroy -> unbound resources");
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:

                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Processes error response from server
     *
     * @param TAG
     * @param retrofitError
     */
    protected void showErrorMessages(String TAG, RetrofitError retrofitError) {

        Response response = retrofitError.getResponse();

        if (response != null) {

            int httpCode = response.getStatus();

            switch (httpCode) {
                case 400:
                    ErrorResponse errorResponse = (ErrorResponse) retrofitError.getBodyAs(ErrorResponse.class);
                    errorResponse.setStatusCode(httpCode);

                    Integer apiResponseCode = errorResponse.getCode();
                    String apiMessage = errorResponse.getMessage();
                    int httpResponseCode = errorResponse.getStatusCode();

                    Log.d(TAG, "Response status: " + httpResponseCode);
                    Log.d(TAG, "Response code: " + apiResponseCode);
                    Log.d(TAG, "Response message: " + apiMessage);

                    break;
                case 401:
                    Toaster.showLong(getApplicationContext(), R.string.error_user_login_not_valid);
                    PreferencesUtils.clearUserCredentials(getApplicationContext());
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                    break;
                case 404:
                    Toaster.showLong(getApplicationContext(), R.string.error_service_not_available);
                    break;
                case 503:
                    Toaster.showLong(getApplicationContext(), R.string.error_server_temporary_unavailable);
                    break;
                default:
                    Toaster.showLong(getApplicationContext(), R.string.error_unknown);
                    break;
            }
        } else {
            Toaster.showLong(getApplicationContext(), R.string.error_service_not_available);
        }
    }
}
