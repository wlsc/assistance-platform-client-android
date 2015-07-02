package de.tu_darmstadt.tk.android.assistance.activities.common;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import de.tu_darmstadt.tk.android.assistance.R;
import de.tu_darmstadt.tk.android.assistance.models.http.HttpErrorCode;
import de.tu_darmstadt.tk.android.assistance.models.http.response.ErrorResponse;
import de.tu_darmstadt.tk.android.assistance.utils.Constants;
import de.tu_darmstadt.tk.android.assistance.utils.Toaster;
import de.tu_darmstadt.tk.android.assistance.utils.Utils;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Base activity for common stuff
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected boolean mBackButtonPressedOnce;

    public BaseActivity() {
        this.mBackButtonPressedOnce = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                    ErrorResponse errorResponse = (ErrorResponse) retrofitError.getBodyAs(ErrorResponse.class);
                    errorResponse.setStatusCode(httpCode);

                    Integer apiResponseCode = errorResponse.getCode();
                    String apiMessage = errorResponse.getMessage();
                    int httpResponseCode = errorResponse.getStatusCode();
                    HttpErrorCode.ErrorCode apiErrorType = HttpErrorCode.fromCode(apiResponseCode);

                    Log.d(TAG, "Response status: " + httpResponseCode);
                    Log.d(TAG, "Response code: " + apiResponseCode);
                    Log.d(TAG, "Response message: " + apiMessage);

                    break;
                case 404:
                    Toaster.showLong(getApplicationContext(), R.string.error_service_not_available);
                    break;
                case 503:
                    Toaster.showLong(getApplicationContext(), R.string.error_server_temporary_unavailable);
                    break;
            }
        } else {
            Toaster.showLong(getApplicationContext(), R.string.error_service_not_available);
        }
    }

    /**
     * Returns user email saved in SharedPreferences
     *
     * @return
     */
    protected String getUserEmail() {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String userEmail = sp.getString(Constants.PREF_USER_EMAIL, "");

        return userEmail;
    }

    /**
     * Returns user token saved in SharedPreferences
     *
     * @return
     */
    protected String getUserToken() {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        return sp.getString(Constants.PREF_USER_TOKEN, "");
    }
}
