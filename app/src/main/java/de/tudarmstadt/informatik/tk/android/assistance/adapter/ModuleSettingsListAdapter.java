package de.tudarmstadt.informatik.tk.android.assistance.adapter;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;
import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModule;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 25.12.2015
 */
public class ModuleSettingsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = ModuleSettingsListAdapter.class.getSimpleName();

    private List<DbModule> modulesList;

    public ModuleSettingsListAdapter(List<DbModule> list) {

        if (list == null) {
            this.modulesList = Collections.emptyList();
        } else {
            this.modulesList = list;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = null;

        // list has items
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_module_settings, parent, false);

        ModuleSettingsViewHolder moduleSettingsHolder = new ModuleSettingsViewHolder(view);

        return moduleSettingsHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof ModuleSettingsViewHolder) {

            final DbModule dbModule = getItem(position);
            final ModuleSettingsViewHolder viewHolder = (ModuleSettingsViewHolder) holder;

            viewHolder.title.setText(dbModule.getTitle());
        }
    }

    @Override
    public int getItemCount() {
        return modulesList.size();
    }

    @Nullable
    public DbModule getItem(int position) {

        if (position < 0 || position >= modulesList.size()) {
            return null;
        }

        return modulesList.get(position);
    }

    /**
     * Swaps out old data with new data in the adapter
     *
     * @param newList
     */
    public void swapData(List<DbModule> newList) {

        if (newList == null) {
            modulesList = Collections.emptyList();
        } else {
            modulesList.clear();
            modulesList.addAll(newList);
        }

        notifyDataSetChanged();
    }

    protected static class ModuleSettingsViewHolder extends RecyclerView.ViewHolder {

        protected final TextView title;

        public ModuleSettingsViewHolder(View view) {
            super(view);

            title = ButterKnife.findById(view, R.id.title);
        }
    }
}
