package de.tudarmstadt.informatik.tk.android.assistance.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.registration.RegistrationRequest;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.registration.RegistrationResponse;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.endpoint.UserEndpoint;
import de.tudarmstadt.informatik.tk.android.assistance.util.CommonUtils;
import de.tudarmstadt.informatik.tk.android.assistance.util.Constants;
import de.tudarmstadt.informatik.tk.android.assistance.util.ValidationUtils;
import de.tudarmstadt.informatik.tk.android.assistance.util.PreferencesUtils;
import de.tudarmstadt.informatik.tk.android.assistance.notification.Toaster;
import de.tudarmstadt.informatik.tk.android.kraken.model.api.endpoint.EndpointGenerator;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * New user registration view
 *
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 28.06.2015
 */
public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = RegisterActivity.class.getSimpleName();

    @Bind(R.id.register_email)
    protected EditText mUserEmail;

    @Bind(R.id.register_password1)
    protected EditText mUserPassword1;

    @Bind(R.id.register_password2)
    protected EditText mUserPassword2;

    @Bind(R.id.sign_up_button)
    protected Button mSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setTitle(R.string.register_activity_title);

        ButterKnife.bind(this);
    }

    /**
     * Registration button
     */
    @OnClick(R.id.sign_up_button)
    protected void onUserSignUp() {

        String email = mUserEmail.getText().toString().trim();
        String password1 = mUserPassword1.getText().toString();
        String password2 = mUserPassword2.getText().toString();

        if (isInputOK(email, password1, password2)) {
            doRegisterUser(email, password1);
        }
    }

    /**
     * Validates user's input
     *
     * @return
     */
    private boolean isInputOK(String email, String password1, String password2) {

        // reset all errors
        mUserEmail.setError(null);
        mUserPassword1.setError(null);
        mUserPassword2.setError(null);

        // EMPTY FIELDS CHECK
        if (TextUtils.isEmpty(email)) {
            mUserEmail.setError(getString(R.string.error_field_required));
            mUserEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password1)) {
            mUserPassword1.setError(getString(R.string.error_field_required));
            mUserPassword1.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password2)) {
            mUserPassword2.setError(getString(R.string.error_field_required));
            mUserPassword2.requestFocus();
            return false;
        }

        // NOT VALID EMAIL
        if (!ValidationUtils.isValidEmail(email)) {
            mUserEmail.setError(getString(R.string.error_invalid_email));
            mUserEmail.requestFocus();
            return false;
        }

        // NOT EQUAL PASSWORDS
        if (!password1.equals(password2)) {
            mUserPassword1.setError(getString(R.string.error_not_same_passwords));
            mUserPassword2.setError(getString(R.string.error_not_same_passwords));
            return false;
        }

        // NOT VALID LENGTH
        if (!ValidationUtils.isPasswordLengthValid(password1)) {
            mUserPassword1.setError(getString(R.string.error_invalid_password));
            mUserPassword2.setError(getString(R.string.error_invalid_password));
            mUserPassword1.requestFocus();
            return false;
        }

        CommonUtils.hideKeyboard(getApplicationContext(), getCurrentFocus());

        return true;
    }

    /**
     * Sends registration data to server
     *
     * @param email
     * @param password
     */
    private void doRegisterUser(String email, String password) {

//        String passwordHashed = CommonUtils.generateSHA256(password);
        String passwordHashed = password;

        // forming a login request
        RegistrationRequest request = new RegistrationRequest();
        request.setUserEmail(email);
        request.setPassword(passwordHashed);

        // calling api service
        UserEndpoint service = EndpointGenerator.getInstance(getApplicationContext())
                .create(UserEndpoint.class);
        service.registerUser(request, new Callback<RegistrationResponse>() {

            @Override
            public void success(RegistrationResponse apiResponse, Response response) {
                showLoginScreen(apiResponse);
                Log.d(TAG, "success! userId: " + apiResponse.getUserId());
            }

            @Override
            public void failure(RetrofitError error) {
                showErrorMessages(TAG, error);
            }
        });
    }

    /**
     * Shows login screen if registration was successful
     *
     * @param registrationResponse
     */
    private void showLoginScreen(RegistrationResponse registrationResponse) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(Constants.INTENT_USER_ID, registrationResponse.getUserId());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        ButterKnife.unbind(this);
        Log.d(TAG, "onDestroy -> unbound resources");
        super.onDestroy();
    }

    /**
     * Processes error response from server
     *
     * @param TAG
     * @param retrofitError
     */
    protected void showErrorMessages(String TAG, RetrofitError retrofitError) {

        Response response = retrofitError.getResponse();

        if (response != null) {

            int httpCode = response.getStatus();

            switch (httpCode) {
                case 400:
                    break;
                case 401:
                    Toaster.showLong(getApplicationContext(), R.string.error_user_login_not_valid);
                    PreferencesUtils.clearUserCredentials(getApplicationContext());
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                    break;
                case 404:
                    Toaster.showLong(getApplicationContext(), R.string.error_service_not_available);
                    break;
                case 503:
                    Toaster.showLong(getApplicationContext(), R.string.error_server_temporary_unavailable);
                    break;
                default:
                    Toaster.showLong(getApplicationContext(), R.string.error_unknown);
                    break;
            }
        } else {
            Toaster.showLong(getApplicationContext(), R.string.error_service_not_available);
        }
    }
}
