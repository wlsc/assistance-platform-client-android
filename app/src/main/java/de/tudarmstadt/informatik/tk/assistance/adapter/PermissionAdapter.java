package de.tudarmstadt.informatik.tk.assistance.adapter;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import de.tudarmstadt.informatik.tk.assistance.R;
import de.tudarmstadt.informatik.tk.assistance.event.module.CheckIfModuleCapabilityPermissionWasGrantedEvent;
import de.tudarmstadt.informatik.tk.assistance.event.module.settings.ModuleCapabilityHasChangedEvent;
import de.tudarmstadt.informatik.tk.assistance.model.item.PermissionListItem;
import de.tudarmstadt.informatik.tk.assistance.sdk.db.DbModuleCapability;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.sensing.SensorApiType;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.ServiceUtils;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.logger.Log;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 09.11.2015
 */
public class PermissionAdapter extends RecyclerView.Adapter<PermissionAdapter.ViewHolder> {

    private static final String TAG = PermissionAdapter.class.getSimpleName();

    private final EventBus eventBus;

    private List<PermissionListItem> mData;

    private final int requiredState;

    private final boolean isModuleActive;
    private final boolean isInstallView;

    public static final int OPTIONAL = 0;
    public static final int REQUIRED = 1;
    public static final int HIDDEN = 2;

    public PermissionAdapter(List<PermissionListItem> mData,
                             int requiredState,
                             boolean isModuleActive,
                             boolean isInstallView) {

        if (mData == null) {
            this.mData = Collections.emptyList();
        } else {
            this.mData = mData;
        }

        this.isModuleActive = isModuleActive;
        this.isInstallView = isInstallView;
        this.requiredState = requiredState;
        this.eventBus = EventBus.getDefault();
    }

    @Override
    public PermissionAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_permission, viewGroup, false);

        final ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        PermissionListItem permItem = mData.get(position);
        final DbModuleCapability capability = permItem.getCapability();

        String title = "";

        if (capability != null) {
            title = SensorApiType.getName(SensorApiType.getDtoType(capability.getType()), holder.mTitle.getResources());
        }

        holder.mTitle.setText(title);
        holder.mEnablerSwitch.setChecked(permItem.isChecked());

        if (requiredState == REQUIRED) {
            holder.mEnablerSwitch.setChecked(true);
            holder.mEnablerSwitch.setEnabled(false);
        }

        if (requiredState == OPTIONAL) {

            holder.mEnablerSwitch.setVisibility(View.VISIBLE);
            holder.mEnablerSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

                if (isChecked) {
                    Log.d(TAG, "Optional permission was ENABLED");
                } else {
                    Log.d(TAG, "Optional permission was DISABLED");
                }

                permItem.setChecked(isChecked);
                capability.setActive(isChecked);
                permItem.setCapability(capability);

                if (isChecked) {
                    if (eventBus.hasSubscriberForEvent(CheckIfModuleCapabilityPermissionWasGrantedEvent.class)) {
                        eventBus.post(
                                new CheckIfModuleCapabilityPermissionWasGrantedEvent(capability, position));
                    }
                }

                if (ServiceUtils.isHarvesterAbleToRun(holder.mEnablerSwitch.getContext())) {
                    if (eventBus.hasSubscriberForEvent(ModuleCapabilityHasChangedEvent.class)) {
                        // fire state changed
                        eventBus.post(new ModuleCapabilityHasChangedEvent(capability));
                    }
                }

                notifyDataSetChanged();
            });
        }

        if ((requiredState == HIDDEN || !isModuleActive) && !isInstallView) {
            holder.mEnablerSwitch.setVisibility(View.GONE);
        }
    }

    public boolean isModuleActive() {
        return this.isModuleActive;
    }

    /**
     * Swaps out old data with new data in the adapter
     *
     * @param newList
     */
    public void swapData(List<PermissionListItem> newList) {

        if (newList == null) {
            mData = Collections.emptyList();
        } else {
            mData = Lists.newArrayList(newList);
        }

        notifyDataSetChanged();
    }

    /**
     * Returns the total number of items in the data set hold by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return mData.size();
    }

    /**
     * List of current objects in adapter
     *
     * @return
     */
    public List<PermissionListItem> getData() {
        return mData;
    }

    /**
     * View holder for permission dialog
     */
    protected static class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.permission_item_title)
        protected TextView mTitle;

        @Bind(R.id.permission_item_switcher)
        protected SwitchCompat mEnablerSwitch;

        public ViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }
    }

}