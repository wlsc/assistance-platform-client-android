package de.tu_darmstadt.tk.android.assistance.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.tu_darmstadt.tk.android.assistance.R;
import de.tu_darmstadt.tk.android.assistance.activities.common.BaseActivity;
import de.tu_darmstadt.tk.android.assistance.models.http.HttpErrorCode;
import de.tu_darmstadt.tk.android.assistance.models.http.request.ResetPasswordRequest;
import de.tu_darmstadt.tk.android.assistance.models.http.response.ErrorResponse;
import de.tu_darmstadt.tk.android.assistance.services.ResetPasswordService;
import de.tu_darmstadt.tk.android.assistance.services.ServiceGenerator;
import de.tu_darmstadt.tk.android.assistance.utils.InputValidation;
import de.tu_darmstadt.tk.android.assistance.utils.Toaster;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ResetPasswordActivity extends BaseActivity {

    private static final String TAG = ResetPasswordActivity.class.getSimpleName();

    @Bind(R.id.reset_email)
    EditText mUserEmailEditText;

    @Bind(R.id.reset_button)
    Button mResetButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_reset_password);
        setTitle(R.string.reset_password_activity_title);

        ButterKnife.bind(this);
    }

    @OnClick(R.id.reset_button)
    void onResetPasswordButtonClicked(){

        mUserEmailEditText.setError(null);

        String mUserEmail = mUserEmailEditText.getText().toString();

        if (TextUtils.isEmpty(mUserEmail)) {
            mUserEmailEditText.setError(getString(R.string.error_field_required));
            mUserEmailEditText.requestFocus();
            return;
        }

        if(!InputValidation.isValidEmail(mUserEmail)){
            mUserEmailEditText.setError(getString(R.string.error_invalid_email));
            mUserEmailEditText.requestFocus();
            return;
        }

        Log.d(TAG, "Requesting reset password service...");

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail(mUserEmail);

        ResetPasswordService service = ServiceGenerator.createService(ResetPasswordService.class);
        service.resetUserPassword(request, new Callback<Void>() {

            @Override
            public void success(Void aVoid, Response response) {
                if(response.getStatus() == 200){
                    Toaster.showLong(getApplicationContext(), R.string.reset_successful_reset);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                showErrorMessages(TAG, error);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }
}
