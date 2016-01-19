package de.tudarmstadt.informatik.tk.assistance.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.tudarmstadt.informatik.tk.assistance.R;
import de.tudarmstadt.informatik.tk.assistance.activity.base.BaseActivity;
import de.tudarmstadt.informatik.tk.assistance.notification.Toaster;
import de.tudarmstadt.informatik.tk.assistance.presenter.password.ResetPasswordPresenter;
import de.tudarmstadt.informatik.tk.assistance.presenter.password.ResetPasswordPresenterImpl;
import de.tudarmstadt.informatik.tk.assistance.util.PreferenceUtils;
import de.tudarmstadt.informatik.tk.assistance.view.ResetPasswordView;

/**
 * Resetting user password view
 *
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 28.06.2015
 */
public class ResetPasswordActivity extends
        BaseActivity implements
        ResetPasswordView {

    private static final String TAG = ResetPasswordActivity.class.getSimpleName();

    @Bind(R.id.reset_email)
    EditText mUserEmailEditText;

    private ResetPasswordPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPresenter(new ResetPasswordPresenterImpl(getApplicationContext()));
        presenter.initView();
    }

    @Override
    protected void onDestroy() {
        ButterKnife.unbind(this);
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

    @OnClick(R.id.reset_button)
    void onResetPasswordButtonClicked() {
        presenter.doResetPassword(mUserEmailEditText.getText().toString().trim());
    }

    @Override
    public void setPresenter(ResetPasswordPresenter presenter) {
        this.presenter = presenter;
        this.presenter.setView(this);
    }

    @Override
    public void clearErrors() {
        mUserEmailEditText.setError(null);
    }

    @Override
    public void setErrorEmailFieldRequired() {
        mUserEmailEditText.setError(getString(R.string.error_field_required));
        mUserEmailEditText.requestFocus();
    }

    @Override
    public void setErrorEmailInvalid() {
        mUserEmailEditText.setError(getString(R.string.error_invalid_email));
        mUserEmailEditText.requestFocus();
    }

    @Override
    public void showRequestSuccessful() {
        Toaster.showLong(getApplicationContext(), R.string.reset_successful_reset);
    }

    @Override
    public void initView() {
        setContentView(R.layout.activity_reset_password);
        setTitle(R.string.reset_password_activity_title);

        ButterKnife.bind(this);
    }

    @Override
    public void startLoginActivity() {

        Toaster.showLong(getApplicationContext(), R.string.error_user_login_not_valid);
        PreferenceUtils.clearUserCredentials(getApplicationContext());
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
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
        // empty
    }
}
