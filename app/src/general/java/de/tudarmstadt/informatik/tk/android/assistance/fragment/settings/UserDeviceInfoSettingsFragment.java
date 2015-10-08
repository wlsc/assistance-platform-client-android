package de.tudarmstadt.informatik.tk.android.assistance.fragment.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.activity.SettingsActivity;
import de.tudarmstadt.informatik.tk.android.assistance.util.UserUtils;
import de.tudarmstadt.informatik.tk.android.kraken.model.api.endpoint.EndpointGenerator;
import de.tudarmstadt.informatik.tk.android.kraken.model.api.endpoint.DeviceEndpoint;
import de.tudarmstadt.informatik.tk.android.kraken.provider.DbProvider;
import de.tudarmstadt.informatik.tk.android.kraken.db.DbDevice;
import de.tudarmstadt.informatik.tk.android.kraken.db.DbDeviceDao;
import de.tudarmstadt.informatik.tk.android.kraken.model.api.device.DeviceUserDefinedNameRequest;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Wladimir Schmidt on 29.06.2015.
 */
public class UserDeviceInfoSettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = UserDeviceInfoSettingsFragment.class.getSimpleName();

    private Toolbar mParentToolbar;

    private static DbDeviceDao dbDeviceDao;

    public UserDeviceInfoSettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preference_user_device_info);

        mParentToolbar = ((SettingsActivity) getActivity()).getToolBar();
        mParentToolbar.setTitle(R.string.settings_header_user_device_title);

        if (dbDeviceDao == null) {
            dbDeviceDao = DbProvider.getInstance(getActivity().getApplicationContext()).getDaoSession().getDbDeviceDao();
        }

        long currentDeviceId = UserUtils.getCurrentDeviceId(getActivity().getApplicationContext());

        DbDevice dbDevice = dbDeviceDao
                .queryBuilder()
                .where(DbDeviceDao.Properties.Id.eq(currentDeviceId))
                .limit(1)
                .build()
                .unique();

        if (dbDevice != null) {

            String userDeviceName = dbDevice.getUserDefinedName();

            Preference deviceNameProp = findPreference("pref_device_set_title");

            if (userDeviceName != null) {
                deviceNameProp.setDefaultValue(userDeviceName);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override

    public void onDestroy() {
        Log.d(TAG, "Destroying device info fragment. Unbinding data...");
        dbDeviceDao = null;
        super.onDestroy();
    }

    /**
     * Called when a shared preference is changed, added, or removed. This
     * may be called even if a preference is set to its existing value.
     * <p/>
     * <p>This callback will be run on your main thread.
     *
     * @param sharedPreferences The {@link SharedPreferences} that received
     *                          the change.
     * @param key               The key of the preference that was changed, added, or
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        // update user defined device title
        if (key.equalsIgnoreCase("pref_device_set_title")) {

            final String userToken = UserUtils.getUserToken(getActivity().getApplicationContext());
            final long currentDeviceId = UserUtils.getCurrentDeviceId(getActivity().getApplicationContext());
            final String deviceName = sharedPreferences.getString("pref_device_set_title", "");

            DeviceUserDefinedNameRequest deviceUserDefinedNameRequest = new DeviceUserDefinedNameRequest();

            deviceUserDefinedNameRequest.setDeviceId(currentDeviceId);
            deviceUserDefinedNameRequest.setUserDefinedName(deviceName);

            DeviceEndpoint deviceEndpoint = EndpointGenerator.create(DeviceEndpoint.class);
            deviceEndpoint.setUserDefinedName(userToken, deviceUserDefinedNameRequest, new Callback<Void>() {

                @Override
                public void success(Void aVoid, Response response) {

                    if (response != null && (response.getStatus() == 200 || response.getStatus() == 204)) {
                        updateDevice(currentDeviceId, deviceName);
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    // TODO: show here errors to user or not
                }
            });
        }
    }

    /**
     * Updates device's user defined title
     *
     * @param currentDeviceId
     * @param deviceName
     */
    private void updateDevice(long currentDeviceId, String deviceName) {

        Log.d(TAG, "Updating device's user defined name...");

        DbDevice dbDevice = dbDeviceDao
                .queryBuilder()
                .where(DbDeviceDao.Properties.Id.eq(currentDeviceId))
                .limit(1)
                .build()
                .unique();

        if (dbDevice != null) {

            dbDevice.setUserDefinedName(deviceName);

            dbDeviceDao.update(dbDevice);

            Log.d(TAG, "Successful finished updating device's user defined name!");

        } else {
            Log.d(TAG, "Cannot update device information in db");
        }
    }
}
