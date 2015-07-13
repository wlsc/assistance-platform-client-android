package de.tu_darmstadt.tk.android.assistance.services;

import de.tu_darmstadt.tk.android.assistance.Config;
import de.tu_darmstadt.tk.android.assistance.httpclient.UntrustedOkHttpClient;
import retrofit.RestAdapter;
import retrofit.android.AndroidLog;
import retrofit.client.OkClient;

/**
 * Created by Wladimir Schmidt on 27.06.2015.
 */
public class ServiceGenerator {

    private ServiceGenerator() {
    }

    public static <T> T createService(Class<T> serviceClass) {

        RestAdapter.Builder builder = new RestAdapter.Builder()
                // enabling log traces
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setLog(new AndroidLog("HTTP_CLIENT"))
                .setEndpoint(Config.ASSISTANCE_URL)
                .setClient(new OkClient(new UntrustedOkHttpClient().getUnsafeOkHttpClient()));

        RestAdapter adapter = builder.build();

        return adapter.create(serviceClass);
    }
}
