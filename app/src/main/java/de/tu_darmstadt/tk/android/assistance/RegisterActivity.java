package de.tu_darmstadt.tk.android.assistance;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.tu_darmstadt.tk.android.assistance.models.http.HttpErrorCode;
import de.tu_darmstadt.tk.android.assistance.models.http.request.RegistrationRequest;
import de.tu_darmstadt.tk.android.assistance.models.http.response.ErrorResponse;
import de.tu_darmstadt.tk.android.assistance.models.http.response.RegistrationResponse;
import de.tu_darmstadt.tk.android.assistance.services.RegistrationService;
import de.tu_darmstadt.tk.android.assistance.services.ServiceGenerator;
import de.tu_darmstadt.tk.android.assistance.utils.Toaster;
import de.tu_darmstadt.tk.android.assistance.utils.Util;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class RegisterActivity extends AppCompatActivity {

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
        if (!Util.isValidEmail(email)) {
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
        if (!Util.isPasswordLengthValid(password1)) {
            etUserPassword1.setError(getString(R.string.error_invalid_password));
            etUserPassword2.setError(getString(R.string.error_invalid_password));
            etUserPassword1.requestFocus();
            return false;
        }

        Util.hideKeyboard(getApplicationContext(), getCurrentFocus());

        return true;
    }

    private void doRegisterUser(String email, String password) {

//        String passwordHashed = Util.generateSHA256(password);
        String passwordHashed = password;

        RegistrationRequest request = new RegistrationRequest();
        request.setUserEmail(email);
        request.setPassword(passwordHashed);

        RegistrationService service = ServiceGenerator.createService(RegistrationService.class);
        service.registerUser(request, new Callback<RegistrationResponse>() {

            @Override
            public void success(RegistrationResponse registrationResponse, Response response) {

                if (registrationResponse != null && registrationResponse.getUserId() != null) {
                    showLoginScreen(registrationResponse);
                    Log.d(TAG, "success! userId: " + registrationResponse.getUserId());
                } else {
                    Log.d(TAG, "failure. registrationResponse = null");
                }
            }

            @Override
            public void failure(RetrofitError error) {

                ErrorResponse errorResponse = (ErrorResponse) error.getBodyAs(ErrorResponse.class);
                errorResponse.setStatusCode(error.getResponse().getStatus());

                handleRegistrationError(errorResponse);
            }
        });
    }

    /**
     * Processes error response from server
     *
     * @param errorResponse
     */
    private void handleRegistrationError(ErrorResponse errorResponse) {

        Integer apiResponseCode = errorResponse.getCode();
        String apiMessage = errorResponse.getMessage();
        int httpResponseCode = errorResponse.getStatusCode();
        HttpErrorCode.ErrorCode apiErrorType = HttpErrorCode.fromCode(apiResponseCode);

        Log.d(TAG, "Response status: " + httpResponseCode);
        Log.d(TAG, "Response code: " + apiResponseCode);
        Log.d(TAG, "Response message: " + apiMessage);

        if(httpResponseCode == 400) {

            switch (apiErrorType) {

                case EMAIL_ALREADY_EXISTS:
                    Toaster.showLong(this, R.string.error_email_exists);
                    break;
                default:
                    Toaster.showLong(this, R.string.error_unknown);
                    break;
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showLoginScreen(RegistrationResponse registrationResponse) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("user_id", registrationResponse.getUserId());
        startActivity(intent);
    }
}
