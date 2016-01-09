package de.tudarmstadt.informatik.tk.android.assistance.model.item;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 09.01.2016
 */
public class ModuleAllowedTypeItem {

    private String title;

    private boolean allowed;

    private int requiredByModules;

    public ModuleAllowedTypeItem(String title, int requiredByModules) {
        this.title = title;
        this.allowed = true;
        this.requiredByModules = requiredByModules;
    }

    public ModuleAllowedTypeItem(String title) {
        this.title = title;
        this.allowed = true;
        this.requiredByModules = 0;
    }

    public ModuleAllowedTypeItem(String title, boolean allowed, int requiredByModules) {
        this.title = title;
        this.allowed = allowed;
        this.requiredByModules = requiredByModules;
    }

    public String getTitle() {
        return this.title;
    }

    public boolean isAllowed() {
        return this.allowed;
    }

    public int getRequiredByModules() {
        return this.requiredByModules;
    }

    @Override
    public String toString() {
        return "ModuleAllowedTypeItem{" +
                "title='" + title + '\'' +
                ", allowed=" + allowed +
                ", requiredByModules=" + requiredByModules +
                '}';
    }
}