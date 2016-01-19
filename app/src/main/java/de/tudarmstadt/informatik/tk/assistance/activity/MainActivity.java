package de.tudarmstadt.informatik.tk.assistance.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.maps.model.LatLng;

import org.solovyev.android.views.llm.LinearLayoutManager;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import de.tudarmstadt.informatik.tk.assistance.Constants;
import de.tudarmstadt.informatik.tk.assistance.R;
import de.tudarmstadt.informatik.tk.assistance.activity.base.BaseActivity;
import de.tudarmstadt.informatik.tk.assistance.adapter.NewsAdapter;
import de.tudarmstadt.informatik.tk.assistance.event.ShowGoogleMapEvent;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.ClientFeedbackDto;
import de.tudarmstadt.informatik.tk.assistance.notification.Toaster;
import de.tudarmstadt.informatik.tk.assistance.presenter.main.MainPresenter;
import de.tudarmstadt.informatik.tk.assistance.presenter.main.MainPresenterImpl;
import de.tudarmstadt.informatik.tk.assistance.sdk.event.ShowAccessibilityServiceTutorialEvent;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.module.ActivatedModulesResponse;
import de.tudarmstadt.informatik.tk.assistance.sdk.provider.SensorProvider;
import de.tudarmstadt.informatik.tk.assistance.sdk.service.GcmRegistrationIntentService;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.RxUtils;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.ServiceUtils;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.logger.Log;
import de.tudarmstadt.informatik.tk.assistance.util.PreferenceUtils;
import de.tudarmstadt.informatik.tk.assistance.view.MainView;
import retrofit.RetrofitError;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;

/**
 * Module information dashboard
 *
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 28.06.2015
 */
public class MainActivity extends
        BaseActivity implements
        MainView {

    private static final String TAG = MainActivity.class.getSimpleName();

    private MainPresenter presenter;

    private static final long FEEDBACK_POLLING_RATE = 10;
    private static final TimeUnit FEEDBACK_TIME_UNIT = TimeUnit.SECONDS;

    @Bind(R.id.toolbar)
    protected Toolbar mToolbar;

    @Bind(R.id.assistance_list)
    protected RecyclerView mRecyclerView;

    @Bind(R.id.show_available_modules)
    protected FloatingActionButton showAvailableModules;

    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

    private Subscription activatedModulesSubscription;
    private Subscription modulesFeedbackSubscription;

    private ShowcaseView showCaseTutorial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setPresenter(new MainPresenterImpl(this));
        presenter.initView();
    }

    @Override
    public void initView() {

        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        try {

            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

        } catch (Exception e) {
            // fix for Samsung Android 4.2.2 AppCompat ClassNotFoundException
        }

        setTitle(R.string.main_activity_title);

        showNoNews();
    }

    @Override
    public void showNoNews() {

        ButterKnife.findById(this, R.id.assistance_list).setVisibility(View.GONE);
        ButterKnife.findById(this, R.id.noData).setVisibility(View.VISIBLE);
    }

    @Override
    public void setNewsItems(List<ClientFeedbackDto> assistanceNews) {

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(new NewsAdapter(assistanceNews, getApplicationContext()));
        mRecyclerView.setVisibility(View.VISIBLE);
        ButterKnife.findById(this, R.id.noData).setVisibility(View.GONE);
    }

    @Override
    public void prepareGCMRegistration() {
        presenter.registerGCMPush(this);
    }

    @Override
    public void startGcmRegistrationService() {

        Intent intent = new Intent(this, GcmRegistrationIntentService.class);
        startService(intent);
    }

    @Override
    public void showGooglePlayServicesImportantView() {

        Intent intent = new Intent(this, NoPlayServicesActivity.class);
        startActivity(intent);
    }

    @Override
    public void showModulesList() {

        Intent intent = new Intent(this, ModulesActivity.class);
        startActivity(intent);
    }

    @Override
    public void subscribeActiveAvailableModules(Observable<ActivatedModulesResponse> observable) {
        activatedModulesSubscription = observable.subscribe(new ActivatedModulesSubscriber());
    }

    @Override
    public void showPermissionsAreCrucialDialog(Set<String> declinedPermissions) {
        Toaster.showLong(getApplicationContext(), R.string.permission_is_crucial);
        Toaster.showLong(getApplicationContext(), R.string.error_modules_were_disabled);
    }

    @Override
    public void showAvailableModulesTutorial() {

        if (showCaseTutorial != null && showCaseTutorial.isShowing()) {
            showCaseTutorial.hide();
        }

        if (PreferenceUtils.isModulesTutorialShown(getApplicationContext())) {
            return;
        }

        ShowcaseView.Builder showCaseBuilder = new ShowcaseView.Builder(this);

        showCaseBuilder.setTarget(new ViewTarget(showAvailableModules));
        showCaseBuilder.setContentTitle(R.string.main_activity_tutorial_title);
        showCaseBuilder.setContentText(R.string.main_activity_tutorial_text);
        showCaseBuilder.setStyle(R.style.AppTheme);
        showCaseBuilder.withNewStyleShowcase();
        showCaseBuilder.singleShot(10);

        showCaseTutorial = showCaseBuilder.build();

        PreferenceUtils.setModulesTutorialShown(getApplicationContext(), true);
    }

    @Override
    public void subscribeModuleFeedback(Observable<List<ClientFeedbackDto>> observable) {

        if (executor != null) {
            executor.shutdown();
        }

        executor = new ScheduledThreadPoolExecutor(1);
        executor.scheduleAtFixedRate(() -> {

            if (modulesFeedbackSubscription != null) {
                RxUtils.unsubscribe(modulesFeedbackSubscription);
            }

            modulesFeedbackSubscription = observable.subscribe(new ModulesFeedbackSubscriber());

        }, 0, FEEDBACK_POLLING_RATE, FEEDBACK_TIME_UNIT);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (!ServiceUtils.isHarvesterAbleToRun(getApplicationContext())) {
            showAvailableModulesTutorial();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.news_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivityForResult(intent, Constants.INTENT_SETTINGS_LOGOUT_RESULT);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        if (ServiceUtils.isHarvesterAbleToRun(getApplicationContext())) {
            presenter.requestNewNews();
            SensorProvider.getInstance(this).synchronizeRunningSensorsWithDb();
        }

        super.onResume();
    }

    @Override
    protected void onPause() {

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }

        executor.shutdown();

        super.onPause();
    }

    @Override
    protected void subscribeRequests() {

    }

    @Override
    protected void unsubscribeRequests() {

    }

    @Override
    protected void recreateRequests() {

    }

    @Override
    protected void onDestroy() {
        ButterKnife.unbind(this);
        RxUtils.unsubscribe(activatedModulesSubscription);
        RxUtils.unsubscribe(modulesFeedbackSubscription);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.d(TAG, "onActivityResult. RequestCode: " + requestCode + " resultCode: " + resultCode);

        presenter.handleResultCode(resultCode);
        presenter.handleRequestCode(requestCode);
    }

    @OnClick(R.id.show_available_modules)
    protected void onShowAvailableModules() {

        Intent intent = new Intent(this, ModulesActivity.class);
        startActivityForResult(intent, Constants.INTENT_AVAILABLE_MODULES_RESULT);
    }

    @Override
    public void startLoginActivity() {

        PreferenceUtils.clearUserCredentials(getApplicationContext());
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void clearErrors() {

    }

    @Override
    public void showServiceUnavailable() {
        Toaster.showLong(getApplicationContext(), R.string.error_service_not_available);
    }

    @Override
    public void showServiceTemporaryUnavailable() {
        Toaster.showLong(getApplicationContext(), R.string.error_server_temporary_unavailable);
    }

    @Override
    public void showUnknownErrorOccurred() {
        Toaster.showLong(getApplicationContext(), R.string.error_unknown);
    }

    @Override
    public void showUserForbidden() {
        Toaster.showLong(getApplicationContext(), R.string.error_user_login_not_valid);
    }

    @Override
    public void showActionProhibited() {
        // empty
    }

    @Override
    public void showRetryLaterNotification() {
        Toaster.showLong(getApplicationContext(), R.string.error_service_retry_later);
    }

    @Override
    public void askPermissions(Set<String> permsRequired, Set<String> permsOptional) {

        ActivityCompat.requestPermissions(this,
                permsRequired.toArray(new String[permsRequired.size()]),
                Constants.PERM_MODULE_ACTIVATED_REQUEST
        );
    }

    @Override
    public void setPresenter(MainPresenter presenter) {
        this.presenter = presenter;
        this.presenter.setView(this);
    }

    @Override
    public void showAccessibilityServiceTutorial() {

        Intent intent = new Intent(this, AccessibilityTutorialActivity.class);
        startActivityForResult(intent, Constants.INTENT_ACCESSIBILITY_SERVICE_IGNORED_RESULT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        presenter.presentRequestPermissionResult(requestCode, permissions, grantResults);
    }

    public void onEvent(ShowAccessibilityServiceTutorialEvent event) {
        Log.d(TAG, "ShowAccessibilityServiceTutorialEvent has arrived.");

        showAccessibilityServiceTutorial();
    }

    public void onEvent(ShowGoogleMapEvent event) {

        Log.d(TAG, "ShowGoogleMapEvent has arrived.");

        LatLng point = event.getLatLng();

        Log.d(TAG, "Latitude: " + point.latitude);
        Log.d(TAG, "Longitude: " + point.longitude);

        String uri = String.format(Locale.getDefault(), "geo:%f,%f", point.latitude, point.longitude);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(intent);
    }

    private class ActivatedModulesSubscriber extends Subscriber<ActivatedModulesResponse> {

        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
            if (e instanceof RetrofitError) {
                presenter.doDefaultErrorProcessing((RetrofitError) e);
            }
        }

        @Override
        public void onNext(ActivatedModulesResponse activatedModulesResponse) {
            presenter.onActivatedModulesReceived(activatedModulesResponse);
        }
    }

    private class ModulesFeedbackSubscriber extends Subscriber<List<ClientFeedbackDto>> {

        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {

            if (e instanceof RetrofitError) {
                presenter.doDefaultErrorProcessing((RetrofitError) e);
            }
        }

        @Override
        public void onNext(List<ClientFeedbackDto> clientFeedbackDtos) {

            if (clientFeedbackDtos == null) {
                showUnknownErrorOccurred();
                return;
            }

            presenter.presentModuleCardNews(clientFeedbackDtos);
        }
    }
}