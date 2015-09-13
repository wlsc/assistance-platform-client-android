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
import de.tudarmstadt.informatik.tk.android.assistance.event.DrawerUpdateEvent;
import de.tudarmstadt.informatik.tk.android.assistance.event.ModuleInstallEvent;
import de.tudarmstadt.informatik.tk.android.assistance.event.ModuleShowMoreInfoEvent;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.error.ErrorResponse;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.module.AvailableModuleResponse;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.module.ModuleCapabilityResponse;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.module.ToggleModuleRequest;
import de.tudarmstadt.informatik.tk.android.assistance.service.ModuleService;
import de.tudarmstadt.informatik.tk.android.assistance.service.ServiceGenerator;
import de.tudarmstadt.informatik.tk.android.assistance.util.ConverterUtils;
import de.tudarmstadt.informatik.tk.android.assistance.util.PreferencesUtils;
import de.tudarmstadt.informatik.tk.android.assistance.util.Toaster;
import de.tudarmstadt.informatik.tk.android.assistance.util.UserUtils;
import de.tudarmstadt.informatik.tk.android.assistance.view.CardView;
import de.tudarmstadt.informatik.tk.android.kraken.db.DatabaseManager;
import de.tudarmstadt.informatik.tk.android.kraken.db.Module;
import de.tudarmstadt.informatik.tk.android.kraken.db.ModuleCapability;
import de.tudarmstadt.informatik.tk.android.kraken.db.ModuleCapabilityDao;
import de.tudarmstadt.informatik.tk.android.kraken.db.ModuleDao;
import de.tudarmstadt.informatik.tk.android.kraken.db.ModuleInstallation;
import de.tudarmstadt.informatik.tk.android.kraken.db.ModuleInstallationDao;
import de.tudarmstadt.informatik.tk.android.kraken.db.User;
import de.tudarmstadt.informatik.tk.android.kraken.db.UserDao;
import de.tudarmstadt.informatik.tk.android.kraken.utils.DateUtils;
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

    protected Toolbar mToolbar;

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
        setContentView(R.layout.activity_available_modules);

        mToolbar = ButterKnife.findById(this, R.id.toolbar_actionbar);
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
        EventBus.getDefault().register(this);

        loadModules();
    }

    /**
     * Loads module list from db or in case its empty
     * loads it from server
     */
    private void loadModules() {

        String userEmail = UserUtils.getUserEmail(getApplicationContext());

        if (userDao == null) {
            userDao = DatabaseManager.getInstance(getApplicationContext()).getDaoSession().getUserDao();
        }

        User user = userDao
                .queryBuilder()
                .where(UserDao.Properties.PrimaryEmail.eq(userEmail))
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
                card.setTitle(module.getDescriptionShort());
                card.addCardHeader(header);
                card.setModuleId(module.getPackageName());

                CardThumbnail thumb = new CardThumbnail(this);

                String logoUrl = module.getLogoUrl();

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
     * Request available modules from service
     */
    private void requestAvailableModules() {

        String userToken = UserUtils.getUserToken(getApplicationContext());

        // calling api service
        ModuleService moduleService = ServiceGenerator.createService(ModuleService.class);
        moduleService.getAvailableModules(userToken, new Callback<List<AvailableModuleResponse>>() {

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

        String userEmail = UserUtils.getUserEmail(getApplicationContext());

        if (moduleCapabilityDao == null) {
            moduleCapabilityDao = DatabaseManager.getInstance(getApplicationContext()).getDaoSession().getModuleCapabilityDao();
        }

        User user = userDao
                .queryBuilder()
                .where(UserDao.Properties.PrimaryEmail.eq(userEmail))
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
                    dbCap.setModuleId(moduleId);

                    modCaps.add(dbCap);
                }
            }

            if (optCaps != null && !optCaps.isEmpty()) {

                // process optional capabilities
                for (ModuleCapabilityResponse cap : optCaps) {

                    ModuleCapability dbCap = ConverterUtils.convertModuleCapability(cap);

                    dbCap.setRequired(false);
                    dbCap.setModuleId(moduleId);

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

                String userEmail = UserUtils.getUserEmail(getApplicationContext());

                EventBus.getDefault().post(new DrawerUpdateEvent(userEmail));
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
    private void installModule(final String moduleId) {

        Log.d(TAG, "Installation of a module " + moduleId + " started...");
        Log.d(TAG, "Requesting service...");

        String userToken = UserUtils.getUserToken(getApplicationContext());

        ToggleModuleRequest toggleModuleRequest = new ToggleModuleRequest();
        toggleModuleRequest.setModuleId(moduleId);

        ModuleService moduleService = ServiceGenerator.createService(ModuleService.class);
        moduleService.activateModule(userToken, toggleModuleRequest, new Callback<Void>() {

            @Override
            public void success(Void aVoid, Response response) {
                if (response.getStatus() == 200) {
                    Log.d(TAG, "Module was activated!");
                    saveInstallationOnDevice(moduleId);
                    Log.d(TAG, "Installation of a module " + moduleId + " finished!");
                } else {
                    Log.d(TAG, "FAIL: service responded with code: " + response.getStatus());
                }
            }

            @Override
            public void failure(RetrofitError error) {
                showErrorMessages(TAG, error);

                Log.d(TAG, "Installation of a module " + moduleId + " has failed!");
            }
        });
    }

    /**
     * Saves module installations status on device
     */
    private void saveInstallationOnDevice(final String moduleId) {

        String userEmail = UserUtils.getUserEmail(getApplicationContext());

        User user = userDao
                .queryBuilder()
                .where(UserDao.Properties.PrimaryEmail.eq(userEmail))
                .limit(1)
                .build()
                .unique();

        if (moduleInstallationDao == null) {
            moduleInstallationDao = DatabaseManager.getInstance(getApplicationContext()).getDaoSession().getModuleInstallationDao();
        }

        Module module = moduleDao
                .queryBuilder()
                .where(ModuleDao.Properties.PackageName.eq(moduleId))
                .where(ModuleDao.Properties.UserId.eq(user.getId()))
                .limit(1)
                .build()
                .unique();

        ModuleInstallation moduleInstallation = new ModuleInstallation();

        moduleInstallation.setModuleId(module.getId());
        moduleInstallation.setUserId(user.getId());
        moduleInstallation.setActive(true);
        moduleInstallation.setCreated(DateUtils.dateToISO8601String(new Date(), Locale.getDefault()));

        Long installId = moduleInstallationDao.insert(moduleInstallation);

        if (installId != null) {
            Toaster.showLong(getApplicationContext(), R.string.module_installation_successful);
        } else {
            Toaster.showLong(getApplicationContext(), R.string.module_installation_unsuccessful);
        }

        Log.d(TAG, "Installation id: " + installId);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
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
