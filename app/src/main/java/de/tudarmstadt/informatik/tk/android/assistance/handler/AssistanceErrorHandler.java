package de.tudarmstadt.informatik.tk.android.assistance.handler;

import android.util.Log;

import java.net.HttpURLConnection;

import de.tudarmstadt.informatik.tk.android.assistance.model.api.error.ErrorResponse;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.exception.BadRequestException;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.exception.ServiceNotAvailableException;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.exception.ServiceTemporaryNotAvailableException;
import retrofit.ErrorHandler;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 21.08.2015
 */
public class AssistanceErrorHandler implements ErrorHandler {

    private String TAG = AssistanceErrorHandler.class.getSimpleName();

    public AssistanceErrorHandler() {
    }

    /**
     * Return a custom exception to be thrown for a {@link RetrofitError}. It is recommended that you
     * pass the supplied error as the cause to any new exceptions.
     * <p/>
     * If the return exception is checked it must be declared to be thrown on the interface method.
     * <p/>
     * Example usage:
     * <pre>
     * class MyErrorHandler implements ErrorHandler {
     *   &#64;Override public Throwable handleError(RetrofitError cause) {
     *     Response r = cause.getResponse();
     *     if (r != null &amp;&amp; r.getStatus() == 401) {
     *       return new UnauthorizedException(cause);
     *     }
     *     return cause;
     *   }
     * }
     * </pre>
     *
     * @param retrofitError the original {@link RetrofitError} exception
     * @return Throwable an exception which will be thrown from a synchronous interface method or
     * passed to an asynchronous error callback. Must not be {@code null}.
     */
    @Override
    public Throwable handleError(RetrofitError retrofitError) {

        ErrorResponse errorResponse = (ErrorResponse) retrofitError.getBodyAs(ErrorResponse.class);

        if (errorResponse == null) {
            return retrofitError;
        }

        Integer apiResponseCode = errorResponse.getCode();
        String apiMessage = errorResponse.getMessage();
        int httpResponseCode = errorResponse.getStatusCode();

        Log.d(TAG, "------------------------------------");
        Log.d(TAG, "Error handler information");
        Log.d(TAG, "------------------------------------");
        Log.d(TAG, "Response status: " + httpResponseCode);
        Log.d(TAG, "Response code: " + apiResponseCode);
        Log.d(TAG, "Response message: " + apiMessage);
        Log.d(TAG, "------------------------------------");

        Response response = retrofitError.getResponse();

        if (response != null) {

            int httpCode = response.getStatus();
            errorResponse.setStatusCode(httpCode);

            switch (httpCode) {
                case HttpURLConnection.HTTP_BAD_REQUEST:
                    return new BadRequestException(apiMessage);
                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    return new IllegalAccessException(apiMessage);
                case HttpURLConnection.HTTP_NOT_FOUND:
                    return new ServiceNotAvailableException(apiMessage);
                case HttpURLConnection.HTTP_UNAVAILABLE:
                    return new ServiceTemporaryNotAvailableException(apiMessage);
                default:
                    return new NoSuchMethodException(apiMessage);
            }
        } else {
            return new ServiceNotAvailableException(apiMessage);
        }
    }
}
