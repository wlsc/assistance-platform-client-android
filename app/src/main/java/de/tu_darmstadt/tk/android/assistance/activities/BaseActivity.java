package de.tu_darmstadt.tk.android.assistance.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import de.tu_darmstadt.tk.android.assistance.R;
import de.tu_darmstadt.tk.android.assistance.models.http.HttpErrorCode;
import de.tu_darmstadt.tk.android.assistance.models.http.response.ErrorResponse;
import de.tu_darmstadt.tk.android.assistance.utils.Toaster;
import de.tu_darmstadt.tk.android.assistance.utils.Utils;

/**
 * Base activity for common stuff
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Processes error response from server
     *
     * @param errorResponse
     */
    protected void handleError(ErrorResponse errorResponse, String TAG) {

        Integer apiResponseCode = errorResponse.getCode();
        String apiMessage = errorResponse.getMessage();
        int httpResponseCode = errorResponse.getStatusCode();
        HttpErrorCode.ErrorCode apiErrorType = HttpErrorCode.fromCode(apiResponseCode);

        Log.d(TAG, "Response status: " + httpResponseCode);
        Log.d(TAG, "Response code: " + apiResponseCode);
        Log.d(TAG, "Response message: " + apiMessage);

        if (httpResponseCode == 400) {

            switch (apiErrorType) {

                case EMAIL_ALREADY_EXISTS:
                    Toaster.showLong(this, R.string.error_email_exists);
                    break;
                default:
                    Toaster.showLong(this, R.string.error_unknown);
                    break;
            }

            Utils.showKeyboard(getApplicationContext(), getCurrentFocus());
        }
    }
}
