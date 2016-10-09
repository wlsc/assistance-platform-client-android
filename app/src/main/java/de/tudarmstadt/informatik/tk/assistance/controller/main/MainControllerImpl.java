package de.tudarmstadt.informatik.tk.assistance.controller.main;

import android.app.Activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import de.tudarmstadt.informatik.tk.assistance.controller.CommonControllerImpl;
import de.tudarmstadt.informatik.tk.assistance.handler.OnGooglePlayServicesAvailable;
import de.tudarmstadt.informatik.tk.assistance.handler.OnResponseHandler;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.ClientFeedbackDto;
import de.tudarmstadt.informatik.tk.assistance.presenter.main.MainPresenter;
import de.tudarmstadt.informatik.tk.assistance.sdk.db.DbModule;
import de.tudarmstadt.informatik.tk.assistance.sdk.db.DbModuleCapability;
import de.tudarmstadt.informatik.tk.assistance.sdk.db.DbNews;
import de.tudarmstadt.informatik.tk.assistance.sdk.db.DbUser;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.module.ActivatedModulesResponse;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.module.ModuleCapabilityResponseDto;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.module.ModuleResponseDto;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.user.profile.ProfileResponseDto;
import de.tudarmstadt.informatik.tk.assistance.sdk.provider.ApiProvider;
import de.tudarmstadt.informatik.tk.assistance.sdk.provider.SensorProvider;
import de.tudarmstadt.informatik.tk.assistance.sdk.provider.api.ModuleApiProvider;
import de.tudarmstadt.informatik.tk.assistance.sdk.provider.api.UserApiProvider;
import de.tudarmstadt.informatik.tk.assistance.sdk.provider.dao.news.NewsDao;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.AppUtils;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.ConverterUtils;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.DateUtils;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.GcmUtils;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.PermissionUtils;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.logger.Log;
import de.tudarmstadt.informatik.tk.assistance.util.PreferenceUtils;
import retrofit.RetrofitError;
import rx.Observable;
import rx.Subscriber;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 02.12.2015
 */
public class MainControllerImpl extends
        CommonControllerImpl implements
        MainController {

    private static final String TAG = MainControllerImpl.class.getSimpleName();

    private final MainPresenter presenter;

    private final ModuleApiProvider moduleApiProvider;

    public MainControllerImpl(MainPresenter presenter) {
        super(presenter.getContext());
        this.presenter = presenter;
        this.moduleApiProvider = apiProvider.getModuleApiProvider();
    }

    @Override
    public List<DbNews> getCachedNews(long userId) {
        return daoProvider.getNewsDao().getAll(userId);
    }

    @Override
    public List<ClientFeedbackDto> convertDbEntries(List<DbNews> dbNews) {

        if (dbNews == null) {
            return Collections.emptyList();
        }

        final List<ClientFeedbackDto> result = new ArrayList<>(dbNews.size());
        final NewsDao newsDao = daoProvider.getNewsDao();

        for (DbNews entry : dbNews) {

            if (entry == null) {
                continue;
            }

            result.add(newsDao.convert(entry));
        }

        return result;
    }

    @Override
    public List<DbNews> convertDtos(List<ClientFeedbackDto> feedbackDtos) {

        if (feedbackDtos == null) {
            return Collections.emptyList();
        }

        List<DbNews> result = new ArrayList<>(feedbackDtos.size());
        NewsDao newsDao = daoProvider.getNewsDao();

        for (ClientFeedbackDto entry : feedbackDtos) {

            if (entry == null) {
                continue;
            }

            result.add(newsDao.convert(entry));
        }

        return result;
    }

    @Override
    public void registerGCMPush(Activity activity, OnGooglePlayServicesAvailable handler) {

        boolean isTokenWasSent = PreferenceUtils.isGcmTokenWasSent(presenter.getContext());

        if (isTokenWasSent) {
            return;
        }

        // check for play services installation
        if (GcmUtils.isPlayServicesInstalled(activity)) {

            Log.d(TAG, "Google Play Services are installed.");

            // starting registration GCM service
            handler.onPlayServicesAvailable();

        } else {
            Log.d(TAG, "Google Play Services NOT installed.");

            handler.onPlayServicesNotAvailable();
        }
    }

    @Override
    public void requestUserProfile(String userToken, final OnResponseHandler<ProfileResponseDto> handler) {

        UserApiProvider userService = ApiProvider.getInstance(presenter.getContext()).getUserApiProvider();

        userService.getUserProfileShort(userToken)
                .subscribe(new Subscriber<ProfileResponseDto>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof RetrofitError) {
                            handler.onError((RetrofitError) e);
                        }
                    }

                    @Override
                    public void onNext(ProfileResponseDto profileResponseDto) {
                        handler.onSuccess(profileResponseDto);
                    }
                });
    }

    @Override
    public Observable<List<ClientFeedbackDto>> requestModuleFeedback(String userToken,
                                                                     Long deviceId) {
        return moduleApiProvider.moduleFeedback(userToken, deviceId);
//        return moduleApiProvider.moduleFeedbackPeriodic(userToken, deviceId);
    }

    @Override
    public void persistLogin(ProfileResponseDto apiResponse) {

        // check already available user in db
        DbUser user = daoProvider.getUserDao().getByEmail(apiResponse.getPrimaryEmail());

        // check for user existence in the db
        if (user == null) {
            // no user found -> create one

            user = new DbUser();

            user.setFirstname(apiResponse.getFirstname());
            user.setLastname(apiResponse.getLastname());
            user.setPrimaryEmail(apiResponse.getPrimaryEmail());

            if (apiResponse.getJoinedSince() != null) {
                user.setJoinedSince(DateUtils.dateToISO8601String(new Date(apiResponse.getJoinedSince()), Locale.getDefault()));
            }

            if (apiResponse.getLastLogin() != null) {
                user.setLastLogin(DateUtils.dateToISO8601String(new Date(apiResponse.getLastLogin()), Locale.getDefault()));
            }

            user.setCreated(DateUtils.dateToISO8601String(new Date(), Locale.getDefault()));

            daoProvider.getUserDao().insert(user);

        } else {
            // found a user -> update for device and user information

            user.setFirstname(apiResponse.getFirstname());
            user.setLastname(apiResponse.getLastname());
            user.setPrimaryEmail(apiResponse.getPrimaryEmail());

            if (apiResponse.getJoinedSince() != null) {
                user.setJoinedSince(DateUtils.dateToISO8601String(new Date(), Locale.getDefault()));
            }

            if (apiResponse.getLastLogin() != null) {
                user.setLastLogin(DateUtils.dateToISO8601String(new Date(apiResponse.getLastLogin()), Locale.getDefault()));
            }

            daoProvider.getUserDao().update(user);
        }
    }

    @Override
    public void initUUID(DbUser user) {

        if (user == null) {
            Log.d(TAG, "User is null");
            return;
        }

        String uuidToken = user.getUuid();

        if (uuidToken == null || uuidToken.isEmpty()) {

            // generate new
            user.setUuid(AppUtils.generateUUID());

            // update db entry
            daoProvider.getUserDao().update(user);
        }
    }

    @Override
    public Observable<ActivatedModulesResponse> requestActivatedModules(String userToken) {
        return moduleApiProvider.getActivatedModules(userToken);
    }

    @Override
    public void disableModules(String userToken, Set<String> declinedPermissions) {

        if (declinedPermissions == null || declinedPermissions.isEmpty()) {
            return;
        }

        DbUser user = daoProvider.getUserDao().getByToken(userToken);

        if (user == null) {
            return;
        }

        List<DbModule> activeModules = daoProvider.getModuleDao().getAllActive(user.getId());

        if (activeModules.isEmpty()) {
            return;
        }

        PermissionUtils permissionUtils = PermissionUtils.getInstance(presenter.getContext());
        Map<String, String[]> dangerousPermissions = permissionUtils.getDangerousPermissionsToDtoMapping();

        for (DbModule activeModule : activeModules) {

            boolean isTimeToBreak = false;

            List<DbModuleCapability> caps = activeModule.getDbModuleCapabilityList();

            if (caps.isEmpty()) {
                continue;
            }

            for (DbModuleCapability dbCap : caps) {

                // optional capability -> skip
                if (!dbCap.getRequired()) {
                    continue;
                }

                String[] capPerms = dangerousPermissions.get(dbCap.getType());

                if (capPerms == null || capPerms.length == 0) {
                    continue;
                }

                for (String perm : Arrays.asList(capPerms)) {

                    // we have declined permission in required for a module
                    // disable that module
                    if (declinedPermissions.contains(perm)) {
                        activeModule.setActive(false);
                        isTimeToBreak = true;
                        break;
                    }
                }

                if (isTimeToBreak) {
                    break;
                }
            }
        }

        // update information about module state
        daoProvider.getModuleDao().update(activeModules);
    }

    @Override
    public void insertActiveModules(List<ModuleResponseDto> modulesToInstall) {

        if (modulesToInstall == null || modulesToInstall.isEmpty()) {
            return;
        }

        for (ModuleResponseDto moduleResponseDto : modulesToInstall) {
            insertModuleResponseWithCapabilities(moduleResponseDto);
        }

        SensorProvider.getInstance(presenter.getContext()).synchronizeRunningSensorsWithDb();
    }

    @Override
    public long insertModuleToDb(DbModule module) {

        if (module == null) {
            return -1L;
        }

        return daoProvider.getModuleDao().insert(module);
    }

    @Override
    public void insertModuleCapabilitiesToDb(List<DbModuleCapability> dbRequiredCaps) {
        daoProvider.getModuleCapabilityDao().insert(dbRequiredCaps);
    }

    @Override
    public boolean insertModuleResponseWithCapabilities(ModuleResponseDto moduleResponse) {

        DbUser user = getUserByEmail(PreferenceUtils.getUserEmail(presenter.getContext()));

        if (user == null) {
            Log.d(TAG, "User is null");
            return false;
        }

        DbModule module = ConverterUtils.convertModule(moduleResponse);

        if (module == null) {
            Log.d(TAG, "Module is null");
            return false;
        }

        module.setActive(true);
        module.setUserId(user.getId());

        DbModule existingModule = daoProvider.getModuleDao()
                .getByPackageIdUserId(module.getPackageName(), module.getUserId());

        long installId;

        if (existingModule == null) {

            installId = insertModuleToDb(module);

            if (installId == -1) {
                return false;
            }

            List<ModuleCapabilityResponseDto> requiredCaps = moduleResponse.getSensorsRequired();
            List<ModuleCapabilityResponseDto> optionalCaps = moduleResponse.getSensorsOptional();

            List<DbModuleCapability> dbRequiredCaps = new ArrayList<>(requiredCaps.size());
            List<DbModuleCapability> dbOptionalCaps = new ArrayList<>(optionalCaps.size());

            for (ModuleCapabilityResponseDto response : requiredCaps) {

                final DbModuleCapability cap = ConverterUtils.convertModuleCapability(response);

                if (cap == null) {
                    continue;
                }

                cap.setModuleId(installId);
                cap.setRequired(true);
                cap.setActive(true);

                dbRequiredCaps.add(cap);
            }

//        for (ModuleCapabilityResponseDto response : optionalCaps) {
//
//            final DbModuleCapability cap = ConverterUtils.convertModuleCapability(response);
//
//            cap.setModulePackageName(installId);
//            cap.setActive(true);
//
//            dbOptionalCaps.add(cap);
//        }

            insertModuleCapabilitiesToDb(dbRequiredCaps);
//        insertModuleCapabilitiesToDb(dbOptionalCaps);
        }

        return true;
    }
}