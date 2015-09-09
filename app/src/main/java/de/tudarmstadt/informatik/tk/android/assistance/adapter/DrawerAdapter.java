package de.tudarmstadt.informatik.tk.android.assistance.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pkmmte.view.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.handler.DrawerHandler;
import de.tudarmstadt.informatik.tk.android.assistance.model.item.DrawerItem;


/**
 * Navigation recycler view adapter to manage content
 */
public class DrawerAdapter extends RecyclerView.Adapter<DrawerAdapter.ViewHolder> {

    private Context mContext;

    private List<DrawerItem> mData;
    private DrawerHandler mDrawerHandler;
    private View mSelectedView;
    private int mSelectedPosition;

    public DrawerAdapter(Context context, List<DrawerItem> data) {
        this.mContext = context;
        mData = data;
    }

    public DrawerHandler getNavigationDrawerCallbacks() {
        return mDrawerHandler;
    }

    public void setNavigationDrawerCallbacks(DrawerHandler drawerHandler) {
        mDrawerHandler = drawerHandler;
    }

    @Override
    public DrawerAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.drawer_item, viewGroup, false);

        final ViewHolder viewHolder = new ViewHolder(v);

        viewHolder.itemView.setClickable(true);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {

                                                   @Override
                                                   public void onClick(View v) {

                                                       if (mSelectedView != null) {
                                                           mSelectedView.setSelected(false);
                                                       }

                                                       mSelectedPosition = viewHolder.getAdapterPosition();
                                                       v.setSelected(true);

                                                       mSelectedView = v;
                                                       if (mDrawerHandler != null) {
                                                           mDrawerHandler.onNavigationDrawerItemSelected(viewHolder.getAdapterPosition());
                                                       }
                                                   }
                                               }
        );

        viewHolder.itemView.setBackgroundResource(R.drawable.row_selector);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(DrawerAdapter.ViewHolder viewHolder, int position) {

        Picasso
                .with(mContext)
                .load(mData.get(position).getIconUrl())
                .into(viewHolder.icon);

        viewHolder.textView.setText(mData.get(position).getText());

        if (mSelectedPosition == position) {
            if (mSelectedView != null) {
                mSelectedView.setSelected(false);
            }

            mSelectedPosition = position;
            mSelectedView = viewHolder.itemView;
            mSelectedView.setSelected(true);
        }
    }


    public void selectPosition(int position) {
        mSelectedPosition = position;
        notifyItemChanged(position);
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    /**
     * Drawer item holder
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.item_icon)
        protected CircularImageView icon;

        @Bind(R.id.item_name)
        protected TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }

    public DrawerItem getDataItem(int position) {
        return mData.get(position);
    }
}