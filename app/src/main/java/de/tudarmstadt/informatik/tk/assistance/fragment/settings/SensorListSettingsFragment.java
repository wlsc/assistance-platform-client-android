package de.tudarmstadt.informatik.tk.assistance.fragment.settings;

import android.app.Fragment;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.tudarmstadt.informatik.tk.assistance.R;
import de.tudarmstadt.informatik.tk.assistance.activity.SettingsActivity;
import de.tudarmstadt.informatik.tk.assistance.adapter.SensorsListAdapter;
import de.tudarmstadt.informatik.tk.assistance.model.item.SensorsListItem;
import de.tudarmstadt.informatik.tk.assistance.util.PreferenceUtils;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 17.07.2015
 */
public class SensorListSettingsFragment extends Fragment {

    private static final String TAG = SensorListSettingsFragment.class.getSimpleName();

    private Toolbar mParentToolbar;

    @Bind(R.id.sensor_list)
    protected RecyclerView recyclerView;

    public SensorListSettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mParentToolbar = ((SettingsActivity) getActivity()).getToolBar();
        mParentToolbar.setTitle(R.string.settings_list_of_sensors_title);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_preference_sensors_list, container, false);

        ButterKnife.bind(this, view);

        recyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(layoutManager);

        List<SensorsListItem> sensorsListItems = getSensorsList();

        SensorsListAdapter sensorListAdapter = new SensorsListAdapter(sensorsListItems);
        recyclerView.setAdapter(sensorListAdapter);

        return view;
    }

    /**
     * Supplies a list of available sensors on particular device
     *
     * @return
     */
    private List<SensorsListItem> getSensorsList() {

        boolean isDeveloperEnabled = PreferenceUtils.getPreference(getActivity(), "pref_be_developer", false);

        List<SensorsListItem> sensorsListItems = new ArrayList<>();
        SensorManager manager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = manager.getSensorList(Sensor.TYPE_ALL);


        for (Sensor sensor : sensors) {
            SensorsListItem item = new SensorsListItem(sensor.getName());
            item.setVisible(isDeveloperEnabled);
            sensorsListItems.add(item);
        }

        return sensorsListItems;
    }
}
