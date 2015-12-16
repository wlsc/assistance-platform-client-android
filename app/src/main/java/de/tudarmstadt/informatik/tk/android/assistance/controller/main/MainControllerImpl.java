package de.tudarmstadt.informatik.tk.android.assistance.controller.main;

import android.app.Activity;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import de.tudarmstadt.informatik.tk.android.assistance.controller.CommonControllerImpl;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnActiveModulesResponseHandler;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnGooglePlayServicesAvailable;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnModuleFeedbackResponseHandler;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnResponseHandler;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.dto.profile.ProfileResponseDto;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.endpoint.ModuleEndpoint;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.endpoint.UserEndpoint;
import de.tudarmstadt.informatik.tk.android.assistance.presenter.main.MainPresenter;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbNews;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbUser;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.model.api.endpoint.EndpointGenerator;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.DateUtils;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.GcmUtils;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.logger.Log;
import de.tudarmstadt.informatik.tk.android.assistance.util.PreferenceUtils;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.ClientFeedbackDto;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 02.12.2015
 */
public class MainControllerImpl extends
        CommonControllerImpl implements
        MainController {

    private static final String TAG = MainControllerImpl.class.getSimpleName();

    private final MainPresenter presenter;

    public MainControllerImpl(MainPresenter presenter) {
        super(presenter.getContext());
        this.presenter = presenter;
    }

    @Override
    public List<DbNews> getCachedNews(long userId) {
        return daoProvider.getNewsDao().get(userId);
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
    public void requestActiveModules(final String userToken,
                                     final OnActiveModulesResponseHandler handler) {

        final ModuleEndpoint moduleEndpoint = EndpointGenerator
                .getInstance(presenter.getContext())
                .create(ModuleEndpoint.class);

        moduleEndpoint.getActiveModules(userToken,
                new Callback<Set<String>>() {

                    @Override
                    public void success(Set<String> activeModules,
                                        Response response) {

                        handler.onActiveModulesReceived(activeModules, response);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        handler.onActiveModulesFailed(error);
                    }
                });
    }

    @Override
    public void requestUserProfile(String userToken, final OnResponseHandler<ProfileResponseDto> handler) {

        UserEndpoint userService = EndpointGenerator
                .getInstance(presenter.getContext())
                .create(UserEndpoint.class);

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
                                      final OnModuleFeedbackResponseHandler handler) {

        final ModuleEndpoint moduleEndpoint = EndpointGenerator
                .getInstance(presenter.getContext())
                .create(ModuleEndpoint.class);

        moduleEndpoint.getModuleFeedback(userToken, new Callback<List<ClientFeedbackDto>>() {

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
}
