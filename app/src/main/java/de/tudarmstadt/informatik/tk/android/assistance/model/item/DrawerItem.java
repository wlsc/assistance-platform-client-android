package de.tudarmstadt.informatik.tk.android.assistance.model.item;


import de.tudarmstadt.informatik.tk.android.kraken.db.Module;

/**
 * Item for navigation drawer
 */
public class DrawerItem {

    private Module module;

    private String title;
    private String iconUrl;

    public DrawerItem() {
    }

    public DrawerItem(String title, String iconUrl, Module module) {
        this.module = module;
        this.title = title;
        this.iconUrl = iconUrl;
    }

    public Module getModule() {
        return this.module;
    }

    public void setModule(Module module) {
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
