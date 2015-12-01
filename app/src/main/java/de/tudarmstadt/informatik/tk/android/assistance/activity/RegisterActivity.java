package de.tudarmstadt.informatik.tk.android.assistance.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.notification.Toaster;
import de.tudarmstadt.informatik.tk.android.assistance.presenter.register.RegisterPresenter;
import de.tudarmstadt.informatik.tk.android.assistance.presenter.register.RegisterPresenterImpl;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.logger.Log;
import de.tudarmstadt.informatik.tk.android.assistance.util.CommonUtils;
import de.tudarmstadt.informatik.tk.android.assistance.util.PreferenceUtils;
import de.tudarmstadt.informatik.tk.android.assistance.view.RegisterView;

/**
 * New user registration view
 *
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 28.06.2015
 */
public class RegisterActivity extends
        AppCompatActivity implements
        RegisterView {

    private static final String TAG = RegisterActivity.class.getSimpleName();

    @Bind(R.id.register_email)
    protected EditText mUserEmail;

    @Bind(R.id.register_password1)
    protected EditText mUserPassword1;

    @Bind(R.id.register_password2)
    protected EditText mUserPassword2;

    @Bind(R.id.sign_up_button)
    protected Button mSignUp;

    private RegisterPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setTitle(R.string.register_activity_title);

        ButterKnife.bind(this);

        setPresenter(new RegisterPresenterImpl(getApplicationContext()));
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
    protected void onDestroy() {
        ButterKnife.unbind(this);
        Log.d(TAG, "onDestroy -> unbound resources");
        super.onDestroy();
    }

    @Override
    public void setPresenter(RegisterPresenter presenter) {
        this.presenter = presenter;
        this.presenter.setLogTag(TAG);
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

}
