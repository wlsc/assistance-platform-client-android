package de.tudarmstadt.informatik.tk.android.assistance.controller.main;

import android.app.Activity;

import java.util.List;
import java.util.Set;

import de.tudarmstadt.informatik.tk.android.assistance.controller.CommonController;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnGooglePlayServicesAvailable;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnModuleFeedbackResponseHandler;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnResponseHandler;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.user.profile.ProfileResponseDto;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbNews;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbUser;
import rx.Observable;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 02.12.2015
 */
public interface MainController extends CommonController {

    List<DbNews> getCachedNews(long userId);

    void registerGCMPush(Activity activity, OnGooglePlayServicesAvailable handler);

    /**
     * Asks backend for activated modules
     */
    Observable<Set<String>> requestActiveModules(String userToken);

    /**
     * Requests user profile information
     */
    void requestUserProfile(String userToken, OnResponseHandler<ProfileResponseDto> handler);

    void requestModuleFeedback(String userToken, Long deviceId, OnModuleFeedbackResponseHandler handler);

    /**
     * Updates existent user login or creates one in db
     *
     * @param apiResponse
     */
    void persistLogin(ProfileResponseDto apiResponse);

    void initUUID(DbUser userId);
}
