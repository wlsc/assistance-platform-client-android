package de.tudarmstadt.informatik.tk.android.assistance.services;

import com.google.gson.GsonBuilder;

import de.tudarmstadt.informatik.tk.android.assistance.Config;
import de.tudarmstadt.informatik.tk.android.assistance.models.httpclient.UntrustedOkHttpClient;
import retrofit.RestAdapter;
import retrofit.android.AndroidLog;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

/**
 * Created by Wladimir Schmidt on 27.06.2015.
 */
public class ServiceGenerator {

    private ServiceGenerator() {
    }

    /**
     * Creates request service
     *
     * @param serviceClass
     * @param <T>
     * @return
     */
    public static <T> T createService(Class<T> serviceClass) {

        GsonBuilder gsonBuilder = new GsonBuilder().excludeFieldsWithoutExposeAnnotation();

        RestAdapter adapter = new RestAdapter.Builder()
//                .setErrorHandler(new AssistanceErrorHandler())
                .setLogLevel(RestAdapter.LogLevel.FULL) // enabling log traces
                .setLog(new AndroidLog("HTTPS_CLIENT"))
                .setConverter(new GsonConverter(gsonBuilder.create()))
                .setEndpoint(Config.ASSISTANCE_ENDPOINT)
                .setClient(new OkClient(new UntrustedOkHttpClient().getClient()))
                .build();

        return adapter.create(serviceClass);
    }
}
