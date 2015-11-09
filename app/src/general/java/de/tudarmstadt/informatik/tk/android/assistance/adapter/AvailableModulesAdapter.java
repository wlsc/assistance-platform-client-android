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
import de.tudarmstadt.informatik.tk.android.assistance.event.ModuleInstallEvent;
import de.tudarmstadt.informatik.tk.android.assistance.event.ModuleShowMoreInfoEvent;
import de.tudarmstadt.informatik.tk.android.kraken.db.DbModule;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 18.10.2015
 */
public class AvailableModulesAdapter extends RecyclerView.Adapter<AvailableModulesAdapter.ViewHolder> {

    private static final String TAG = AvailableModulesAdapter.class.getSimpleName();

    private static final int EMPTY_VIEW_TYPE = 10;

    private List<DbModule> modulesList;

    public AvailableModulesAdapter(List<DbModule> modulesList) {
        this.modulesList = modulesList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = null;

        if (viewType == EMPTY_VIEW_TYPE) {
            // list is empty
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.empty_view, parent, false);
            EmptyViewHolder emptyView = new EmptyViewHolder(view);

            return emptyView;
        } else {
            // list has items
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.available_module_card_item, parent, false);

            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        if (holder instanceof ViewHolder) {

            final DbModule module = getItem(position);

            final ViewHolder viewHolder = (ViewHolder) holder;

            viewHolder.mMainTitle.setText(module.getTitle());
            viewHolder.mMainSecondaryTitle.setText(module.getDescriptionShort());

            viewHolder.mInstallModule.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    EventBus.getDefault().post(new ModuleInstallEvent(module.getPackageName()));
                }
            });

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
        return modulesList.size();
    }

    public DbModule getItem(int position) {
        return modulesList.get(position);
    }

    @Override
    public int getItemViewType(int position) {

        if (getItemCount() == 0) {
            return EMPTY_VIEW_TYPE;
        }

        return super.getItemViewType(position);
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

        public ViewHolder(View view) {
            super(view);
            mMainTitle = ButterKnife.findById(view, R.id.main_title);
            mMainSecondaryTitle = ButterKnife.findById(view, R.id.main_secondary_title);
            mMoreInfoModule = ButterKnife.findById(view, R.id.more_info_module);
            mInstallModule = ButterKnife.findById(view, R.id.install_module);
        }
    }
}
