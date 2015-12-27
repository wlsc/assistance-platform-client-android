package de.tudarmstadt.informatik.tk.android.assistance.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;
import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.model.item.PermissionListItem;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModuleCapability;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.model.api.dto.DtoType;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 09.11.2015
 */
public class PermissionAdapter extends RecyclerView.Adapter<PermissionAdapter.ViewHolder> {

    private static final String TAG = PermissionAdapter.class.getSimpleName();

    private final List<PermissionListItem> mData;

    private final int requiredState;

    public static final int OPTIONAL = 0;
    public static final int REQUIRED = 1;

    public PermissionAdapter(List<PermissionListItem> mData, int requiredState) {

        if (mData == null) {
            this.mData = Collections.emptyList();
        } else {
            this.mData = mData;
        }

        this.requiredState = requiredState;
    }

    @Override
    public PermissionAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_permission, viewGroup, false);

        final ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        PermissionListItem permItem = mData.get(position);
        DbModuleCapability capability = permItem.getCapability();

        String title = "";

        if (capability != null) {
            title = DtoType.getName(DtoType.getDtoType(capability.getType()), holder.mTitle.getResources());
        }

        holder.mTitle.setText(title);
        holder.mEnablerSwitch.setChecked(permItem.isChecked());

        if (requiredState == REQUIRED) {
            holder.mEnablerSwitch.setChecked(true);
            holder.mEnablerSwitch.setEnabled(false);
        }
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

        protected TextView mTitle;

        protected Switch mEnablerSwitch;

        public ViewHolder(View view) {
            super(view);

            mTitle = ButterKnife.findById(view, R.id.permission_item_title);
            mEnablerSwitch = ButterKnife.findById(view, R.id.permission_item_enabler);
        }
    }

}