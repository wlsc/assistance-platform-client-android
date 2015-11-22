package de.tudarmstadt.informatik.tk.android.assistance.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.event.module.ModuleInstallEvent;
import de.tudarmstadt.informatik.tk.android.assistance.event.module.ModuleShowMoreInfoEvent;
import de.tudarmstadt.informatik.tk.android.assistance.event.module.ModuleUninstallEvent;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModule;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 18.10.2015
 */
public class AvailableModulesAdapter extends RecyclerView.Adapter<AvailableModulesAdapter.ViewHolder> {

    private static final String TAG = AvailableModulesAdapter.class.getSimpleName();

    private static final int EMPTY_VIEW_TYPE = 10;

    private List<DbModule> modules;

    public AvailableModulesAdapter(List<DbModule> modules) {
        this.modules = modules;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = null;

        if (viewType == EMPTY_VIEW_TYPE) {
            // list is empty
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_list_empty_view, parent, false);

            return new EmptyViewHolder(view);
        } else {
            // list has items
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_available_module_card, parent, false);

            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        // only for non empty list
        if (holder instanceof ViewHolder) {

            final DbModule module = getItem(position);

            final ViewHolder viewHolder = (ViewHolder) holder;

            viewHolder.mMainTitle.setText(module.getTitle());
            viewHolder.mMainSecondaryTitle.setText(module.getDescriptionShort());

            if (module.getActive()) {

                viewHolder.mUninstallModule.setVisibility(View.VISIBLE);
                viewHolder.mInstallModule.setVisibility(View.GONE);

                viewHolder.mUninstallModule.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        EventBus.getDefault().post(new ModuleUninstallEvent(module.getPackageName()));
                    }
                });

            } else {

                viewHolder.mUninstallModule.setVisibility(View.GONE);
                viewHolder.mInstallModule.setVisibility(View.VISIBLE);

                viewHolder.mInstallModule.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        EventBus.getDefault().post(new ModuleInstallEvent(module.getPackageName()));
                    }
                });
            }

            viewHolder.mMoreInfoModule.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    EventBus.getDefault().post(new ModuleShowMoreInfoEvent(module.getPackageName()));
                }
            });
        }
    }

    @Override
    public int getItemCount() {

        if (modules == null) {
            return 0;
        }

        return modules.size();
    }

    /**
     * Returns item on given position
     *
     * @param position
     * @return
     */
    public DbModule getItem(int position) {

        if (position < 0 || position >= modules.size()) {
            return null;
        }

        return modules.get(position);
    }

    /**
     * Returns item by pacakge id
     *
     * @param modulePackageId
     * @return
     */
    public DbModule getItem(String modulePackageId) {

        if (getItemCount() == 0 || modulePackageId == null) {
            return null;
        }

        for (DbModule module : modules) {

            if (modulePackageId.equals(module.getPackageName())) {
                return module;
            }
        }

        return null;
    }

    @Override
    public int getItemViewType(int position) {

        if (getItemCount() == 0) {
            return EMPTY_VIEW_TYPE;
        }

        return super.getItemViewType(position);
    }

    /**
     * Swaps out old data with new data in the adapter
     *
     * @param newList
     */
    public void swapData(List<DbModule> newList) {

        modules.clear();
        modules.addAll(newList);
        notifyDataSetChanged();
    }

    /**
     * An empty view holder if no items available
     */
    public class EmptyViewHolder extends AvailableModulesAdapter.ViewHolder {
        public EmptyViewHolder(View view) {
            super(view);
        }
    }

    /**
     * View holder for available module
     */
    protected static class ViewHolder extends RecyclerView.ViewHolder {

        protected final TextView mMainTitle;
        protected final TextView mMainSecondaryTitle;
        protected final Button mMoreInfoModule;
        protected final Button mInstallModule;
        protected final Button mUninstallModule;

        public ViewHolder(View view) {
            super(view);
            mMainTitle = ButterKnife.findById(view, R.id.main_title);
            mMainSecondaryTitle = ButterKnife.findById(view, R.id.main_secondary_title);
            mMoreInfoModule = ButterKnife.findById(view, R.id.more_info_module);
            mInstallModule = ButterKnife.findById(view, R.id.install_module);
            mUninstallModule = ButterKnife.findById(view, R.id.uninstall_module);
        }
    }
}
