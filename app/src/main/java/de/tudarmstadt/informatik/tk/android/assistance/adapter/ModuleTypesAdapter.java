package de.tudarmstadt.informatik.tk.android.assistance.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.model.item.ModuleAllowedTypeItem;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 09.01.2016
 */
public class ModuleTypesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "ModuleTypesAdapter";

    private List<ModuleAllowedTypeItem> mData;

    public ModuleTypesAdapter(List<ModuleAllowedTypeItem> data) {

        if (data == null) {
            mData = Collections.emptyList();
        } else {
            mData = data;
        }
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
            mData.clear();
            mData.addAll(newList);
        }

        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_module_allowed_capability, parent, false);
        ModuleTypesViewHolder holder = new ModuleTypesViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof ModuleTypesViewHolder) {

            final ModuleAllowedTypeItem item = getItem(position);
            final ModuleTypesViewHolder viewHolder = (ModuleTypesViewHolder) holder;

            viewHolder.title.setText(item != null ? item.getTitle() : "");
            viewHolder.switcher.setChecked(item.isAllowed());

            if (item.getRequiredByModules() == 0) {
                viewHolder.requiredByModules.setVisibility(View.INVISIBLE);
            } else {
                viewHolder.requiredByModules.setVisibility(View.VISIBLE);
                viewHolder.requiredByModules.setText(((ModuleTypesViewHolder) holder)
                        .requiredByModules
                        .getResources()
                        .getQuantityString(R.plurals.settings_module_types_permission_required_by_modules,
                                item.getRequiredByModules(), item.getRequiredByModules()));
            }
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
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
