package de.tudarmstadt.informatik.tk.assistance.adapter;

import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import de.tudarmstadt.informatik.tk.assistance.R;
import de.tudarmstadt.informatik.tk.assistance.event.module.ModuleAllowedPermissionStateChangedEvent;
import de.tudarmstadt.informatik.tk.assistance.model.item.ModuleRunningSensorTypeItem;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.logger.Log;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 09.01.2016
 */
public class ModuleRunningSensorsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "ModuleRunningSensorsAdapter";

    private List<ModuleRunningSensorTypeItem> mData;

    public ModuleRunningSensorsAdapter(List<ModuleRunningSensorTypeItem> data) {

        if (data == null) {
            mData = Collections.emptyList();
        } else {
            mData = data;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_module_allowed_capability, parent, false);
        ModuleTypesViewHolder holder = new ModuleTypesViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        if (viewHolder instanceof ModuleTypesViewHolder) {

            final ModuleRunningSensorTypeItem item = getItem(position);
            final ModuleTypesViewHolder holder = (ModuleTypesViewHolder) viewHolder;

            holder.title.setText(item != null ? item.getTitle() : "");

            if (holder.switcher.isChecked() != item.isAllowed()) {
                holder.switcher.setChecked(item.isAllowed());
            }

            holder.switcher.setOnClickListener(v -> {

                boolean isChecked = holder.switcher.isChecked();

                if (isChecked) {
                    Log.d(TAG, "Permission ENABLED");
                } else {
                    Log.d(TAG, "Permission DISABLED");
                }

                item.setAllowed(isChecked);

                EventBus.getDefault().post(
                        new ModuleAllowedPermissionStateChangedEvent(
                                item.getType(),
                                isChecked,
                                item.getRequiredByModules()));

//                notifyDataSetChanged();
            });

            if (item.getRequiredByModules() == 0) {
                holder.requiredByModules.setVisibility(View.INVISIBLE);
            } else {
                holder.requiredByModules.setVisibility(View.VISIBLE);
                holder.requiredByModules.setText(holder
                        .requiredByModules
                        .getResources()
                        .getQuantityString(R.plurals.settings_module_allowed_capability_required_by_modules,
                                item.getRequiredByModules(), item.getRequiredByModules()));
            }
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    /**
     * Swaps out old data with new data in the adapter
     *
     * @param newList
     */
    public void swapData(List<ModuleRunningSensorTypeItem> newList) {

        if (newList == null) {
            mData = Collections.emptyList();
        } else {
            mData = Lists.newArrayList(newList);
        }

        notifyDataSetChanged();
    }

    public List<ModuleRunningSensorTypeItem> getData() {
        return mData;
    }

    public ModuleRunningSensorTypeItem getItem(int position) {

        if (position < 0 || position >= mData.size()) {
            return null;
        }

        return mData.get(position);
    }

    /**
     * View holder
     */
    protected static class ModuleTypesViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.title)
        protected AppCompatTextView title;

        @Bind(R.id.switcher)
        protected SwitchCompat switcher;

        @Bind(R.id.requiredBy)
        protected AppCompatTextView requiredByModules;

        public ModuleTypesViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }
    }
}