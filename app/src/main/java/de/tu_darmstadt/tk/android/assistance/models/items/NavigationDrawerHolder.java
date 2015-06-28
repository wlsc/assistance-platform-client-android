package de.tu_darmstadt.tk.android.assistance.models.items;


import android.graphics.drawable.Drawable;


/**
 * Holder for navigation drawer
 */
public class NavigationDrawerHolder {

    private String mText;
    private Drawable mDrawable;

    public NavigationDrawerHolder(String text, Drawable drawable) {
        mText = text;
        mDrawable = drawable;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    public Drawable getDrawable() {
        return mDrawable;
    }

    public void setDrawable(Drawable drawable) {
        mDrawable = drawable;
    }
}
