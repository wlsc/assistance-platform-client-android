package de.tudarmstadt.informatik.tk.assistance.handler;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 28.01.2016
 */
public class RecyclerViewOnItemClickListener implements RecyclerView.OnItemTouchListener {

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    private OnItemClickListener clickListener;

    GestureDetector gestureDetector;

    public RecyclerViewOnItemClickListener(Context context, OnItemClickListener listener) {

        clickListener = listener;
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onSingleTapUp(MotionEvent event) {
                return true;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent event) {

        View childView = view.findChildViewUnder(event.getX(), event.getY());

        if (childView != null && clickListener != null && gestureDetector.onTouchEvent(event)) {

            clickListener.onItemClick(childView, view.getChildAdapterPosition(childView));
        }

        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) {
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }
}