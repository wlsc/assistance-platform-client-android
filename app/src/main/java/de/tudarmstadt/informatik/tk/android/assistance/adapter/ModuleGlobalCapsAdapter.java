package de.tudarmstadt.informatik.tk.android.assistance.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.event.module.ModuleAllowedPermissionStateChangedEvent;
import de.tudarmstadt.informatik.tk.android.assistance.model.item.ModuleAllowedTypeItem;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.logger.Log;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 09.01.2016
 */
public class ModuleGlobalCapsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "ModuleGlobalCapsAdapter";

    private List<ModuleAllowedTypeItem> mData;

    public ModuleGlobalCapsAdapter(List<ModuleAllowedTypeItem> data) {

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

            final ModuleAllowedTypeItem item = getItem(position);
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

                EventBus.getDefault().post(
                        new ModuleAllowedPermissionStateChangedEvent(
                                item.getType(),
                                isChecked,
                                item.getRequiredByModules()));
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
    public void swapData(List<ModuleAllowedTypeItem> newList) {

        if (newList == null) {
            mData = Collections.emptyList();
        } else {
            mData = Lists.newArrayList(newList);
        }

        notifyDataSetChanged();
    }

    public List<ModuleAllowedTypeItem> getData() {
        return mData;
    }

    public ModuleAllowedTypeItem getItem(int position) {

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
        protected TextView title;

        @Bind(R.id.switcher)
        protected Switch switcher;

        @Bind(R.id.requiredBy)
        protected TextView requiredByModules;

        public ModuleTypesViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }
    }
}
