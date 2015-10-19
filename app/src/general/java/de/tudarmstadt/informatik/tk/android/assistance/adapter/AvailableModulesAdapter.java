package de.tudarmstadt.informatik.tk.android.assistance.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
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

    private final TypedValue mTypedValue = new TypedValue();

    private List<DbModule> modulesList;

    public AvailableModulesAdapter(List<DbModule> modulesList) {
        this.modulesList = modulesList;
    }

    @Override
    public AvailableModulesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.available_module_card_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AvailableModulesAdapter.ViewHolder holder, int position) {

        final DbModule module = getItem(position);

        holder.mMainTitle.setText(module.getTitle());
        holder.mMainSecondaryTitle.setText(module.getDescriptionShort());

        holder.mInstallModule.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new ModuleInstallEvent(module.getPackageName()));
            }
        });

        holder.mMoreInfoModule.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new ModuleShowMoreInfoEvent(module.getPackageName()));
            }
        });

    }

    @Override
    public int getItemCount() {
        return modulesList.size();
    }

    public DbModule getItem(int position) {
        return modulesList.get(position);
    }

    /**
     * View holder for available module
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final View mView;

        protected final TextView mMainTitle;
        protected final TextView mMainSecondaryTitle;
        protected final Button mMoreInfoModule;
        protected final Button mInstallModule;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mMainTitle = ButterKnife.findById(view, R.id.main_title);
            mMainSecondaryTitle = ButterKnife.findById(view, R.id.main_secondary_title);
            mMoreInfoModule = ButterKnife.findById(view, R.id.more_info_module);
            mInstallModule = ButterKnife.findById(view, R.id.install_module);
        }
    }
}
