package de.tudarmstadt.informatik.tk.android.assistance.models.items;


import android.graphics.drawable.Drawable;


/**
 * Item for navigation drawer
 */
public class DrawerItem {

    private String mText;
    private Drawable mDrawable;

    public DrawerItem(String text, Drawable drawable) {
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
