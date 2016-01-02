package de.tudarmstadt.informatik.tk.android.assistance.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.solovyev.android.views.llm.LinearLayoutManager;

import java.util.List;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.activity.base.BaseActivity;
import de.tudarmstadt.informatik.tk.android.assistance.adapter.NewsAdapter;
import de.tudarmstadt.informatik.tk.android.assistance.notification.Toaster;
import de.tudarmstadt.informatik.tk.android.assistance.presenter.main.MainPresenter;
import de.tudarmstadt.informatik.tk.android.assistance.presenter.main.MainPresenterImpl;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbNews;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.model.api.module.ActivatedModulesResponse;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.service.GcmRegistrationIntentService;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.RxUtils;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.logger.Log;
import de.tudarmstadt.informatik.tk.android.assistance.util.Constants;
import de.tudarmstadt.informatik.tk.android.assistance.util.PreferenceUtils;
import de.tudarmstadt.informatik.tk.android.assistance.view.MainView;
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

    @Bind(R.id.toolbar)
    protected Toolbar mToolbar;

    @Bind(R.id.assistance_list)
    protected RecyclerView mRecyclerView;

    private Subscription subActivatedModules;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPresenter(new MainPresenterImpl(this));
        presenter.doInitView();
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
    }

    @Override
    public void showNoNews() {

        ButterKnife.findById(this, R.id.assistance_list).setVisibility(View.GONE);
        ButterKnife.findById(this, R.id.noData).setVisibility(View.VISIBLE);
    }

    @Override
    public void setNewsItems(List<DbNews> assistanceNews) {

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(new NewsAdapter(assistanceNews));
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

        Intent intent = new Intent(getApplicationContext(), NoPlayServicesActivity.class);
        startActivity(intent);
    }

    @Override
    public void showModulesList() {

        Intent intent = new Intent(this, ModulesActivity.class);
        startActivity(intent);
    }

    @Override
    public void subscribeActiveAvailableModules(Observable<ActivatedModulesResponse> observable) {

        subActivatedModules = observable.subscribe(new Subscriber<ActivatedModulesResponse>() {

            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                if (e instanceof RetrofitError) {
                    presenter.onActivatedModulesFailed((RetrofitError) e);
                }
            }

            @Override
            public void onNext(ActivatedModulesResponse activatedModulesResponse) {
                presenter.onActivatedModulesReceived(activatedModulesResponse);
            }
        });
    }

    @Override
    public void showPermissionsAreCrucialDialog(Set<String> declinedPermissions) {

        Toaster.showLong(getApplicationContext(), R.string.permission_is_crucial);
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
    protected void onDestroy() {
        ButterKnife.unbind(this);
        RxUtils.unsubscribe(subActivatedModules);
        super.onDestroy();
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
    public void askPermissions(Set<String> permsToAsk) {

        if (permsToAsk.isEmpty()) {
            return;
        }

        requestPermissions(permsToAsk.toArray(new String[permsToAsk.size()]), 88);
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
}