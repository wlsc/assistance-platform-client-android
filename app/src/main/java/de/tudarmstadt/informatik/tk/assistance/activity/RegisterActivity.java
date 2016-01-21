package de.tudarmstadt.informatik.tk.assistance.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;

import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.tudarmstadt.informatik.tk.assistance.R;
import de.tudarmstadt.informatik.tk.assistance.activity.base.BaseActivity;
import de.tudarmstadt.informatik.tk.assistance.notification.Toaster;
import de.tudarmstadt.informatik.tk.assistance.presenter.register.RegisterPresenter;
import de.tudarmstadt.informatik.tk.assistance.presenter.register.RegisterPresenterImpl;
import de.tudarmstadt.informatik.tk.assistance.util.CommonUtils;
import de.tudarmstadt.informatik.tk.assistance.util.PreferenceUtils;
import de.tudarmstadt.informatik.tk.assistance.view.RegisterView;

/**
 * New user registration view
 *
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 28.06.2015
 */
public class RegisterActivity extends
        BaseActivity implements
        RegisterView {

    private static final String TAG = RegisterActivity.class.getSimpleName();

    @Bind(R.id.register_email)
    protected AppCompatEditText mUserEmail;

    @Bind(R.id.register_password1)
    protected AppCompatEditText mUserPassword1;

    @Bind(R.id.register_password2)
    protected AppCompatEditText mUserPassword2;

    private RegisterPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPresenter(new RegisterPresenterImpl(getApplicationContext()));
        presenter.initView();
    }

    /**
     * Registration button
     */
    @OnClick(R.id.sign_up_button)
    protected void onUserSignUp() {

        presenter.registerUser(
                mUserEmail.getText().toString().trim(),
                mUserPassword1.getText().toString().trim(),
                mUserPassword2.getText().toString().trim());
    }

    @Override
    public void saveUserCredentials() {

        PreferenceUtils.setUserEmail(getApplicationContext(),
                mUserEmail.getText().toString().trim());
        PreferenceUtils.setUserPassword(getApplicationContext(),
                mUserPassword1.getText().toString().trim());
    }

    @Override
    public void showErrorEmailAreadyExists() {
        Toaster.showLong(getApplicationContext(), R.string.error_email_exists);
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

    @Override
    public void setPresenter(RegisterPresenter presenter) {
        this.presenter = presenter;
        this.presenter.setView(this);
    }

    @Override
    public void setErrorEmailEmpty() {
        mUserEmail.setError(getString(R.string.error_field_required));
        mUserEmail.requestFocus();
    }

    @Override
    public void setErrorPassword1Empty() {
        mUserPassword1.setError(getString(R.string.error_field_required));
        mUserPassword1.requestFocus();
    }

    @Override
    public void setErrorPassword2Empty() {
        mUserPassword2.setError(getString(R.string.error_field_required));
        mUserPassword2.requestFocus();
    }

    @Override
    public void setErrorEmailInvalid() {
        mUserEmail.setError(getString(R.string.error_invalid_email));
        mUserEmail.requestFocus();
    }

    @Override
    public void setErrorPasswordsNotSame() {
        mUserPassword1.setError(getString(R.string.error_not_same_passwords));
        mUserPassword2.setError(getString(R.string.error_not_same_passwords));
    }

    @Override
    public void setErrorPasswordLengthInvalid() {
        mUserPassword1.setError(getString(R.string.error_invalid_password));
        mUserPassword2.setError(getString(R.string.error_invalid_password));
        mUserPassword1.requestFocus();
    }

    @Override
    public void hideKeyboard() {
        CommonUtils.hideKeyboard(getApplicationContext(), getCurrentFocus());
    }

    @Override
    public void initView() {
        setContentView(R.layout.activity_register);
        setTitle(R.string.register_activity_title);

        ButterKnife.bind(this);
    }

    @Override
    public void startLoginActivity() {

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void clearErrors() {

        mUserEmail.setError(null);
        mUserPassword1.setError(null);
        mUserPassword2.setError(null);
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
