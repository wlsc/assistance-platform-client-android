package de.tu_darmstadt.tk.android.assistance.services;

import com.squareup.okhttp.OkHttpClient;

import de.tu_darmstadt.tk.android.assistance.utils.Util;
import retrofit.RestAdapter;
import retrofit.client.OkClient;

/**
 * Created by Wladimir Schmidt on 27.06.2015.
 */
public class ServiceGenerator {

    private ServiceGenerator() {
    }

    public static <T> T createService(Class<T> serviceClass) {

        RestAdapter.Builder builder = new RestAdapter.Builder()
                .setEndpoint(Util.ASSISTANCE_URL)
                .setClient(new OkClient(new OkHttpClient()));

        RestAdapter adapter = builder.build();

        return adapter.create(serviceClass);
    }
}
