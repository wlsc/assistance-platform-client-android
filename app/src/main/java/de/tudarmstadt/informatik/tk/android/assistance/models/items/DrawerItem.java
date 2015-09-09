package de.tudarmstadt.informatik.tk.android.assistance.models.items;


/**
 * Item for navigation drawer
 */
public class DrawerItem {

    private String mText;
    private String mIconUrl;

    public DrawerItem(String text, String url) {
        mText = text;
        mIconUrl = url;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    public String getIconUrl() {
        return mIconUrl;
    }

    public void setIconUrl(String drawable) {
        mIconUrl = drawable;
    }
}
