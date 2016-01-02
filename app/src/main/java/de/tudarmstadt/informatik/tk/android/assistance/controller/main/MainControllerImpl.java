package de.tudarmstadt.informatik.tk.android.assistance.controller.main;

import android.app.Activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.tudarmstadt.informatik.tk.android.assistance.controller.CommonControllerImpl;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnGooglePlayServicesAvailable;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnModuleFeedbackResponseHandler;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnResponseHandler;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.user.UserApi;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.user.profile.ProfileResponseDto;
import de.tudarmstadt.informatik.tk.android.assistance.presenter.main.MainPresenter;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbNews;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbUser;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.model.api.ApiGenerator;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.model.api.module.ActivatedModulesResponse;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.model.api.module.ModuleApi;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.model.api.module.ModuleApiManager;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.provider.dao.news.NewsDao;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.AppUtils;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.DateUtils;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.GcmUtils;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.logger.Log;
import de.tudarmstadt.informatik.tk.android.assistance.util.PreferenceUtils;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.ClientFeedbackDto;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import rx.Observable;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 02.12.2015
 */
public class MainControllerImpl extends
        CommonControllerImpl implements
        MainController {

    private static final String TAG = MainControllerImpl.class.getSimpleName();

    private final MainPresenter presenter;

    private final ModuleApiManager moduleApiManager;

    public MainControllerImpl(MainPresenter presenter) {
        super(presenter.getContext());
        this.presenter = presenter;
        this.moduleApiManager = ModuleApiManager.getInstance(presenter.getContext());
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

        List<ClientFeedbackDto> result = new ArrayList<>();
        NewsDao newsDao = daoProvider.getNewsDao();

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

        List<DbNews> result = new ArrayList<>();
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

        UserApi userService = ApiGenerator
                .getInstance(presenter.getContext())
                .create(UserApi.class);

        userService.getUserProfileShort(userToken, new Callback<ProfileResponseDto>() {

            @Override
            public void success(ProfileResponseDto profileResponse, Response response) {
                handler.onSuccess(profileResponse, response);
            }

            @Override
            public void failure(RetrofitError error) {
                handler.onError(error);
            }
        });
    }

    @Override
    public void requestModuleFeedback(String userToken,
                                      Long deviceId,
                                      final OnModuleFeedbackResponseHandler handler) {

        final ModuleApi moduleEndpoint = ApiGenerator
                .getInstance(presenter.getContext())
                .create(ModuleApi.class);

        moduleEndpoint.getModuleFeedback(userToken, deviceId, new Callback<List<ClientFeedbackDto>>() {

            @Override
            public void success(List<ClientFeedbackDto> clientFeedbackDto, Response response) {
                handler.onModuleFeedbackSuccess(clientFeedbackDto, response);
            }

            @Override
            public void failure(RetrofitError error) {
                handler.onModuleFeedbackFailed(error);
            }
        });
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
        return moduleApiManager.getActivatedModules(userToken);
    }
}
