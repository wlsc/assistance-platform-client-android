package de.tudarmstadt.informatik.tk.assistance.controller.main;

import android.app.Activity;

import java.util.List;
import java.util.Set;

import de.tudarmstadt.informatik.tk.assistance.controller.CommonController;
import de.tudarmstadt.informatik.tk.assistance.handler.OnGooglePlayServicesAvailable;
import de.tudarmstadt.informatik.tk.assistance.handler.OnResponseHandler;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.user.profile.ProfileResponseDto;
import de.tudarmstadt.informatik.tk.assistance.sdk.db.DbModule;
import de.tudarmstadt.informatik.tk.assistance.sdk.db.DbModuleCapability;
import de.tudarmstadt.informatik.tk.assistance.sdk.db.DbNews;
import de.tudarmstadt.informatik.tk.assistance.sdk.db.DbUser;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.module.ActivatedModulesResponse;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.module.ModuleResponseDto;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.ClientFeedbackDto;
import rx.Observable;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 02.12.2015
 */
public interface MainController extends CommonController {

    List<DbNews> getCachedNews(long userId);

    List<ClientFeedbackDto> convertDbEntries(List<DbNews> dbNews);

    List<DbNews> convertDtos(List<ClientFeedbackDto> dbNews);

    void registerGCMPush(Activity activity, OnGooglePlayServicesAvailable handler);

    /**
     * Requests user profile information
     */
    void requestUserProfile(String userToken, OnResponseHandler<ProfileResponseDto> handler);

    Observable<List<ClientFeedbackDto>> requestModuleFeedback(String userToken, Long deviceId);

    /**
     * Updates existent user login or creates one in db
     *
     * @param apiResponse
     */
    void persistLogin(ProfileResponseDto apiResponse);

    void initUUID(DbUser userId);

    Observable<ActivatedModulesResponse> requestActivatedModules(String userToken);

    void disableModules(String userToken, Set<String> declinedPermissions);

    void insertActiveModules(List<ModuleResponseDto> modulesToInstall);

    long insertModuleToDb(DbModule module);

    void insertModuleCapabilitiesToDb(List<DbModuleCapability> dbRequiredCaps);

    boolean insertModuleResponseWithCapabilities(ModuleResponseDto moduleResponseDto);

    void updateAvailabilityOfModuleCapability(Set<String> grantedPermissions);
}
