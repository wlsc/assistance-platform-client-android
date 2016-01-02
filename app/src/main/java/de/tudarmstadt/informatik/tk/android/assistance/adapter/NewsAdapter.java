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
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.ClientFeedbackDto;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 24.10.2015
 */
public class NewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int EMPTY_VIEW_TYPE = 10;

    private List<ClientFeedbackDto> news;

    public NewsAdapter(List<ClientFeedbackDto> news) {

        if (news == null) {
            this.news = Collections.emptyList();
        } else {
            this.news = news;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = null;

        if (viewType == EMPTY_VIEW_TYPE) {
            // list is empty
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_empty_list_view, parent, false);
            EmptyViewHolder emptyView = new EmptyViewHolder(view);

            return emptyView;
        }

        // list has items
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_news_card, parent, false);
        NewsViewHolder newsHolder = new NewsViewHolder(view);

        return newsHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof NewsViewHolder) {

            final ClientFeedbackDto newsCard = getItem(position);
            final NewsViewHolder viewHolder = (NewsViewHolder) holder;

            viewHolder.mContent.setText(newsCard.getContent().toString());
        }
    }

    @Override
    public int getItemCount() {
        return news.size();
    }

    @Nullable
    public ClientFeedbackDto getItem(int position) {

        if (position < 0 || position >= news.size()) {
            return null;
        }

        return news.get(position);
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
    public void swapData(List<ClientFeedbackDto> newList) {

        if (newList == null) {
            news = Collections.emptyList();
        } else {
            news.clear();
            news.addAll(newList);
        }

        notifyDataSetChanged();
    }

    /**
     * An empty view holder if no items available
     */
    public class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(View view) {
            super(view);
        }
    }

    /**
     * View holder for available module
     */
    protected static class NewsViewHolder extends RecyclerView.ViewHolder {

        protected final TextView mContent;

        public NewsViewHolder(View view) {
            super(view);
            mContent = ButterKnife.findById(view, R.id.content);
        }
    }
}
