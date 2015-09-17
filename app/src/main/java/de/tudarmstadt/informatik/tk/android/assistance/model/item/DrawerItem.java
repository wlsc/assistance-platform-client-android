package de.tudarmstadt.informatik.tk.android.assistance.model.item;


import de.tudarmstadt.informatik.tk.android.kraken.db.DbModule;

/**
 * Item for navigation drawer
 */
public class DrawerItem {

    private DbModule module;

    private String title;
    private String iconUrl;

    public DrawerItem() {
    }

    public DrawerItem(String title, String iconUrl, DbModule module) {
        this.module = module;
        this.title = title;
        this.iconUrl = iconUrl;
    }

    public DbModule getModule() {
        return this.module;
    }

    public void setModule(DbModule module) {
        this.module = module;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIconUrl() {
        return this.iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
}
