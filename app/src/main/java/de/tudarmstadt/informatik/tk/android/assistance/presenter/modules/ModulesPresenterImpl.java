package de.tudarmstadt.informatik.tk.android.assistance.presenter.modules;

import android.content.Context;
import android.support.v4.app.ActivityCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.tudarmstadt.informatik.tk.android.assistance.controller.modules.ModulesController;
import de.tudarmstadt.informatik.tk.android.assistance.controller.modules.ModulesControllerImpl;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnActiveModulesResponseHandler;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnAvailableModulesResponseHandler;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.dto.module.AvailableModuleResponseDto;
import de.tudarmstadt.informatik.tk.android.assistance.presenter.CommonPresenterImpl;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModule;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbUser;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.logger.Log;
import de.tudarmstadt.informatik.tk.android.assistance.util.Constants;
import de.tudarmstadt.informatik.tk.android.assistance.util.ConverterUtils;
import de.tudarmstadt.informatik.tk.android.assistance.util.LoginUtils;
import de.tudarmstadt.informatik.tk.android.assistance.util.PreferenceUtils;
import de.tudarmstadt.informatik.tk.android.assistance.view.ModulesView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 02.12.2015
 */
public class ModulesPresenterImpl extends
        CommonPresenterImpl implements
        ModulesPresenter,
        OnAvailableModulesResponseHandler,
        OnActiveModulesResponseHandler {

    private static final String TAG = ModulesPresenterImpl.class.getSimpleName();

    private ModulesView view;
    private ModulesController controller;

    private Map<String, AvailableModuleResponseDto> availableModuleResponseMapping;

    public ModulesPresenterImpl(Context context) {
        super(context);
        setController(new ModulesControllerImpl(this));
    }

    @Override
    public void setView(ModulesView view) {
        super.setView(view);
        this.view = view;
    }

    @Override
    public void setController(ModulesController controller) {
        this.controller = controller;
    }

    @Override
    public void doInitView() {
        view.initView();

        String userEmail = PreferenceUtils.getUserEmail(getContext());

        DbUser user = controller.getUserByToken(userEmail);

        if (user == null) {

            LoginUtils.doLogout(getContext());
            view.finishActivity();
            return;
        }

        List<DbModule> installedModules = user.getDbModuleList();

        // no modules was found -> request from server
        if (installedModules.isEmpty()) {
            Log.d(TAG, "Module list not found in db. Requesting from server...");

            requestAvailableModules();

        } else {

            Log.d(TAG, "Installed modules found in the db. Showing them...");

            availableModuleResponseMapping = new HashMap<>();

            for (DbModule module : installedModules) {

                availableModuleResponseMapping.put(
                        module.getPackageName(),
                        ConverterUtils.convertModule(module));
            }

            view.setModuleList(installedModules);

            Set<String> permsToAsk = checkPermissionsGranted();

            // ask if there is something to ask
            if (!permsToAsk.isEmpty()) {

                ActivityCompat.requestPermissions(this,
                        permsToAsk.toArray(new String[permsToAsk.size()]),
                        Constants.PERM_MODULE_INSTALL);
            }
        }
    }

    @Override
    public void requestAvailableModules() {

        final String userToken = PreferenceUtils.getUserToken(getContext());

        // call api service
        controller.requestAvailableModules(userToken, this);
    }

    @Override
    public void onAvailableModulesSuccess(final List<AvailableModuleResponseDto> apiResponse, Response response) {

        List<String> mActiveModules;

        if (apiResponse != null && !apiResponse.isEmpty()) {

            Log.d(TAG, apiResponse.toString());

            if (availableModuleResponseMapping == null) {
                availableModuleResponseMapping = new HashMap<>();
            } else {
                availableModuleResponseMapping.clear();
            }

            for (AvailableModuleResponseDto resp : apiResponse) {
                availableModuleResponseMapping.put(resp.getModulePackage(), resp);
            }

            boolean hasUserRequestedActiveModules = PreferenceUtils
                    .hasUserRequestedActiveModules(getContext());

            if (hasUserRequestedActiveModules) {

                mActiveModules = new ArrayList<>(0);
                view.stopSwipeRefresh();

                controller.processAvailableModules(apiResponse);

                return;
            }

            final String userToken = PreferenceUtils.getUserToken(getContext());

            // get list of already activated modules
            moduleEndpoint.getActiveModules(userToken,
                    new Callback<List<String>>() {

                        @Override
                        public void success(List<String> activeModules,
                                            Response response) {

                            mSwipeRefreshLayout.setRefreshing(false);

                            PreferenceUtils.setUserRequestedActiveModules(getApplicationContext(), true);

                            if (activeModules != null && !activeModules.isEmpty()) {

                                Log.d(TAG, activeModules.toString());
                                mActiveModules = activeModules;

                            } else {
                                mActiveModules = new ArrayList<>(0);
                            }

                            processAvailableModules(apiResponse);
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            showErrorMessages(error);
                            mActiveModules = new ArrayList<>(0);
                            mSwipeRefreshLayout.setRefreshing(false);
                            processAvailableModules(apiResponse);
                        }
                    });

        } else {
            mActiveModules = new ArrayList<>(0);

            view.setNoModulesView();
        }
    }

    @Override
    public void onAvailableModulesError(RetrofitError error) {

        doDefaultErrorProcessing(error);
        view.setErrorView();
    }

    @Override
    public void onActiveModulesReceived(List<String> activeModules, Response response) {

    }

    @Override
    public void onActiveModulesFailed(RetrofitError error) {

    }
}
