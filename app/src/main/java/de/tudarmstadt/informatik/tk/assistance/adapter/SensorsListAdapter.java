package de.tudarmstadt.informatik.tk.assistance.adapter;

import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;
import de.tudarmstadt.informatik.tk.assistance.R.drawable;
import de.tudarmstadt.informatik.tk.assistance.R.id;
import de.tudarmstadt.informatik.tk.assistance.R.layout;
import de.tudarmstadt.informatik.tk.assistance.adapter.SensorsListAdapter.ViewHolder;
import de.tudarmstadt.informatik.tk.assistance.fragment.settings.SensorListSettingsFragment;
import de.tudarmstadt.informatik.tk.assistance.model.item.SensorsListItem;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 17.07.2015
 */
public class SensorsListAdapter extends Adapter<ViewHolder> {

    private static final String TAG = SensorListSettingsFragment.class.getSimpleName();

    private List<SensorsListItem> mData;

    public SensorsListAdapter(List<SensorsListItem> mData) {

        if (mData == null) {
            this.mData = Collections.emptyList();
        } else {
            this.mData = mData;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(layout.item_sensor, viewGroup, false);

        final ViewHolder viewHolder = new ViewHolder(view);

        viewHolder.itemView.setClickable(true);
        viewHolder.itemView.setBackgroundResource(drawable.row_selector);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {

        SensorsListItem currentSensor = mData.get(position);

        viewHolder.textView.setText(currentSensor.getName());
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    /**
     * Sensors list item holder
     */
    protected static class ViewHolder extends RecyclerView.ViewHolder {

        protected AppCompatTextView textView;

        public ViewHolder(View view) {
            super(view);

            textView = ButterKnife.findById(view, id.sensor_list_item);
        }
    }
}
