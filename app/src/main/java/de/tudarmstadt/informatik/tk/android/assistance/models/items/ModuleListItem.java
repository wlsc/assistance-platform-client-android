package de.tudarmstadt.informatik.tk.android.assistance.models.items;

import android.graphics.drawable.Drawable;

/**
 * Created by Wladimir Schmidt on 17.06.2015.
 */
public class ModuleListItem {

    private Drawable mLogo;
    private String mTitle;
    private String mShortDescription;

    public void ModuleListItem(Drawable logo, String title, String shortDescription){
        this.mLogo = logo;
        this.mTitle = title;
        this.mShortDescription = shortDescription;
    }

    public Drawable getmLogo() {
        return mLogo;
    }

    public void setmLogo(Drawable mLogo) {
        this.mLogo = mLogo;
    }

    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getmShortDescription() {
        return mShortDescription;
    }

    public void setmShortDescription(String mShortDescription) {
        this.mShortDescription = mShortDescription;
    }
}
