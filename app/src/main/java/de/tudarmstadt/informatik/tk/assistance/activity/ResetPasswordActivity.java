package de.tudarmstadt.informatik.tk.assistance.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.AppCompatEditText;

import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import de.tudarmstadt.informatik.tk.assistance.R.id;
import de.tudarmstadt.informatik.tk.assistance.R.layout;
import de.tudarmstadt.informatik.tk.assistance.R.string;
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

    private Unbinder unbinder;

    @BindView(id.reset_email)
    protected AppCompatEditText mUserEmailEditText;

    private ResetPasswordPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPresenter(new ResetPasswordPresenterImpl(getApplicationContext()));
        presenter.initView();
    }

    @Override
    protected void onDestroy() {
        unbinder.unbind();
        super.onDestroy();
    }

    @OnClick(id.reset_button)
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
        mUserEmailEditText.setError(getString(string.error_field_required));
        mUserEmailEditText.requestFocus();
    }

    @Override
    public void setErrorEmailInvalid() {
        mUserEmailEditText.setError(getString(string.error_invalid_email));
        mUserEmailEditText.requestFocus();
    }

    @Override
    public void showRequestSuccessful() {
        Toaster.showLong(getApplicationContext(), string.reset_successful_reset);
    }

    @Override
    public void initView() {
        setContentView(layout.activity_reset_password);
        setTitle(string.reset_password_activity_title);

        unbinder = ButterKnife.bind(this);
    }

    @Override
    public void startLoginActivity() {

        Toaster.showLong(getApplicationContext(), string.error_user_login_not_valid);
        PreferenceUtils.clearUserCredentials(getApplicationContext());
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ActivityCompat.startActivity(this, intent, null);
        finish();
    }

    @Override
    public void showServiceUnavailable() {
        Toaster.showLong(getApplicationContext(), string.error_service_not_available);
    }

    @Override
    public void showServiceTemporaryUnavailable() {
        Toaster.showLong(getApplicationContext(), string.error_server_temporary_unavailable);
    }

    @Override
    public void showUnknownErrorOccurred() {
        Toaster.showLong(getApplicationContext(), string.error_unknown);
    }

    @Override
    public void showUserForbidden() {
        Toaster.showLong(getApplicationContext(), string.error_user_login_not_valid);
    }

    @Override
    public void showActionProhibited() {
        // empty
    }

    @Override
    public void showRetryLaterNotification() {
        Toaster.showLong(getApplicationContext(), string.error_service_retry_later);
    }

    @Override
    public void askPermissions(Set<String> permsRequired, Set<String> permsOptional) {
        // empty
    }
}
