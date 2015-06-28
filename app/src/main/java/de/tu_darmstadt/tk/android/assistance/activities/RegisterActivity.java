package de.tu_darmstadt.tk.android.assistance.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.tu_darmstadt.tk.android.assistance.R;
import de.tu_darmstadt.tk.android.assistance.activities.common.BaseActivity;
import de.tu_darmstadt.tk.android.assistance.models.http.request.RegistrationRequest;
import de.tu_darmstadt.tk.android.assistance.models.http.response.ErrorResponse;
import de.tu_darmstadt.tk.android.assistance.models.http.response.RegistrationResponse;
import de.tu_darmstadt.tk.android.assistance.services.RegistrationService;
import de.tu_darmstadt.tk.android.assistance.services.ServiceGenerator;
import de.tu_darmstadt.tk.android.assistance.utils.InputValidation;
import de.tu_darmstadt.tk.android.assistance.utils.Utils;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class RegisterActivity extends BaseActivity {

    private String TAG = RegisterActivity.class.getName();

    @InjectView(R.id.register_email)
    protected EditText etUserEmail;

    @InjectView(R.id.register_password1)
    protected EditText etUserPassword1;

    @InjectView(R.id.register_password2)
    protected EditText etUserPassword2;

    @InjectView(R.id.sign_up_button)
    protected Button bSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ButterKnife.inject(this);
    }

    /**
     * Registration button
     */
    @OnClick(R.id.sign_up_button)
    protected void onUserSignUp() {

        String email = etUserEmail.getText().toString().trim();
        String password1 = etUserPassword1.getText().toString();
        String password2 = etUserPassword2.getText().toString();

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
        etUserEmail.setError(null);
        etUserPassword1.setError(null);
        etUserPassword2.setError(null);

        // EMPTY FIELDS CHECK
        if (TextUtils.isEmpty(email)) {
            etUserEmail.setError(getString(R.string.error_field_required));
            etUserEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password1)) {
            etUserPassword1.setError(getString(R.string.error_field_required));
            etUserPassword1.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password2)) {
            etUserPassword2.setError(getString(R.string.error_field_required));
            etUserPassword2.requestFocus();
            return false;
        }

        // NOT VALID EMAIL
        if (!InputValidation.isValidEmail(email)) {
            etUserEmail.setError(getString(R.string.error_invalid_email));
            etUserEmail.requestFocus();
            return false;
        }

        // NOT EQUAL PASSWORDS
        if (!password1.equals(password2)) {
            etUserPassword1.setError(getString(R.string.error_not_same_passwords));
            etUserPassword2.setError(getString(R.string.error_not_same_passwords));
            return false;
        }

        // NOT VALID LENGTH
        if (!InputValidation.isPasswordLengthValid(password1)) {
            etUserPassword1.setError(getString(R.string.error_invalid_password));
            etUserPassword2.setError(getString(R.string.error_invalid_password));
            etUserPassword1.requestFocus();
            return false;
        }

        Utils.hideKeyboard(getApplicationContext(), getCurrentFocus());

        return true;
    }

    private void doRegisterUser(String email, String password) {

//        String passwordHashed = Utils.generateSHA256(password);
        String passwordHashed = password;

        // forming a login request
        RegistrationRequest request = new RegistrationRequest();
        request.setUserEmail(email);
        request.setPassword(passwordHashed);

        // calling api service
        RegistrationService service = ServiceGenerator.createService(RegistrationService.class);
        service.registerUser(request, new Callback<RegistrationResponse>() {

            @Override
            public void success(RegistrationResponse apiResponse, Response response) {
                showLoginScreen(apiResponse);
                Log.d(TAG, "success! userId: " + apiResponse.getUserId());
            }

            @Override
            public void failure(RetrofitError error) {

                ErrorResponse errorResponse = (ErrorResponse) error.getBodyAs(ErrorResponse.class);
                errorResponse.setStatusCode(error.getResponse().getStatus());

                handleError(errorResponse, TAG);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Shows login screen if registration was successful
     *
     * @param registrationResponse
     */
    private void showLoginScreen(RegistrationResponse registrationResponse) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("user_id", registrationResponse.getUserId());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
