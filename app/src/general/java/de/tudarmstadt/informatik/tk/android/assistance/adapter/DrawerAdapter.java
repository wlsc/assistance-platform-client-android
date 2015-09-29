package de.tudarmstadt.informatik.tk.android.assistance.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pkmmte.view.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.handler.DrawerClickHandler;
import de.tudarmstadt.informatik.tk.android.assistance.model.item.DrawerItem;


/**
 * Navigation recycler view adapter to manage content
 */
public class DrawerAdapter extends RecyclerView.Adapter<DrawerAdapter.ViewHolder> {

    private Context mContext;

    private List<DrawerItem> mData;
    private static DrawerClickHandler mDrawerClickHandler;
    private View mSelectedView;
    private int mSelectedPosition;

    public DrawerAdapter(Context context, List<DrawerItem> data, DrawerClickHandler drawerClickHandler) {
        this.mContext = context;
        mData = data;
        this.mDrawerClickHandler = drawerClickHandler;
    }

    @Override
    public DrawerAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.drawer_item, viewGroup, false);

        final ViewHolder viewHolder = new ViewHolder(v);

        viewHolder.itemView.setBackgroundResource(R.drawable.row_selector);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(DrawerAdapter.ViewHolder viewHolder, int position) {

        String iconUrl = mData.get(position).getIconUrl();

        Picasso
                .with(mContext)
                .load(iconUrl)
                .placeholder(R.drawable.no_image)
                .into(viewHolder.icon);

        viewHolder.textView.setText(mData.get(position).getTitle());

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
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @Bind(R.id.item_icon)
        protected CircularImageView icon;

        @Bind(R.id.item_name)
        protected TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);

            icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            icon.setMaxHeight(10);
        }

        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            mDrawerClickHandler.onNavigationDrawerItemSelected(v, this.getLayoutPosition());
        }
    }

    public DrawerItem getDataItem(int position) {
        return mData.get(position);
    }
}